package com.example.data.repository

import android.graphics.Bitmap
import android.util.Log
import com.example.core.diagnoseAppCheckFailure
import com.example.data.model.AiMessage
import com.example.data.model.AiRemoteConfig
import com.example.data.model.AiTutorMode
import com.example.data.model.AnswerLength
import com.example.data.model.GeneratedMcqItem
import com.example.data.model.StructuredAiAnswer
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class AiRepository {

    companion object {
        private const val TAG = "AiRepository"
        private const val DEFAULT_MODEL = "gemini-3.8-flash"
        private const val FALLBACK_LITE_MODEL = "gemini-3.5-flash-lite"
        private const val TIMEOUT_MS = 60_000L
        private const val RETRY_DELAY_MS = 900L
        private const val SWITCH_DELAY_MS = 250L
        private const val MAX_INLINE_IMAGE_BYTES = 18 * 1024 * 1024 // Stay safely under 20MB limit

        internal fun buildCandidateModels(configuredModel: String): List<String> {
            val primary = configuredModel.trim().ifBlank { DEFAULT_MODEL }
            return listOf(primary, FALLBACK_LITE_MODEL, DEFAULT_MODEL).distinct()
        }

        internal fun isTransientCapacityError(error: Throwable): Boolean {
            val combined = generateSequence(error) { it.cause }
                .joinToString(" | ") { "${it.javaClass.simpleName}: ${it.localizedMessage.orEmpty()}" }
                .lowercase()
            if (combined.contains("app check")) return false
            return combined.contains("high demand") ||
                combined.contains("spikes in demand") ||
                combined.contains("overloaded") ||
                combined.contains("503") ||
                combined.contains("429") ||
                combined.contains("unavailable") ||
                combined.contains("resource_exhausted") ||
                combined.contains("rate limit") ||
                combined.contains("temporarily") ||
                combined.contains("try again later") ||
                combined.contains("serverexception") ||
                combined.contains("serviceunavailable")
        }

        internal fun formatFriendlyAiError(error: Throwable): String {
            val msg = error.localizedMessage.orEmpty()
            return if (isTransientCapacityError(error)) {
                "এই মুহূর্তে AI মডেলে অতিরিক্ত চাপ রয়েছে (High demand)। স্বয়ংক্রিয়ভাবে বিকল্প মডেল চেষ্টা করা হয়েছে—অনুগ্রহ করে কয়েক সেকেন্ড পর আবার চেষ্টা করুন।"
            } else {
                msg.ifBlank { "AI response generation failed. Please try again." }
            }
        }
    }

    private val structuredAnswerSchema = Schema.obj(
        properties = mapOf(
            "prashno" to Schema.string("Restated clear academic question in the target language"),
            "bhumika" to Schema.string("Introduction (ভূমিকা)"),
            "mulAlochona" to Schema.string("Main analytical discussion (মূল আলোচনা)"),
            "importantPoints" to Schema.array(
                items = Schema.string(),
                description = "Key bullet points (গুরুত্বপূর্ণ পয়েন্ট)"
            ),
            "examples" to Schema.string("Relevant political science or historical examples (প্রয়োজনীয় উদাহরণ)"),
            "uposhonghar" to Schema.string("Conclusion (উপসংহার)"),
            "sourceNote" to Schema.string("Clarify verified curriculum concepts vs AI explanation and flag any syllabus uncertainty")
        )
    )

    private val mcqArraySchema = Schema.obj(
        properties = mapOf(
            "items" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "question" to Schema.string("MCQ question stem"),
                        "options" to Schema.array(Schema.string(), "4 answer options"),
                        "correctIndex" to Schema.integer("Zero-based index (0..3) of the correct option"),
                        "explanation" to Schema.string("Brief academic explanation")
                    )
                )
            )
        )
    )

    private val defaultChatModel by lazy {
        Firebase.ai.generativeModel(
            modelName = DEFAULT_MODEL,
            systemInstruction = content {
                text(buildSystemInstruction(null))
            }
        )
    }

    private fun createStructuredAnswerModel(modelId: String, remoteConfig: AiRemoteConfig): GenerativeModel {
        return Firebase.ai.generativeModel(
            modelName = modelId,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = structuredAnswerSchema
                temperature = 0.35f
            },
            systemInstruction = content {
                text(buildSystemInstruction(remoteConfig.systemInstructions))
            }
        )
    }

    private fun createTutorModel(modelId: String, remoteConfig: AiRemoteConfig): GenerativeModel {
        return if (modelId == DEFAULT_MODEL && remoteConfig.systemInstructions.isBlank()) {
            defaultChatModel
        } else {
            Firebase.ai.generativeModel(
                modelName = modelId,
                systemInstruction = content {
                    text(buildSystemInstruction(remoteConfig.systemInstructions))
                }
            )
        }
    }

    private fun createMcqModel(modelId: String, remoteConfig: AiRemoteConfig): GenerativeModel {
        return Firebase.ai.generativeModel(
            modelName = modelId,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = mcqArraySchema
                temperature = 0.4f
            },
            systemInstruction = content {
                text(buildSystemInstruction(remoteConfig.systemInstructions))
            }
        )
    }

    private suspend fun <T> executeWithModelFallback(
        configuredModel: String,
        block: suspend (modelId: String) -> T
    ): T {
        val candidates = buildCandidateModels(configuredModel)
        // Order: try each candidate once quickly, then one delayed retry on the lite fallback if all hit transient demand
        val attempts = candidates + listOf(FALLBACK_LITE_MODEL)
        var lastError: Throwable? = null
        return withTimeout(TIMEOUT_MS) {
            for ((index, modelId) in attempts.withIndex()) {
                try {
                    return@withTimeout block(modelId)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    lastError = e
                    Log.w(TAG, "Model '$modelId' failed on attempt ${index + 1}/${attempts.size}: ${e.localizedMessage}")
                    val canRecover = isTransientCapacityError(e) && index < attempts.lastIndex
                    if (!canRecover) {
                        throw e
                    }
                    val waitMs = if (index >= candidates.lastIndex) RETRY_DELAY_MS else SWITCH_DELAY_MS
                    kotlinx.coroutines.delay(waitMs)
                }
            }
            throw (lastError ?: IllegalStateException("AI request failed."))
        }
    }

    private fun buildSystemInstruction(adminExtraInstructions: String?): String = buildString {
        appendLine("You are the official Honours Solution AI Academic Tutor for National University (NU), Bangladesh students.")
        appendLine("Primary focus: Political Science Honours (1st Year to 4th Year) and general Honours study guidance.")
        appendLine("ACCURACY RULES:")
        appendLine("1. Prioritize verified academic facts and National University exam conventions.")
        appendLine("2. NEVER invent or fabricate official NU course codes, marks distributions, or syllabus policies if uncertain—explicitly state when students should cross-check with the official NU syllabus or uploaded admin notes.")
        appendLine("3. Distinguish verified textbook concepts from AI-generated explanations.")
        appendLine("4. Generate original, plagiarism-free academic explanations rather than reproducing copyrighted textbooks verbatim.")
        appendLine("5. Support fluent, formal academic Bengali (বাংলা) and English.")
        if (!adminExtraInstructions.isNullOrBlank()) {
            appendLine()
            appendLine("ADMIN ACADEMIC INSTRUCTIONS:")
            appendLine(adminExtraInstructions)
        }
    }

    suspend fun solveQuestionStructured(
        questionText: String,
        bitmaps: List<Bitmap>,
        yearLabel: String,
        subjectName: String,
        answerLength: AnswerLength,
        language: String,
        verifiedContext: String,
        remoteConfig: AiRemoteConfig
    ): Result<StructuredAiAnswer> {
        return try {
            val safeBitmaps = downscaleBitmapsUnderLimit(bitmaps)
            val targetLangDesc = if (language == "en") "Academic English" else "Academic Bengali (বাংলা)"

            val promptText = buildString {
                appendLine("Academic Context:")
                appendLine("- University: National University (NU), Bangladesh")
                appendLine("- Year: $yearLabel")
                appendLine("- Subject: $subjectName")
                appendLine("- Requested Answer Length: ${answerLength.labelBn} (${answerLength.labelEn}) — ${answerLength.guidance}")
                appendLine("- Response Language: $targetLangDesc")
                if (verifiedContext.isNotBlank()) {
                    appendLine("- Verified App Material Reference:")
                    appendLine(verifiedContext.take(2500))
                }
                appendLine()
                appendLine("Student Question:")
                appendLine(questionText.ifBlank { "Please read and solve the academic question shown in the attached image(s)." })
            }

            val response = executeWithModelFallback(remoteConfig.modelName) { modelId ->
                val model = createStructuredAnswerModel(modelId, remoteConfig)
                model.generateContent(
                    content {
                        text(promptText)
                        safeBitmaps.forEach { bmp -> image(bmp) }
                    }
                )
            }

            val rawJson = response.text
                ?: return Result.failure(IllegalStateException("AI returned an empty response."))

            val json = JSONObject(rawJson)
            val pointsJson = json.optJSONArray("importantPoints")
            val points = if (pointsJson != null) {
                (0 until pointsJson.length()).mapNotNull { idx ->
                    pointsJson.optString(idx).takeIf { it.isNotBlank() }
                }
            } else {
                emptyList()
            }

            val structured = StructuredAiAnswer(
                question = questionText.ifBlank { json.optString("prashno") },
                answerLength = answerLength.labelBn,
                yearLabel = yearLabel,
                subjectName = subjectName,
                language = language,
                prashno = json.optString("prashno", questionText).ifBlank { questionText },
                bhumika = json.optString("bhumika"),
                mulAlochona = json.optString("mulAlochona"),
                importantPoints = points,
                examples = json.optString("examples"),
                uposhonghar = json.optString("uposhonghar"),
                sourceNote = json.optString(
                    "sourceNote",
                    "AI-generated academic explanation. Verify official syllabus details with NU documents."
                ),
                hasImageInput = safeBitmaps.isNotEmpty()
            )
            Result.success(structured)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI question solver timed out", e)
            Result.failure(Exception("সময়সীমা শেষ হয়েছে (60 সেকেন্ড)। অনুগ্রহ করে আবার চেষ্টা করুন।"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI question solver failed", e)
            val msg = e.localizedMessage.orEmpty()
            if (msg.contains("App Check", ignoreCase = true)) {
                val diag = diagnoseAppCheckFailure()
                Result.failure(Exception("$msg\n[$diag]", e))
            } else {
                Result.failure(Exception(formatFriendlyAiError(e), e))
            }
        }
    }

    fun streamTutorResponse(
        userPrompt: String,
        history: List<AiMessage>,
        mode: AiTutorMode,
        yearLabel: String,
        subjectName: String,
        remoteConfig: AiRemoteConfig
    ): Flow<String> = flow {
        val chatHistory = history.takeLast(12).map { msg ->
            content(if (msg.role == "model") "model" else "user") {
                text(msg.text)
            }
        }
        val contextualPrompt = buildString {
            appendLine("[Mode: ${mode.labelEn} (${mode.labelBn}) | $yearLabel | $subjectName]")
            appendLine(mode.promptPrefix)
            appendLine()
            append(userPrompt)
        }

        val candidates = buildCandidateModels(remoteConfig.modelName)
        val attempts = candidates + listOf(FALLBACK_LITE_MODEL)
        val accumulated = StringBuilder()

        withTimeout(TIMEOUT_MS) {
            var succeeded = false
            var lastError: Throwable? = null
            for ((index, modelId) in attempts.withIndex()) {
                try {
                    val model = createTutorModel(modelId, remoteConfig)
                    val chat = model.startChat(history = chatHistory)
                    chat.sendMessageStream(contextualPrompt).collect { chunk ->
                        val chunkText = chunk.text.orEmpty()
                        if (chunkText.isNotEmpty()) {
                            accumulated.append(chunkText)
                            emit(accumulated.toString())
                        }
                    }
                    if (accumulated.isBlank()) {
                        throw IllegalStateException("AI tutor returned an empty response.")
                    }
                    succeeded = true
                    break
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    lastError = e
                    Log.w(TAG, "Tutor stream model '$modelId' failed on attempt ${index + 1}/${attempts.size}: ${e.localizedMessage}")
                    val canFallback = accumulated.isEmpty() && isTransientCapacityError(e) && index < attempts.lastIndex
                    if (!canFallback) {
                        throw e
                    }
                    val waitMs = if (index >= candidates.lastIndex) RETRY_DELAY_MS else SWITCH_DELAY_MS
                    kotlinx.coroutines.delay(waitMs)
                }
            }
            if (!succeeded) {
                throw (lastError ?: IllegalStateException("AI tutor returned an empty response."))
            }
        }
    }.catch { error ->
        if (error is CancellationException && error !is TimeoutCancellationException) {
            throw error
        }
        Log.e(TAG, "Tutor stream failed", error)
        throw if (error is TimeoutCancellationException) {
            Exception("সময়সীমা শেষ হয়েছে (60 সেকেন্ড)। অনুগ্রহ করে আবার চেষ্টা করুন।", error)
        } else if ((error.localizedMessage ?: "").contains("App Check", ignoreCase = true)) {
            val diag = diagnoseAppCheckFailure()
            Exception("${error.localizedMessage}\n[$diag]", error)
        } else {
            Exception(formatFriendlyAiError(error), error)
        }
    }

    suspend fun generateMcqsStructured(
        topicOrSubject: String,
        yearLabel: String,
        count: Int,
        language: String,
        remoteConfig: AiRemoteConfig
    ): Result<List<GeneratedMcqItem>> {
        return try {
            val langLabel = if (language == "en") "English" else "Bengali (বাংলা)"
            val prompt = "Generate $count National University Honours ($yearLabel) practice MCQs on '$topicOrSubject' in $langLabel. Each MCQ must have exactly 4 distinct options, accurate correctIndex (0-3), and a concise explanation."
            val response = executeWithModelFallback(remoteConfig.modelName) { modelId ->
                val model = createMcqModel(modelId, remoteConfig)
                model.generateContent(prompt)
            }
            val jsonText = response.text
                ?: return Result.failure(IllegalStateException("Empty MCQ response from AI."))
            val root = JSONObject(jsonText)
            val arr = root.getJSONArray("items")
            val list = (0 until arr.length()).map { i ->
                val item = arr.getJSONObject(i)
                val optsArr = item.getJSONArray("options")
                val opts = (0 until optsArr.length()).map { j -> optsArr.getString(j) }
                GeneratedMcqItem(
                    question = item.getString("question"),
                    options = opts,
                    correctIndex = item.optInt("correctIndex", 0).coerceIn(0, (opts.size - 1).coerceAtLeast(0)),
                    explanation = item.optString("explanation")
                )
            }
            Result.success(list)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "MCQ generation timed out", e)
            Result.failure(Exception("MCQ তৈরির সময়সীমা শেষ হয়েছে। অনুগ্রহ করে আবার চেষ্টা করুন।"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "MCQ generation failed", e)
            val msg = e.localizedMessage.orEmpty()
            if (msg.contains("App Check", ignoreCase = true)) {
                val diag = diagnoseAppCheckFailure()
                Result.failure(Exception("$msg\n[$diag]", e))
            } else {
                Result.failure(Exception(formatFriendlyAiError(e), e))
            }
        }
    }

    private fun downscaleBitmapsUnderLimit(bitmaps: List<Bitmap>): List<Bitmap> {
        var totalBytes = 0
        val out = mutableListOf<Bitmap>()
        for (bmp in bitmaps.take(3)) {
            val stream = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 85, stream)
            val size = stream.size()
            if (totalBytes + size < MAX_INLINE_IMAGE_BYTES) {
                out.add(bmp)
                totalBytes += size
            }
        }
        return out
    }
}
