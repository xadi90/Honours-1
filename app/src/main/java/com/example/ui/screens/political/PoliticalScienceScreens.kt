package com.example.ui.screens.political

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.catalog.AcademicSeedCatalog
import com.example.data.model.Chapter
import com.example.data.model.PdfResource
import com.example.data.model.StudyQuestion
import com.example.data.model.Subject
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HonoursTopBar
import com.example.ui.components.ReportIssueDialog

@Composable
fun PoliticalScienceDepartmentScreen(
    subjects: List<Subject>,
    onBackClick: () -> Unit,
    onYearClick: (Int) -> Unit,
    onAskAiClick: () -> Unit,
    onPdfLibraryClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        HonoursTopBar(
            title = "রাষ্ট্রবিজ্ঞান বিভাগ",
            subtitle = "Political Science • NU Honours 1st–4th Year",
            onBackClick = onBackClick
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_academic),
                    contentDescription = "Political Science Academic Hero",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(20.dp))
                )
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Note: Political Science course codes and syllabi can be customized and verified via Admin Content Management to match the latest National University syllabus.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            items(AcademicSeedCatalog.honoursYears) { (yearNum, titleBn) ->
                val count = subjects.count { it.departmentId == "political_science" && it.yearNumber == yearNum.toLong() }
                Card(
                    onClick = { onYearClick(yearNum) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pol_sci_year_$yearNum")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.size(50.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${yearNum}Y",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(titleBn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = "$count Subjects • Chapters, Notes, Suggestions & Model Tests",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Open Year $yearNum")
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onAskAiClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("AI প্রশ্ন সমাধান")
                    }
                    OutlinedButton(
                        onClick = onPdfLibraryClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pol. Sci. PDFs")
                    }
                }
            }
        }
    }
}

@Composable
fun HonoursYearScreen(
    yearNumber: Int,
    subjects: List<Subject>,
    pdfs: List<PdfResource>,
    questions: List<StudyQuestion>,
    onBackClick: () -> Unit,
    onSubjectClick: (String) -> Unit,
    onPdfClick: (String) -> Unit,
    onPreviousQuestionsClick: () -> Unit,
    onSuggestionsClick: () -> Unit,
    onMcqClick: () -> Unit,
    onAskAiClick: () -> Unit
) {
    val yearTitle = AcademicSeedCatalog.honoursYears.firstOrNull { it.first == yearNumber }?.second
        ?: "রাষ্ট্রবিজ্ঞান – অনার্স $yearNumber বর্ষ"
    val yearSubjects = remember(subjects, yearNumber) {
        subjects.filter { it.yearNumber == yearNumber.toLong() }
    }
    val yearPdfs = remember(pdfs, yearNumber) {
        pdfs.filter { it.departmentId == "political_science" && it.yearNumber == yearNumber.toLong() }
    }
    val yearQuestionsCount = remember(questions, yearNumber) {
        questions.count { it.yearNumber == yearNumber.toLong() }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        HonoursTopBar(
            title = yearTitle,
            subtitle = "National University Honours Year $yearNumber",
            onBackClick = onBackClick
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onPreviousQuestionsClick, modifier = Modifier.weight(1f)) {
                        Text("Prev. Qs ($yearQuestionsCount)", maxLines = 1)
                    }
                    OutlinedButton(onClick = onSuggestionsClick, modifier = Modifier.weight(1f)) {
                        Text("Suggestions", maxLines = 1)
                    }
                    OutlinedButton(onClick = onMcqClick, modifier = Modifier.weight(1f)) {
                        Text("MCQs", maxLines = 1)
                    }
                }
            }

            item {
                Text(
                    text = "Subject-wise Courses (${yearSubjects.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (yearSubjects.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.School,
                        title = "No courses added for Year $yearNumber yet",
                        message = "Administrators can add verified NU Political Science courses from Content Management.",
                        actionLabel = "Ask AI Tutor",
                        onActionClick = onAskAiClick
                    )
                }
            } else {
                items(yearSubjects, key = { it.id }) { subject ->
                    Card(
                        onClick = { onSubjectClick(subject.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("subject_card_${subject.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = subject.courseCode,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(subject.titleBn, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(subject.titleEn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Open Subject")
                        }
                    }
                }
            }

            if (yearPdfs.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Year $yearNumber PDF Books & Notes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(yearPdfs, key = { it.id }) { pdf ->
                    Card(
                        onClick = { onPdfClick(pdf.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(pdf.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text("${pdf.category} • ${pdf.subjectName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectDetailsScreen(
    subject: Subject?,
    chapters: List<Chapter>,
    pdfs: List<PdfResource>,
    onBackClick: () -> Unit,
    onRecordRecent: (String) -> Unit,
    onChapterClick: (String, String) -> Unit,
    onPdfClick: (String) -> Unit,
    onAskAiForSubject: (Subject) -> Unit,
    onStartMcqClick: () -> Unit
) {
    LaunchedEffect(subject?.id) {
        subject?.id?.let(onRecordRecent)
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        HonoursTopBar(
            title = subject?.titleBn ?: "Subject Details",
            subtitle = subject?.let { "${it.courseCode} • Year ${it.yearNumber} • ${it.credits} Credits" },
            onBackClick = onBackClick
        )

        if (subject == null) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                EmptyStateView(
                    icon = Icons.Default.MenuBook,
                    title = "Subject not found",
                    message = "This subject may have been moved or removed by the administrator."
                )
            }
            return
        }

        val subjectChapters = remember(chapters, subject.id) {
            chapters.filter { it.subjectId == subject.id }.sortedBy { it.chapterNumber }
        }
        val subjectPdfs = remember(pdfs, subject) {
            pdfs.filter {
                it.subjectName.contains(subject.titleBn, ignoreCase = true) ||
                    (subject.courseCode.isNotBlank() && it.courseCode.equals(subject.courseCode, ignoreCase = true))
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(subject.titleEn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (subject.syllabusNote.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(subject.syllabusNote, style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { onAskAiForSubject(subject) },
                                modifier = Modifier.weight(1f).testTag("subject_ask_ai_button")
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AI প্রশ্ন সমাধান")
                            }
                            OutlinedButton(
                                onClick = onStartMcqClick,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("MCQ Practice")
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Chapters (${subjectChapters.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (subjectChapters.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.MenuBook,
                        title = "No chapters published yet",
                        message = "Use the AI Question Solver above to ask any topic from ${subject.titleBn}, or wait for Admin chapter uploads.",
                        actionLabel = "Ask AI About ${subject.titleEn}",
                        onActionClick = { onAskAiForSubject(subject) }
                    )
                }
            } else {
                items(subjectChapters, key = { it.id }) { chapter ->
                    Card(
                        onClick = { onChapterClick(subject.id, chapter.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chapter_card_${chapter.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("${chapter.chapterNumber}", fontWeight = FontWeight.Bold)
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(chapter.titleBn, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                if (chapter.titleEn.isNotBlank()) {
                                    Text(chapter.titleEn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Open chapter")
                        }
                    }
                }
            }

            if (subjectPdfs.isNotEmpty()) {
                item {
                    Text("Related PDFs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                items(subjectPdfs, key = { it.id }) { pdf ->
                    Card(
                        onClick = { onPdfClick(pdf.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text(pdf.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text(pdf.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChapterDetailsScreen(
    subject: Subject?,
    chapter: Chapter?,
    isBookmarked: Boolean,
    onBackClick: () -> Unit,
    onToggleBookmark: (Chapter) -> Unit,
    onExplainWithAi: (Chapter) -> Unit,
    onSubmitReport: (targetId: String, reason: String, details: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showReportDialog by remember { mutableStateOf(false) }
    val tabs = listOf("Summary", "Definitions", "Short Q&A", "Broad & Creative")

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        HonoursTopBar(
            title = chapter?.titleBn ?: "Chapter Details",
            subtitle = subject?.titleBn ?: "Chapter Study Material",
            onBackClick = onBackClick,
            actions = {
                if (chapter != null) {
                    IconButton(
                        onClick = { onToggleBookmark(chapter) },
                        modifier = Modifier.testTag("chapter_bookmark_button")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark chapter"
                        )
                    }
                    IconButton(onClick = { showReportDialog = true }) {
                        Icon(Icons.Default.Flag, contentDescription = "Report chapter issue")
                    }
                }
            }
        )

        if (chapter == null) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                EmptyStateView(icon = Icons.Default.MenuBook, title = "Chapter not found", message = "Please go back and select another chapter.")
            }
            return
        }

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, maxLines = 1) }
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (chapter.topics.isNotEmpty() && selectedTab == 0) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("অধ্যায়ের আলোচ্য বিষয়সমূহ (Topics)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            chapter.topics.forEach { topic ->
                                Text("• $topic", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 2.dp))
                            }
                        }
                    }
                }
            }

            item {
                val bodyContent = when (selectedTab) {
                    0 -> chapter.summary.ifBlank { "No summary added yet." }
                    1 -> chapter.definitions.ifBlank { "No definitions added yet." }
                    2 -> chapter.shortQuestions.ifBlank { "No short Q&A added yet." }
                    else -> buildString {
                        if (chapter.broadQuestions.isNotBlank()) {
                            appendLine("রচনামূলক প্রশ্ন (Broad Questions):")
                            appendLine(chapter.broadQuestions)
                            appendLine()
                        }
                        if (chapter.creativeQuestions.isNotBlank()) {
                            appendLine("বিশ্লেষণধর্মী / সৃজনশীল প্রশ্ন (Analytical):")
                            appendLine(chapter.creativeQuestions)
                        }
                    }.ifBlank { "No broad or creative questions added yet." }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = bodyContent,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            item {
                Button(
                    onClick = { onExplainWithAi(chapter) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("chapter_ask_ai_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Explain & Solve with Gemini AI")
                }
            }
        }
    }

    if (showReportDialog && chapter != null) {
        ReportIssueDialog(
            onDismiss = { showReportDialog = false },
            onSubmit = { reason, details ->
                onSubmitReport(chapter.id, reason, details)
                showReportDialog = false
            }
        )
    }
}
