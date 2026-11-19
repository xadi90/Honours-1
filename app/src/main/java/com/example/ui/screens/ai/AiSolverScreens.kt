package com.example.ui.screens.ai

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.core.PdfUtils
import com.example.data.catalog.AcademicSeedCatalog
import com.example.data.model.AiConversation
import com.example.data.model.AiMessage
import com.example.data.model.AiQuotaState
import com.example.data.model.AiRemoteConfig
import com.example.data.model.AiTutorMode
import com.example.data.model.AnswerLength
import com.example.data.model.StructuredAiAnswer
import com.example.data.model.Subject
import com.example.data.model.UiState
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ErrorStateCard
import com.example.ui.components.HonoursTopBar
import com.example.ui.components.LoadingStateBox
import com.example.ui.components.ReportIssueDialog
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun AiQuestionSolverScreen(
    subjects: List<Subject>,
    savedAnswersState: UiState<List<StructuredAiAnswer>>,
    solveState: UiState<StructuredAiAnswer>,
    attachedImages: List<Bitmap>,
    remoteConfig: AiRemoteConfig,
    isOnline: Boolean,
    initialPrompt: String = "",
    onAttachImages: (List<Bitmap>) -> Unit,
    onClearImages: () -> Unit,
    onSolveQuestion: (
        question: String,
        yearLabel: String,
        subjectName: String,
        answerLength: AnswerLength,
        language: String
    ) -> Unit,
    onOpenAnswerDetails: (StructuredAiAnswer) -> Unit,
    onDeleteSavedAnswer: (String) -> Unit,
    onOpenAiChat: () -> Unit,
    onOpenImageUploadScreen: () -> Unit,
    aiQuotaState: AiQuotaState = AiQuotaState(),
    onOpenBuyLimit: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var questionText by remember(initialPrompt) { mutableStateOf(initialPrompt) }
    var selectedYear by remember { mutableIntStateOf(1) }
    var selectedLength by remember { mutableStateOf(AnswerLength.EXAM_READY) }
    var selectedLanguage by remember { mutableStateOf("bn") }

    val yearSubjects = remember(subjects, selectedYear) {
        subjects.filter { it.yearNumber == selectedYear.toLong() }
    }
    var selectedSubjectName by remember(selectedYear, yearSubjects) {
        mutableStateOf(yearSubjects.firstOrNull()?.titleBn ?: "রাষ্ট্রবিজ্ঞান (সাধারণ)")
    }

    var cameraFileUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val bmp = PdfUtils.decodeScaledBitmap(context, uri)
                if (bmp != null) onAttachImages(attachedImages + bmp)
            }
        }
    }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val bitmaps = PdfUtils.extractPdfPagesAsBitmaps(context, uri, maxPages = 2)
                if (bitmaps.isNotEmpty()) {
                    onAttachImages(attachedImages + bitmaps)
                } else {
                    Toast.makeText(context, "Could not render pages from selected PDF.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = cameraFileUri
        if (success && uri != null) {
            scope.launch {
                val bmp = PdfUtils.decodeScaledBitmap(context, uri)
                if (bmp != null) onAttachImages(attachedImages + bmp)
            }
        }
    }

    val launchCameraCapture = {
        val dir = File(context.cacheDir, "camera").apply { mkdirs() }
        val file = File(dir, "q_capture_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        cameraFileUri = uri
        takePictureLauncher.launch(uri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCameraCapture()
        } else {
            Toast.makeText(context, "Camera permission is required to capture question photos.", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AI প্রশ্ন সমাধান",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Powered by Firebase AI Logic (${remoteConfig.modelName})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(
                    onClick = onOpenAiChat,
                    modifier = Modifier.testTag("open_ai_chat_button")
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI Tutor Chat")
                }
            }
        }

        item {
            AiQuotaBannerCard(
                quota = aiQuotaState,
                onBuyLimitClick = onOpenBuyLimit
            )
        }

        if (!isOnline) {
            item {
                ErrorStateCard(
                    message = "AI Question Solver requires an active internet connection. Your saved answers remain accessible offline below."
                )
            }
        }

        // Year & Subject Selector
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("১. বর্ষ ও বিষয় নির্বাচন করুন", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(AcademicSeedCatalog.honoursYears) { (yr, _) ->
                            FilterChip(
                                selected = selectedYear == yr,
                                onClick = { selectedYear = yr },
                                label = { Text("${yr}ম/য় বর্ষ") }
                            )
                        }
                    }

                    if (yearSubjects.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(yearSubjects) { subj ->
                                FilterChip(
                                    selected = selectedSubjectName == subj.titleBn,
                                    onClick = { selectedSubjectName = subj.titleBn },
                                    label = { Text(subj.titleBn, maxLines = 1) }
                                )
                            }
                        }
                    }

                    Text("২. উত্তরের ধরন ও ভাষা", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(AnswerLength.entries) { len ->
                            FilterChip(
                                selected = selectedLength == len,
                                onClick = { selectedLength = len },
                                label = { Text(len.labelBn) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedLanguage == "bn",
                            onClick = { selectedLanguage = "bn" },
                            label = { Text("বাংলা উত্তর") }
                        )
                        FilterChip(
                            selected = selectedLanguage == "en",
                            onClick = { selectedLanguage = "en" },
                            label = { Text("English Answer") }
                        )
                    }
                }
            }
        }

        // Question Input & Multimodal Tools
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = questionText,
                        onValueChange = { questionText = it },
                        label = { Text("আপনার প্রশ্ন লিখুন (বাংলা বা English)") },
                        placeholder = { Text("যেমন: জন অস্টিনের সার্বভৌমত্ব তত্ত্ব সমালোচনাসহ আলোচনা করো...") },
                        minLines = 3,
                        maxLines = 6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_question_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f).testTag("pick_question_image_button")
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Image", maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = {
                                val granted = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                                if (granted) launchCameraCapture()
                                else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f).testTag("capture_question_photo_button")
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Camera", maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f).testTag("pick_question_pdf_button")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF", maxLines = 1)
                        }
                    }

                    TextButton(
                        onClick = onOpenImageUploadScreen,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Open Full Question Image / PDF Studio →")
                    }

                    if (attachedImages.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Attached (${attachedImages.size} page/image)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            TextButton(onClick = onClearImages) { Text("Clear") }
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(attachedImages) { bmp ->
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Attached question preview",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(84.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val yearLabel = AcademicSeedCatalog.honoursYears
                                .firstOrNull { it.first == selectedYear }?.second ?: "অনার্স $selectedYear বর্ষ"
                            onSolveQuestion(
                                questionText,
                                yearLabel,
                                selectedSubjectName,
                                selectedLength,
                                selectedLanguage
                            )
                        },
                        enabled = solveState !is UiState.Loading && isOnline,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("solve_question_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (solveState is UiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("উত্তর তৈরি হচ্ছে...")
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("উত্তর তৈরি করুন (Solve with AI)")
                        }
                    }
                }
            }
        }

        if (solveState is UiState.Error) {
            item {
                ErrorStateCard(message = solveState.message)
            }
        }

        // Saved AI Answers History
        item {
            Text(
                text = "Saved AI Answers",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        when (savedAnswersState) {
            is UiState.Loading -> item { LoadingStateBox("Loading saved answers...") }
            is UiState.Error -> item { ErrorStateCard(savedAnswersState.message) }
            is UiState.Success -> {
                val list = savedAnswersState.data
                if (list.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = Icons.Default.AutoAwesome,
                            title = "No saved AI answers yet",
                            message = "Ask any Political Science or Honours question above—your structured answers will automatically save here."
                        )
                    }
                } else {
                    items(list, key = { it.id }) { ans ->
                        Card(
                            onClick = { onOpenAnswerDetails(ans) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${ans.subjectName} • ${ans.answerLength}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = ans.prashno.ifBlank { ans.question },
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                IconButton(onClick = { onDeleteSavedAnswer(ans.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete saved answer")
                                }
                            }
                        }
                    }
                }
            }
            UiState.Idle -> {}
        }
    }
}

@Composable
fun QuestionImageUploadScreen(
    attachedImages: List<Bitmap>,
    solveState: UiState<StructuredAiAnswer>,
    isOnline: Boolean,
    onBackClick: () -> Unit,
    onAttachImages: (List<Bitmap>) -> Unit,
    onClearImages: () -> Unit,
    onSolveMultimodal: (notes: String, yearLabel: String, subjectName: String, answerLength: AnswerLength) -> Unit,
    aiQuotaState: AiQuotaState = AiQuotaState(),
    onOpenBuyLimit: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var extraPrompt by remember { mutableStateOf("") }
    var selectedYear by remember { mutableIntStateOf(1) }
    var selectedLength by remember { mutableStateOf(AnswerLength.EXAM_READY) }
    var cameraFileUri by remember { mutableStateOf<Uri?>(null) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                PdfUtils.decodeScaledBitmap(context, uri)?.let { onAttachImages(attachedImages + it) }
            }
        }
    }
    val pdfPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                val pages = PdfUtils.extractPdfPagesAsBitmaps(context, uri, 2)
                if (pages.isNotEmpty()) onAttachImages(attachedImages + pages)
            }
        }
    }
    val takePic = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val u = cameraFileUri
        if (ok && u != null) {
            scope.launch {
                PdfUtils.decodeScaledBitmap(context, u)?.let { onAttachImages(attachedImages + it) }
            }
        }
    }
    val launchCamera = {
        val dir = File(context.cacheDir, "camera").apply { mkdirs() }
        val file = File(dir, "upload_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        cameraFileUri = uri
        takePic.launch(uri)
    }
    val camPerm = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchCamera()
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        HonoursTopBar(
            title = "Question Image & PDF Studio",
            subtitle = "Multimodal Question Recognition",
            onBackClick = onBackClick
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AiQuotaBannerCard(
                quota = aiQuotaState,
                onBuyLimitClick = onOpenBuyLimit
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gallery")
                }
                Button(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            launchCamera()
                        } else {
                            camPerm.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Camera")
                }
                OutlinedButton(
                    onClick = { pdfPicker.launch(arrayOf("application/pdf")) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PDF")
                }
            }

            if (attachedImages.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.CameraAlt,
                    title = "No question image or PDF selected",
                    message = "Upload a photo of a question paper or select a PDF document to extract and solve questions with Gemini Vision."
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(attachedImages) { bmp ->
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Question image preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(width = 200.dp, height = 240.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                    }
                }
                TextButton(onClick = onClearImages) { Text("Remove All Attachments") }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(AcademicSeedCatalog.honoursYears) { (yr, _) ->
                    FilterChip(
                        selected = selectedYear == yr,
                        onClick = { selectedYear = yr },
                        label = { Text("Year $yr") }
                    )
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(AnswerLength.entries) { len ->
                    FilterChip(
                        selected = selectedLength == len,
                        onClick = { selectedLength = len },
                        label = { Text(len.labelBn) }
                    )
                }
            }

            OutlinedTextField(
                value = extraPrompt,
                onValueChange = { extraPrompt = it },
                label = { Text("Optional instruction (e.g., 'Solve Question 3(b) in Bengali')") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            if (solveState is UiState.Error) {
                ErrorStateCard(solveState.message)
            }

            Button(
                onClick = {
                    val yrLabel = AcademicSeedCatalog.honoursYears.firstOrNull { it.first == selectedYear }?.second
                        ?: "অনার্স $selectedYear বর্ষ"
                    onSolveMultimodal(extraPrompt, yrLabel, "রাষ্ট্রবিজ্ঞান", selectedLength)
                },
                enabled = attachedImages.isNotEmpty() && solveState !is UiState.Loading && isOnline,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (solveState is UiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analyzing & Solving...")
                } else {
                    Text("Solve Attached Question")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    conversations: List<AiConversation>,
    activeMessages: List<AiMessage>,
    streamingText: String,
    isStreaming: Boolean,
    chatError: String?,
    isOnline: Boolean,
    initialPrompt: String = "",
    onBackClick: () -> Unit,
    onSelectConversation: (String?) -> Unit,
    onDeleteConversation: (String) -> Unit,
    onSendMessage: (prompt: String, mode: AiTutorMode, yearLabel: String, yearNumber: Long, subjectName: String) -> Unit,
    aiQuotaState: AiQuotaState = AiQuotaState(),
    onOpenBuyLimit: () -> Unit = {}
) {
    var inputText by remember(initialPrompt) { mutableStateOf(initialPrompt) }
    var selectedMode by remember { mutableStateOf(AiTutorMode.CHAT) }
    var selectedYear by remember { mutableIntStateOf(1) }
    var showHistorySheet by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(activeMessages.size, streamingText) {
        if (activeMessages.isNotEmpty()) {
            listState.animateScrollToItem(activeMessages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        HonoursTopBar(
            title = "AI Study Assistant",
            subtitle = selectedMode.labelBn,
            onBackClick = onBackClick,
            actions = {
                IconButton(onClick = { onSelectConversation(null) }) {
                    Icon(Icons.Default.Add, contentDescription = "New chat")
                }
                IconButton(onClick = { showHistorySheet = true }) {
                    Icon(Icons.Default.History, contentDescription = "Chat history")
                }
            }
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(AiTutorMode.entries) { mode ->
                FilterChip(
                    selected = selectedMode == mode,
                    onClick = { selectedMode = mode },
                    label = { Text(mode.labelBn) }
                )
            }
        }

        AiQuotaBannerCard(
            quota = aiQuotaState,
            onBuyLimitClick = onOpenBuyLimit,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )

        if (chatError != null) {
            ErrorStateCard(message = chatError, modifier = Modifier.padding(horizontal = 12.dp))
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (activeMessages.isEmpty() && !isStreaming) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.AutoAwesome,
                        title = "Ask Honours Solution AI Tutor",
                        message = "Choose a study mode above (Concept, Summary, Question Gen, MCQ Gen, Evaluate Answer, Study Plan) and start chatting in Bengali or English."
                    )
                }
            }

            items(activeMessages, key = { it.id }) { msg ->
                val isUser = msg.role == "user"
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Surface(
                        color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        contentColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = if (isUser) 0.dp else 2.dp,
                        modifier = Modifier.widthIn(max = 330.dp)
                    ) {
                        Text(
                            text = msg.text,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }

            if (isStreaming) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(16.dp),
                            tonalElevation = 2.dp,
                            modifier = Modifier.widthIn(max = 330.dp)
                        ) {
                            Text(
                                text = streamingText.ifBlank { "Thinking..." },
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }
            }
        }

        Surface(tonalElevation = 4.dp, color = MaterialTheme.colorScheme.surface) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("প্রশ্ন বা টপিক লিখুন...") },
                    maxLines = 4,
                    modifier = Modifier.weight(1f).testTag("ai_chat_input")
                )
                IconButton(
                    onClick = {
                        val yrLabel = AcademicSeedCatalog.honoursYears
                            .firstOrNull { it.first == selectedYear }?.second ?: "অনার্স ১ম বর্ষ"
                        val textToSend = inputText
                        inputText = ""
                        onSendMessage(textToSend, selectedMode, yrLabel, selectedYear.toLong(), "রাষ্ট্রবিজ্ঞান")
                    },
                    enabled = inputText.isNotBlank() && !isStreaming && isOnline,
                    modifier = Modifier.testTag("ai_chat_send_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send message")
                }
            }
        }
    }

    if (showHistorySheet) {
        ModalBottomSheet(onDismissRequest = { showHistorySheet = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Previous AI Conversations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                if (conversations.isEmpty()) {
                    Text("No saved conversations yet.", style = MaterialTheme.typography.bodySmall)
                } else {
                    LazyColumn(modifier = Modifier.height(300.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(conversations, key = { it.id }) { conv ->
                            Card(
                                onClick = {
                                    onSelectConversation(conv.id)
                                    showHistorySheet = false
                                },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(conv.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(conv.lastMessage, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    IconButton(onClick = { onDeleteConversation(conv.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete conversation")
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun AnswerDetailsScreen(
    answer: StructuredAiAnswer?,
    onBackClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    onExportPdf: suspend () -> Result<File>,
    onFollowUpClick: (String) -> Unit,
    onSubmitReport: (targetId: String, reason: String, details: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showReportDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        HonoursTopBar(
            title = "Answer Details",
            subtitle = answer?.let { "${it.subjectName} • ${it.answerLength}" },
            onBackClick = onBackClick,
            actions = {
                if (answer != null) {
                    IconButton(onClick = onToggleBookmark, modifier = Modifier.testTag("answer_bookmark_button")) {
                        Icon(
                            imageVector = if (answer.bookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark answer"
                        )
                    }
                    IconButton(onClick = { showReportDialog = true }, modifier = Modifier.testTag("answer_report_button")) {
                        Icon(Icons.Default.Flag, contentDescription = "Report answer")
                    }
                }
            }
        )

        if (answer == null) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                EmptyStateView(icon = Icons.Default.AutoAwesome, title = "No answer loaded", message = "Generate or select a saved answer first.")
            }
            return
        }

        val fullPlainText = remember(answer) {
            buildString {
                appendLine("প্রশ্ন: ${answer.prashno}")
                appendLine("\nভূমিকা:\n${answer.bhumika}")
                appendLine("\nমূল আলোচনা:\n${answer.mulAlochona}")
                if (answer.importantPoints.isNotEmpty()) {
                    appendLine("\nগুরুত্বপূর্ণ পয়েন্ট:")
                    answer.importantPoints.forEachIndexed { idx, p -> appendLine("${idx + 1}. $p") }
                }
                if (answer.examples.isNotBlank()) {
                    appendLine("\nপ্রয়োজনীয় উদাহরণ:\n${answer.examples}")
                }
                appendLine("\nউপসংহার:\n${answer.uposhonghar}")
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                AnswerSectionCard(title = "প্রশ্ন (Question)", body = answer.prashno, highlight = true)
            }
            item {
                AnswerSectionCard(title = "ভূমিকা (Introduction)", body = answer.bhumika)
            }
            item {
                AnswerSectionCard(title = "মূল আলোচনা (Main Discussion)", body = answer.mulAlochona)
            }
            if (answer.importantPoints.isNotEmpty()) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("গুরুত্বপূর্ণ পয়েন্ট (Key Points)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            answer.importantPoints.forEachIndexed { idx, pt ->
                                Text("${idx + 1}. $pt", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
            if (answer.examples.isNotBlank()) {
                item {
                    AnswerSectionCard(title = "প্রয়োজনীয় উদাহরণ (Examples)", body = answer.examples)
                }
            }
            item {
                AnswerSectionCard(title = "উপসংহার (Conclusion)", body = answer.uposhonghar)
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), shape = RoundedCornerShape(14.dp)) {
                    Text(
                        text = answer.sourceNote,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Actions: Copy, Share, Export PDF, Follow-up
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cb.setPrimaryClip(ClipData.newPlainText("Honours Answer", fullPlainText))
                            Toast.makeText(context, "Answer copied!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy")
                    }
                    OutlinedButton(
                        onClick = {
                            runCatching {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, answer.prashno)
                                    putExtra(Intent.EXTRA_TEXT, fullPlainText)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Answer"))
                            }.onFailure {
                                Toast.makeText(context, "No app available to share.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                onExportPdf().onSuccess { file ->
                                    runCatching {
                                        val shareUri = PdfUtils.getShareUri(context, file)
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/pdf"
                                            putExtra(Intent.EXTRA_STREAM, shareUri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Export Answer PDF"))
                                    }.onFailure {
                                        Toast.makeText(context, "Saved PDF, but no share app found.", Toast.LENGTH_SHORT).show()
                                    }
                                }.onFailure { err ->
                                    Toast.makeText(context, err.localizedMessage ?: "Export failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("export_answer_pdf_button")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export PDF")
                    }
                    OutlinedButton(
                        onClick = { onFollowUpClick("Follow-up on: ${answer.prashno}\n\n") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Follow Up")
                    }
                }
            }
        }
    }

    if (showReportDialog && answer != null) {
        ReportIssueDialog(
            onDismiss = { showReportDialog = false },
            onSubmit = { reason, details ->
                onSubmitReport(answer.id.ifBlank { answer.prashno.take(60) }, reason, details)
                showReportDialog = false
            }
        )
    }
}

@Composable
private fun AnswerSectionCard(title: String, body: String, highlight: Boolean = false) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (highlight) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = body.ifBlank { "—" },
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
