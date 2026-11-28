package com.example.core

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val MAX_CHAT_AUDIO_DATA_URI_CHARS = 450_000
private val CHAT_AUDIO_REGEX = Regex(
    "^data:audio/(mp4|m4a|aac|mpeg);base64,[A-Za-z0-9+/=]+$",
    RegexOption.IGNORE_CASE
)

fun isValidChatAudioInput(value: String): Boolean {
    val trimmed = value.trim()
    if (trimmed.isEmpty()) return true
    return trimmed.length <= MAX_CHAT_AUDIO_DATA_URI_CHARS && CHAT_AUDIO_REGEX.matches(trimmed)
}

fun formatAudioDuration(seconds: Long): String {
    val safeSec = seconds.coerceAtLeast(0L)
    val mins = safeSec / 60L
    val remSec = safeSec % 60L
    return "$mins:${remSec.toString().padStart(2, '0')}"
}

fun formatBlockRemainingTime(remainingMs: Long): String {
    val totalSec = (remainingMs / 1000L).coerceAtLeast(0L)
    val hours = totalSec / 3600L
    val mins = (totalSec % 3600L) / 60L
    val secs = totalSec % 60L
    return when {
        hours > 0L -> "${hours} ঘণ্টা ${mins} মিনিট"
        mins > 0L -> "${mins} মিনিট ${secs} সেকেন্ড"
        else -> "${secs} সেকেন্ড"
    }
}

class VoiceRecorderManager(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startedAtMs: Long = 0L

    val isRecording: Boolean
        get() = recorder != null

    fun startRecording(): Result<Unit> = runCatching {
        cancelRecording()
        val file = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
        val mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        try {
            mediaRecorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(16_000)
                setAudioEncodingBitRate(24_000)
                setMaxDuration(60_000)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            recorder = mediaRecorder
            outputFile = file
            startedAtMs = System.currentTimeMillis()
        } catch (t: Throwable) {
            runCatching { mediaRecorder.release() }
            runCatching { file.delete() }
            throw t
        }
        Unit
    }

    suspend fun stopAndEncode(): Result<Pair<String, Long>> = withContext(Dispatchers.IO) {
        val activeRecorder = recorder
        val file = outputFile
        val elapsedSec = ((System.currentTimeMillis() - startedAtMs) / 1000L).coerceIn(1L, 300L)
        recorder = null
        outputFile = null
        startedAtMs = 0L

        if (activeRecorder == null || file == null) {
            return@withContext Result.failure(IllegalStateException("রেকর্ডিং শুরু হয়নি।"))
        }

        try {
            runCatching { activeRecorder.stop() }
            runCatching { activeRecorder.release() }
            if (!file.exists() || file.length() == 0L) {
                return@withContext Result.failure(IllegalStateException("ভয়েস রেকর্ড সংরক্ষণ করা যায়নি।"))
            }
            val bytes = file.readBytes()
            val encoded = Base64.encodeToString(bytes, Base64.NO_WRAP)
            val dataUri = "data:audio/mp4;base64,$encoded"
            if (dataUri.length > MAX_CHAT_AUDIO_DATA_URI_CHARS) {
                return@withContext Result.failure(
                    IllegalStateException("ভয়েস মেসেজ অনেক বড়। অনুগ্রহ করে ১ মিনিটের মধ্যে রাখুন।")
                )
            }
            Result.success(dataUri to elapsedSec)
        } finally {
            runCatching { file.delete() }
        }
    }

    fun cancelRecording() {
        val activeRecorder = recorder
        val file = outputFile
        recorder = null
        outputFile = null
        startedAtMs = 0L
        if (activeRecorder != null) {
            runCatching { activeRecorder.stop() }
            runCatching { activeRecorder.release() }
        }
        if (file != null) {
            runCatching { file.delete() }
        }
    }
}

class VoicePlayerController(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var activeMessageId: String? = null

    fun currentPlayingId(): String? = activeMessageId

    fun isCurrentlyPlaying(messageId: String): Boolean {
        return activeMessageId == messageId && (runCatching { mediaPlayer?.isPlaying == true }.getOrDefault(false))
    }

    fun currentProgressFraction(): Float {
        val player = mediaPlayer ?: return 0f
        return runCatching {
            val dur = player.duration
            if (dur <= 0) 0f else (player.currentPosition.toFloat() / dur.toFloat()).coerceIn(0f, 1f)
        }.getOrDefault(0f)
    }

    fun togglePlay(
        messageId: String,
        audioDataUri: String,
        onStateChanged: (playingId: String?, isPlaying: Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        if (activeMessageId == messageId && mediaPlayer != null) {
            val player = mediaPlayer!!
            runCatching {
                if (player.isPlaying) {
                    player.pause()
                    onStateChanged(messageId, false)
                } else {
                    player.start()
                    onStateChanged(messageId, true)
                }
            }.onFailure {
                stop()
                onStateChanged(null, false)
            }
            return
        }

        stop()
        val commaIdx = audioDataUri.indexOf(',')
        if (commaIdx <= 0) {
            onError("ভয়েস ফাইল পড়া যায়নি।")
            return
        }
        val base64Payload = audioDataUri.substring(commaIdx + 1)
        val audioFile = File(context.cacheDir, "voice_play_${messageId.hashCode()}.m4a")
        runCatching {
            val bytes = Base64.decode(base64Payload, Base64.DEFAULT)
            audioFile.writeBytes(bytes)
            val player = MediaPlayer().apply {
                setDataSource(audioFile.absolutePath)
                setOnCompletionListener {
                    activeMessageId = null
                    onStateChanged(null, false)
                }
                prepare()
                start()
            }
            mediaPlayer = player
            activeMessageId = messageId
            onStateChanged(messageId, true)
        }.onFailure {
            stop()
            onStateChanged(null, false)
            onError("ভয়েস মেসেজ চালানো যায়নি।")
        }
    }

    fun stop() {
        mediaPlayer?.let { player ->
            runCatching {
                if (player.isPlaying) player.stop()
            }
            runCatching { player.release() }
        }
        mediaPlayer = null
        activeMessageId = null
    }
}
