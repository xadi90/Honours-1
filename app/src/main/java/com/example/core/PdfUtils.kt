package com.example.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.data.model.StructuredAiAnswer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

sealed interface PdfDownloadState {
    data object Idle : PdfDownloadState
    data class Downloading(val progressPercent: Int, val downloadedBytes: Long, val totalBytes: Long) : PdfDownloadState
    data class Completed(val file: File) : PdfDownloadState
    data class Failed(val message: String) : PdfDownloadState
}

object PdfUtils {

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(25, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    fun getPdfDirectory(context: Context): File {
        val dir = File(context.filesDir, "pdfs")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getExportDirectory(context: Context): File {
        val dir = File(context.filesDir, "exports")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getOfflinePdfFile(context: Context, pdfId: String, localFileName: String = ""): File {
        val safeName = if (localFileName.isNotBlank()) localFileName else "${pdfId}.pdf"
        return File(getPdfDirectory(context), safeName)
    }

    fun getShareUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    suspend fun getPdfPageCount(file: File): Int = withContext(Dispatchers.IO) {
        if (!file.exists() || file.length() == 0L) return@withContext 0
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        try {
            pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd)
            renderer.pageCount
        } catch (_: Exception) {
            0
        } finally {
            try { renderer?.close() } catch (_: Exception) {}
            try { pfd?.close() } catch (_: Exception) {}
        }
    }

    suspend fun renderPdfPage(file: File, pageIndex: Int, zoomScale: Float = 1.5f): Bitmap? =
        withContext(Dispatchers.IO) {
            if (!file.exists() || file.length() == 0L) return@withContext null
            var pfd: ParcelFileDescriptor? = null
            var renderer: PdfRenderer? = null
            var page: PdfRenderer.Page? = null
            try {
                pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                renderer = PdfRenderer(pfd)
                if (pageIndex < 0 || pageIndex >= renderer.pageCount) return@withContext null
                page = renderer.openPage(pageIndex)
                val safeScale = zoomScale.coerceIn(1.0f, 3.0f)
                val width = (page.width * safeScale).toInt().coerceIn(300, 2400)
                val height = (page.height * safeScale).toInt().coerceIn(400, 3400)
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmap
            } catch (_: Exception) {
                null
            } finally {
                try { page?.close() } catch (_: Exception) {}
                try { renderer?.close() } catch (_: Exception) {}
                try { pfd?.close() } catch (_: Exception) {}
            }
        }

    suspend fun extractPdfPagesAsBitmaps(
        context: Context,
        uri: Uri,
        maxPages: Int = 2
    ): List<Bitmap> = withContext(Dispatchers.IO) {
        val tempFile = File(context.cacheDir, "ai_question_temp_${System.currentTimeMillis()}.pdf")
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext emptyList()

            val count = min(maxPages, getPdfPageCount(tempFile))
            val result = mutableListOf<Bitmap>()
            for (i in 0 until count) {
                renderPdfPage(tempFile, i, zoomScale = 1.6f)?.let { result.add(it) }
            }
            result
        } catch (_: Exception) {
            emptyList()
        } finally {
            tempFile.delete()
        }
    }

    suspend fun decodeScaledBitmap(context: Context, uri: Uri, maxDimension: Int = 1600): Bitmap? =
        withContext(Dispatchers.IO) {
            try {
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, options)
                }
                val longest = max(options.outWidth, options.outHeight)
                var sampleSize = 1
                while (longest / sampleSize > maxDimension) {
                    sampleSize *= 2
                }
                val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, decodeOpts)
                }
            } catch (_: Exception) {
                null
            }
        }

    suspend fun copyUriToPdfFile(context: Context, uri: Uri, destFileName: String): Pair<File, Int> =
        withContext(Dispatchers.IO) {
            val destFile = File(getPdfDirectory(context), destFileName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            } ?: throw IllegalStateException("Unable to read selected PDF file.")
            val pages = getPdfPageCount(destFile)
            destFile to pages
        }

    fun downloadPdfWithProgress(url: String, destination: File): Flow<PdfDownloadState> = flow {
        emit(PdfDownloadState.Downloading(0, 0L, 0L))
        val request = Request.Builder().url(url).build()
        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            emit(PdfDownloadState.Failed("Download failed (HTTP ${response.code})"))
            return@flow
        }
        val body = response.body ?: run {
            emit(PdfDownloadState.Failed("Empty PDF response"))
            return@flow
        }
        val totalBytes = body.contentLength()
        val tempFile = File(destination.parentFile, "${destination.name}.part")
        try {
            body.byteStream().use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(8192)
                    var downloaded = 0L
                    var read: Int
                    var lastPercent = -1
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloaded += read
                        val percent = if (totalBytes > 0) ((downloaded * 100) / totalBytes).toInt() else 50
                        if (percent != lastPercent) {
                            lastPercent = percent
                            emit(PdfDownloadState.Downloading(percent.coerceIn(0, 100), downloaded, totalBytes))
                        }
                    }
                    output.flush()
                }
            }
            if (destination.exists()) destination.delete()
            tempFile.renameTo(destination)
            emit(PdfDownloadState.Completed(destination))
        } catch (e: Exception) {
            tempFile.delete()
            emit(PdfDownloadState.Failed(e.localizedMessage ?: "Download interrupted"))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun exportAnswerToPdf(context: Context, answer: StructuredAiAnswer): File =
        withContext(Dispatchers.IO) {
            val content = buildString {
                appendLine("Honours Solution — AI Academic Answer")
                appendLine("Subject: ${answer.subjectName} | ${answer.yearLabel} | ${answer.answerLength}")
                appendLine()
                appendLine("প্রশ্ন / Question:")
                appendLine(answer.prashno)
                appendLine()
                appendLine("ভূমিকা / Introduction:")
                appendLine(answer.bhumika)
                appendLine()
                appendLine("মূল আলোচনা / Main Discussion:")
                appendLine(answer.mulAlochona)
                if (answer.importantPoints.isNotEmpty()) {
                    appendLine()
                    appendLine("গুরুত্বপূর্ণ পয়েন্ট / Key Points:")
                    answer.importantPoints.forEachIndexed { index, pt ->
                        appendLine("${index + 1}. $pt")
                    }
                }
                if (answer.examples.isNotBlank()) {
                    appendLine()
                    appendLine("প্রয়োজনীয় উদাহরণ / Examples:")
                    appendLine(answer.examples)
                }
                appendLine()
                appendLine("উপসংহার / Conclusion:")
                appendLine(answer.uposhonghar)
                if (answer.sourceNote.isNotBlank()) {
                    appendLine()
                    appendLine("Note: ${answer.sourceNote}")
                }
            }
            val safeId = answer.id.ifBlank { System.currentTimeMillis().toString() }
            val outFile = File(getExportDirectory(context), "honours_answer_$safeId.pdf")
            writeFormattedPdf(
                outFile = outFile,
                headerTitle = "Honours Solution • National University",
                subHeader = "${answer.subjectName} (${answer.yearLabel})",
                bodyText = content
            )
            outFile
        }

    suspend fun createStudyGuidePdf(
        context: Context,
        fileName: String,
        title: String,
        subtitle: String,
        body: String
    ): File = withContext(Dispatchers.IO) {
        val outFile = File(getPdfDirectory(context), fileName)
        writeFormattedPdf(
            outFile = outFile,
            headerTitle = title,
            subHeader = subtitle,
            bodyText = body
        )
        outFile
    }

    private fun writeFormattedPdf(
        outFile: File,
        headerTitle: String,
        subHeader: String,
        bodyText: String
    ) {
        val pageWidth = 595
        val pageHeight = 842
        val margin = 44f
        val contentWidth = (pageWidth - margin * 2).toInt()

        val headerBgPaint = Paint().apply { color = Color.rgb(15, 39, 68) }
        val headerTextPaint = TextPaint().apply {
            color = Color.WHITE
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val subHeaderPaint = TextPaint().apply {
            color = Color.rgb(253, 230, 138)
            textSize = 11f
            isAntiAlias = true
        }
        val bodyPaint = TextPaint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 11.5f
            isAntiAlias = true
        }
        val footerPaint = TextPaint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 9.5f
            isAntiAlias = true
        }

        val document = PdfDocument()
        val layout = StaticLayout.Builder
            .obtain(bodyText, 0, bodyText.length, bodyPaint, contentWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(4f, 1.15f)
            .build()

        var currentLine = 0
        var pageNumber = 1
        while (currentLine < layout.lineCount) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            val page = document.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val topOffset = if (pageNumber == 1) {
                canvas.drawRect(0f, 0f, pageWidth.toFloat(), 84f, headerBgPaint)
                canvas.drawText(headerTitle.take(65), margin, 36f, headerTextPaint)
                canvas.drawText(subHeader.take(80), margin, 60f, subHeaderPaint)
                106f
            } else {
                48f
            }

            val maxContentHeight = pageHeight - topOffset - 46f
            var yCursor = topOffset

            while (currentLine < layout.lineCount) {
                val lineHeight = (layout.getLineBottom(currentLine) - layout.getLineTop(currentLine)).toFloat()
                if (yCursor + lineHeight > topOffset + maxContentHeight) break
                val start = layout.getLineStart(currentLine)
                val end = layout.getLineEnd(currentLine)
                val lineStr = bodyText.substring(start, end).trimEnd('\n', '\r')
                canvas.drawText(lineStr, margin, yCursor - layout.getLineAscent(currentLine), bodyPaint)
                yCursor += lineHeight
                currentLine++
            }

            canvas.drawText(
                "Honours Solution • Page $pageNumber",
                margin,
                pageHeight - 24f,
                footerPaint
            )
            document.finishPage(page)
            pageNumber++
        }

        FileOutputStream(outFile).use { fos ->
            document.writeTo(fos)
        }
        document.close()
    }
}
