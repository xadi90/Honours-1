package com.example.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

private const val MAX_COLLEGE_IMAGE_DATA_URI_CHARS = 350_000

fun isValidCollegeImageInput(value: String): Boolean {
    val trimmed = value.trim()
    if (trimmed.isEmpty()) return true
    if (trimmed.startsWith("https://", ignoreCase = true) && trimmed.length <= 1000) return true
    if (trimmed.startsWith("data:image/", ignoreCase = true) && trimmed.length <= 450_000) return true
    return false
}

suspend fun encodePickedImageToDataUri(context: Context, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
    runCatching {
        val decodedBitmap = decodeUriToBitmap(context, uri)
            ?: error("ছবিটি পড়া যায়নি। অনুগ্রহ করে অন্য একটি ছবি (JPG/PNG/WEBP/HEIC) চেষ্টা করুন।")

        var workingBitmap = downscaleBitmapIfNeeded(decodedBitmap, 960)
        if (workingBitmap !== decodedBitmap) {
            decodedBitmap.recycle()
        }

        try {
            val targetDims = intArrayOf(960, 640, 480, 360)
            for (dim in targetDims) {
                if (max(workingBitmap.width, workingBitmap.height) > dim) {
                    val resized = downscaleBitmapIfNeeded(workingBitmap, dim)
                    if (resized !== workingBitmap) {
                        workingBitmap.recycle()
                        workingBitmap = resized
                    }
                }
                val encoded = compressBitmapWithinLimit(workingBitmap, MAX_COLLEGE_IMAGE_DATA_URI_CHARS)
                if (encoded != null) {
                    return@runCatching encoded
                }
            }
            error("ছবিটির সাইজ অনেক বড়। অনুগ্রহ করে তুলনামূলক ছোট ছবি দিন।")
        } finally {
            if (!workingBitmap.isRecycled) {
                workingBitmap.recycle()
            }
        }
    }
}

private fun decodeUriToBitmap(context: Context, uri: Uri): Bitmap? {
    val resolver = context.contentResolver

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val decodedWithImageDecoder = runCatching {
            val source = ImageDecoder.createSource(resolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val rawW = info.size.width
                val rawH = info.size.height
                if (rawW > 0 && rawH > 0) {
                    val maxSide = max(rawW, rawH)
                    if (maxSide > 1080) {
                        val scale = 1080f / maxSide.toFloat()
                        decoder.setTargetSize(
                            (rawW * scale).roundToInt().coerceAtLeast(1),
                            (rawH * scale).roundToInt().coerceAtLeast(1)
                        )
                    }
                }
            }
        }.getOrNull()
        if (decodedWithImageDecoder != null) {
            return decodedWithImageDecoder
        }
    }

    val bytes = readAllUriBytes(context, uri) ?: return null

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val decodedFromBytes = runCatching {
            val source = ImageDecoder.createSource(java.nio.ByteBuffer.wrap(bytes))
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val rawW = info.size.width
                val rawH = info.size.height
                if (rawW > 0 && rawH > 0) {
                    val maxSide = max(rawW, rawH)
                    if (maxSide > 1080) {
                        val scale = 1080f / maxSide.toFloat()
                        decoder.setTargetSize(
                            (rawW * scale).roundToInt().coerceAtLeast(1),
                            (rawH * scale).roundToInt().coerceAtLeast(1)
                        )
                    }
                }
            }
        }.getOrNull()
        if (decodedFromBytes != null) {
            return decodedFromBytes
        }
    }

    val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, boundsOptions)
    val rawW = boundsOptions.outWidth
    val rawH = boundsOptions.outHeight
    if (rawW <= 0 || rawH <= 0) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    val targetMaxDim = 960
    var sampleSize = 1
    while ((rawW / sampleSize) > targetMaxDim * 2 || (rawH / sampleSize) > targetMaxDim * 2) {
        sampleSize *= 2
    }
    val decodeOptions = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)
}

private fun readAllUriBytes(context: Context, uri: Uri): ByteArray? {
    val resolver = context.contentResolver
    runCatching {
        resolver.openInputStream(uri)?.use { stream ->
            val data = stream.readBytes()
            if (data.isNotEmpty()) return data
        }
    }
    runCatching {
        resolver.openAssetFileDescriptor(uri, "r")?.use { afd ->
            afd.createInputStream().use { stream ->
                val data = stream.readBytes()
                if (data.isNotEmpty()) return data
            }
        }
    }
    runCatching {
        resolver.openTypedAssetFileDescriptor(uri, "image/*", null)?.use { afd ->
            afd.createInputStream().use { stream ->
                val data = stream.readBytes()
                if (data.isNotEmpty()) return data
            }
        }
    }
    runCatching {
        resolver.openFileDescriptor(uri, "r")?.use { pfd ->
            java.io.FileInputStream(pfd.fileDescriptor).use { stream ->
                val data = stream.readBytes()
                if (data.isNotEmpty()) return data
            }
        }
    }
    return null
}

private fun downscaleBitmapIfNeeded(bitmap: Bitmap, maxDimension: Int): Bitmap {
    val maxSide = max(bitmap.width, bitmap.height)
    if (maxSide <= maxDimension) return bitmap
    val scale = maxDimension.toFloat() / maxSide.toFloat()
    val targetW = (bitmap.width * scale).roundToInt().coerceAtLeast(1)
    val targetH = (bitmap.height * scale).roundToInt().coerceAtLeast(1)
    return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
}

private fun compressBitmapWithinLimit(bitmap: Bitmap, maxChars: Int): String? {
    val qualities = intArrayOf(82, 72, 60, 48, 36, 25)
    val stream = ByteArrayOutputStream()
    for (quality in qualities) {
        stream.reset()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        val encoded = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
        val dataUri = "data:image/jpeg;base64,$encoded"
        if (dataUri.length <= maxChars) {
            return dataUri
        }
    }
    return null
}

@Composable
fun CollegeImage(
    imageSource: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val trimmed = imageSource.trim()
    if (trimmed.isEmpty()) return

    if (trimmed.startsWith("data:image/", ignoreCase = true)) {
        val decodedImage by produceState<ImageBitmap?>(initialValue = null, key1 = trimmed) {
            value = withContext(Dispatchers.Default) {
                runCatching {
                    val commaIndex = trimmed.indexOf(',')
                    if (commaIndex <= 0) return@runCatching null
                    val base64Payload = trimmed.substring(commaIndex + 1)
                    val bytes = Base64.decode(base64Payload, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                }.getOrNull()
            }
        }
        val bmp = decodedImage
        if (bmp != null) {
            Image(
                bitmap = bmp,
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier
            )
        } else {
            Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant))
        }
    } else {
        AsyncImage(
            model = trimmed,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    }
}
