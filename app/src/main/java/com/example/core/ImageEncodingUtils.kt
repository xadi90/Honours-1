package com.example.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
        val resolver = context.contentResolver

        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, boundsOptions)
        } ?: error("ছবিটি পড়া যায়নি।")

        val rawW = boundsOptions.outWidth
        val rawH = boundsOptions.outHeight
        require(rawW > 0 && rawH > 0) { "সঠিক ছবি নির্বাচন করুন।" }

        val targetMaxDim = 960
        var sampleSize = 1
        while ((rawW / sampleSize) > targetMaxDim * 2 || (rawH / sampleSize) > targetMaxDim * 2) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decodedBitmap = resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        } ?: error("ছবিটি লোড করা যায়নি।")

        var workingBitmap = downscaleBitmapIfNeeded(decodedBitmap, targetMaxDim)
        if (workingBitmap !== decodedBitmap) {
            decodedBitmap.recycle()
        }

        try {
            val dataUri = compressBitmapWithinLimit(workingBitmap, MAX_COLLEGE_IMAGE_DATA_URI_CHARS)
            if (dataUri != null) {
                return@runCatching dataUri
            }
            val smallerBitmap = downscaleBitmapIfNeeded(workingBitmap, 640)
            if (smallerBitmap !== workingBitmap) {
                workingBitmap.recycle()
                workingBitmap = smallerBitmap
            }
            compressBitmapWithinLimit(workingBitmap, MAX_COLLEGE_IMAGE_DATA_URI_CHARS)
                ?: error("ছবিটির সাইজ অনেক বড়। অনুগ্রহ করে তুলনামূলক ছোট ছবি দিন।")
        } finally {
            if (!workingBitmap.isRecycled) {
                workingBitmap.recycle()
            }
        }
    }
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
    val qualities = intArrayOf(80, 70, 60, 48, 36)
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
