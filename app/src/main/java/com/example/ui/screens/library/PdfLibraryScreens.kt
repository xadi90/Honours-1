package com.example.ui.screens.library

import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.PdfDownloadState
import com.example.core.PdfUtils
import com.example.data.catalog.AcademicSeedCatalog
import com.example.data.model.Department
import com.example.data.model.PdfResource
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ErrorStateCard
import com.example.ui.components.HonoursTopBar
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun AllDepartmentsPdfLibraryScreen(
    departments: List<Department>,
    pdfs: List<PdfResource>,
    onDepartmentClick: (String) -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenPdfReader: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredDepts = remember(departments, searchQuery) {
        if (searchQuery.isBlank()) departments
        else departments.filter {
            it.nameBn.contains(searchQuery, ignoreCase = true) ||
                it.nameEn.contains(searchQuery, ignoreCase = true) ||
                it.code.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        HonoursTopBar(
            title = "সকল বিভাগের PDF লাইব্রেরি",
            subtitle = "${departments.size} Departments • ${pdfs.size} Resources",
            actions = {
                IconButton(onClick = onOpenDownloads, modifier = Modifier.testTag("library_downloads_button")) {
                    Icon(Icons.Default.Download, contentDescription = "Download Manager")
                }
            }
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("বিভাগ খুঁজুন (যেমন: রাষ্ট্রবিজ্ঞান, বাংলা, English, Economics)...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("pdf_library_dept_search")
                )
            }

            if (pdfs.isNotEmpty() && searchQuery.isBlank()) {
                item {
                    Text("Recent PDFs", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(pdfs.take(5), key = { it.id }) { pdf ->
                            Card(
                                onClick = { onOpenPdfReader(pdf.id) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                modifier = Modifier.width(220.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(pdf.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    Text(pdf.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(pdf.departmentName, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text("Select Honours Department", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            items(filteredDepts, key = { it.id }) { dept ->
                val count = pdfs.count { it.departmentId == dept.id }
                Card(
                    onClick = { onDepartmentClick(dept.id) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dept_card_${dept.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = if (dept.hasStructuredSolution) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = dept.code.ifBlank { dept.nameEn.take(3).uppercase() },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (dept.hasStructuredSolution) Color.White else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(dept.nameBn, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(dept.nameEn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "$count PDF",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DepartmentPdfScreen(
    department: Department?,
    pdfs: List<PdfResource>,
    downloadStates: Map<String, PdfDownloadState>,
    isDownloadedCheck: (PdfResource) -> Boolean,
    offlineRefreshTick: Int,
    onBackClick: () -> Unit,
    onOpenPdfReader: (String) -> Unit,
    onDownloadPdf: (PdfResource) -> Unit,
    onCancelDownload: (String) -> Unit
) {
    var selectedYear by remember { mutableIntStateOf(0) } // 0 = All Years
    var selectedCategory by remember { mutableStateOf("All") }

    val deptPdfs = remember(pdfs, department?.id, selectedYear, selectedCategory) {
        pdfs.filter { pdf ->
            (department == null || pdf.departmentId == department.id) &&
                (selectedYear == 0 || pdf.yearNumber == selectedYear.toLong()) &&
                (selectedCategory == "All" || pdf.category.equals(selectedCategory, ignoreCase = true))
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        HonoursTopBar(
            title = department?.nameBn ?: "Department PDFs",
            subtitle = department?.nameEn ?: "PDF Library",
            onBackClick = onBackClick
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(selected = selectedYear == 0, onClick = { selectedYear = 0 }, label = { Text("All Years") })
                }
                items(4) { idx ->
                    val yr = idx + 1
                    FilterChip(selected = selectedYear == yr, onClick = { selectedYear = yr }, label = { Text("Year $yr") })
                }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(selected = selectedCategory == "All", onClick = { selectedCategory = "All" }, label = { Text("All Categories") })
                }
                items(AcademicSeedCatalog.pdfCategories) { cat ->
                    FilterChip(selected = selectedCategory == cat, onClick = { selectedCategory = cat }, label = { Text(cat) })
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (deptPdfs.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.PictureAsPdf,
                        title = "No PDFs in this filter yet",
                        message = "Administrators can upload PDF books, class notes, lecture sheets, and previous-year papers for ${department?.nameBn ?: "this department"} from Content Management."
                    )
                }
            } else {
                items(deptPdfs, key = { it.id }) { pdf ->
                    val dlState = downloadStates[pdf.id]
                    val isOffline = remember(pdf.id, offlineRefreshTick, dlState) { isDownloadedCheck(pdf) }

                    Card(
                        onClick = { onOpenPdfReader(pdf.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(pdf.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "Year ${pdf.yearNumber} • ${pdf.subjectName} • ${pdf.category}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isOffline) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Available offline",
                                        tint = Color(0xFF059669),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            if (pdf.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(pdf.description, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                            if (dlState is PdfDownloadState.Downloading) {
                                Spacer(modifier = Modifier.height(10.dp))
                                LinearProgressIndicator(
                                    progress = { dlState.progressPercent / 100f },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Downloading ${dlState.progressPercent}%", style = MaterialTheme.typography.labelSmall)
                                    IconButton(onClick = { onCancelDownload(pdf.id) }) {
                                        Icon(Icons.Default.Cancel, contentDescription = "Cancel download")
                                    }
                                }
                            } else if (dlState is PdfDownloadState.Failed) {
                                Spacer(modifier = Modifier.height(8.dp))
                                ErrorStateCard(message = dlState.message, onRetry = { onDownloadPdf(pdf) })
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { onOpenPdfReader(pdf.id) }, modifier = Modifier.weight(1f)) {
                                    Text("Read PDF")
                                }
                                if (!isOffline) {
                                    OutlinedButton(onClick = { onDownloadPdf(pdf) }, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Save Offline")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PdfReaderScreen(
    pdf: PdfResource?,
    isBookmarked: Boolean,
    ensureLocalPdf: suspend (PdfResource) -> Result<File>,
    getInitialPage: suspend (String) -> Int,
    onSavePageProgress: (String, Int) -> Unit,
    onRecordRecentPdf: (String) -> Unit,
    onTogglePageBookmark: (PdfResource, Int) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var localFile by remember { mutableStateOf<File?>(null) }
    var pageCount by remember { mutableIntStateOf(0) }
    var currentPage by remember { mutableIntStateOf(0) }
    var renderedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var zoomScale by remember { mutableFloatStateOf(1.4f) }
    var gestureScale by remember { mutableFloatStateOf(1f) }
    var isFullScreen by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var showSearchBar by remember { mutableStateOf(false) }
    var searchInDoc by remember { mutableStateOf("") }
    var retryTick by remember { mutableIntStateOf(0) }

    LaunchedEffect(pdf?.id, retryTick) {
        if (pdf == null) {
            errorMsg = "PDF not found."
            isLoading = false
            return@LaunchedEffect
        }
        isLoading = true
        errorMsg = null
        onRecordRecentPdf(pdf.id)
        val result = ensureLocalPdf(pdf)
        result.onSuccess { file ->
            localFile = file
            val total = PdfUtils.getPdfPageCount(file)
            pageCount = total
            val saved = getInitialPage(pdf.id).coerceIn(0, (total - 1).coerceAtLeast(0))
            currentPage = saved
        }.onFailure { err ->
            errorMsg = err.localizedMessage ?: "Unable to load PDF file."
            isLoading = false
        }
    }

    LaunchedEffect(localFile, currentPage, zoomScale) {
        val file = localFile ?: return@LaunchedEffect
        isLoading = true
        val bmp = PdfUtils.renderPdfPage(file, currentPage, zoomScale)
        renderedBitmap = bmp
        isLoading = false
        if (pdf != null) {
            onSavePageProgress(pdf.id, currentPage)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (!isFullScreen) {
            HonoursTopBar(
                title = pdf?.title ?: "PDF Reader",
                subtitle = if (pageCount > 0) "Page ${currentPage + 1} of $pageCount" else null,
                onBackClick = onBackClick,
                actions = {
                    if (pdf != null) {
                        IconButton(onClick = { showSearchBar = !showSearchBar }) {
                            Icon(Icons.Default.Search, contentDescription = "Search document details")
                        }
                        IconButton(onClick = { onTogglePageBookmark(pdf, currentPage + 1) }) {
                            Icon(
                                if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark page"
                            )
                        }
                        IconButton(onClick = {
                            localFile?.let { file ->
                                runCatching {
                                    val uri = PdfUtils.getShareUri(context, file)
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share PDF"))
                                }.onFailure {
                                    Toast.makeText(context, "No app found to share PDF.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share PDF")
                        }
                        IconButton(onClick = { isFullScreen = true }) {
                            Icon(Icons.Default.Fullscreen, contentDescription = "Enter fullscreen")
                        }
                    }
                }
            )
        }

        if (showSearchBar && !isFullScreen && pdf != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchInDoc,
                    onValueChange = { searchInDoc = it },
                    placeholder = { Text("Jump to page # or search metadata...") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    val pageNum = searchInDoc.trim().toIntOrNull()
                    if (pageNum != null && pageNum in 1..pageCount) {
                        currentPage = pageNum - 1
                    } else {
                        val match = listOf(pdf.title, pdf.subjectName, pdf.description, pdf.category)
                            .any { it.contains(searchInDoc, ignoreCase = true) }
                        Toast.makeText(
                            context,
                            if (match) "Found in document metadata/index" else "No match (enter page 1..$pageCount)",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }) { Text("Go") }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF1E293B))
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        gestureScale = (gestureScale * zoom).coerceIn(1f, 3f)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            val currentError = errorMsg
            val currentBitmap = renderedBitmap
            when {
                currentError != null -> {
                    ErrorStateCard(
                        message = currentError,
                        onRetry = { retryTick++ },
                        modifier = Modifier.padding(16.dp)
                    )
                }
                isLoading && currentBitmap == null -> {
                    CircularProgressIndicator(color = Color.White)
                }
                currentBitmap != null -> {
                    Image(
                        bitmap = currentBitmap.asImageBitmap(),
                        contentDescription = "PDF Page ${currentPage + 1}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = gestureScale,
                                scaleY = gestureScale
                            )
                            .padding(8.dp)
                    )
                }
            }

            if (isFullScreen) {
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    IconButton(onClick = { isFullScreen = false }) {
                        Icon(Icons.Default.FullscreenExit, contentDescription = "Exit fullscreen", tint = Color.White)
                    }
                }
            }
        }

        if (!isFullScreen) {
            Surface(tonalElevation = 4.dp, color = MaterialTheme.colorScheme.surface) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (currentPage > 0) currentPage-- },
                            enabled = currentPage > 0
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous page")
                        }
                        Text(
                            text = if (pageCount > 0) "${currentPage + 1} / $pageCount" else "0 / 0",
                            style = MaterialTheme.typography.labelLarge
                        )
                        IconButton(
                            onClick = { if (currentPage < pageCount - 1) currentPage++ },
                            enabled = currentPage < pageCount - 1
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next page")
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            zoomScale = (zoomScale - 0.3f).coerceAtLeast(1.0f)
                            gestureScale = 1f
                        }) {
                            Icon(Icons.Default.ZoomOut, contentDescription = "Zoom out")
                        }
                        IconButton(onClick = {
                            zoomScale = (zoomScale + 0.3f).coerceAtMost(2.8f)
                            gestureScale = 1f
                        }) {
                            Icon(Icons.Default.ZoomIn, contentDescription = "Zoom in")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadManagerScreen(
    pdfs: List<PdfResource>,
    downloadStates: Map<String, PdfDownloadState>,
    isDownloadedCheck: (PdfResource) -> Boolean,
    offlineRefreshTick: Int,
    onBackClick: () -> Unit,
    onOpenPdf: (String) -> Unit,
    onRetryDownload: (PdfResource) -> Unit,
    onCancelDownload: (String) -> Unit,
    onDeleteOfflinePdf: (PdfResource) -> Unit
) {
    val context = LocalContext.current
    val offlinePdfs = remember(pdfs, offlineRefreshTick, downloadStates) {
        pdfs.filter { isDownloadedCheck(it) }
    }
    val activeOrFailed = remember(pdfs, downloadStates) {
        pdfs.filter {
            val st = downloadStates[it.id]
            st is PdfDownloadState.Downloading || st is PdfDownloadState.Failed
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        HonoursTopBar(
            title = "Download Manager",
            subtitle = "${offlinePdfs.size} Offline PDFs Available",
            onBackClick = onBackClick
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (activeOrFailed.isNotEmpty()) {
                item {
                    Text("Active & Failed Downloads", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                items(activeOrFailed, key = { "dl_${it.id}" }) { pdf ->
                    val state = downloadStates[pdf.id]
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(pdf.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            if (state is PdfDownloadState.Downloading) {
                                LinearProgressIndicator(progress = { state.progressPercent / 100f }, modifier = Modifier.fillMaxWidth())
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${state.progressPercent}%", style = MaterialTheme.typography.labelSmall)
                                    IconButton(onClick = { onCancelDownload(pdf.id) }) {
                                        Icon(Icons.Default.Cancel, contentDescription = "Cancel")
                                    }
                                }
                            } else if (state is PdfDownloadState.Failed) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(state.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                                    IconButton(onClick = { onRetryDownload(pdf) }) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Retry download")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text("Offline Saved PDFs (${offlinePdfs.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }

            if (offlinePdfs.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.FolderOpen,
                        title = "No offline PDFs saved yet",
                        message = "Open any PDF from the Library and tap 'Save Offline' to read without internet."
                    )
                }
            } else {
                items(offlinePdfs, key = { "off_${it.id}" }) { pdf ->
                    val fileSizeKb = remember(pdf.id, offlineRefreshTick) {
                        PdfUtils.getOfflinePdfFile(context, pdf.id, pdf.localFileName).length() / 1024L
                    }
                    Card(
                        onClick = { onOpenPdf(pdf.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(pdf.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text("${pdf.departmentName} • ${fileSizeKb} KB", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { onDeleteOfflinePdf(pdf) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete offline copy")
                            }
                        }
                    }
                }
            }
        }
    }
}
