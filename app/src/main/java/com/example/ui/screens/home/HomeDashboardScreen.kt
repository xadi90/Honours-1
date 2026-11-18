package com.example.ui.screens.home

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.catalog.AcademicSeedCatalog
import com.example.data.model.Announcement
import com.example.data.model.Chapter
import com.example.data.model.Department
import com.example.data.model.PdfResource
import com.example.data.model.StudyQuestion
import com.example.data.model.Subject
import com.example.data.model.UserProfile
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HonoursTopBar
import com.example.ui.components.SectionHeader
import com.example.ui.theme.Gold500
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy900

@Composable
fun HomeDashboardScreen(
    profile: UserProfile?,
    subjects: List<Subject>,
    pdfs: List<PdfResource>,
    announcements: List<Announcement>,
    recentPdfIds: List<String>,
    recentSubjectIds: List<String>,
    bookmarksCount: Int,
    onOpenDrawer: () -> Unit,
    onSearchClick: () -> Unit,
    onAiSolverClick: () -> Unit,
    onPoliticalScienceClick: () -> Unit,
    onYearClick: (Int) -> Unit,
    onPdfLibraryClick: () -> Unit,
    onPreviousQuestionsClick: () -> Unit,
    onSuggestionsClick: () -> Unit,
    onMcqPracticeClick: () -> Unit,
    onModelTestClick: () -> Unit,
    onBookmarksClick: () -> Unit,
    onDownloadsClick: () -> Unit,
    onSubjectClick: (String) -> Unit,
    onPdfClick: (String) -> Unit,
    onAboutDeveloperClick: () -> Unit
) {
    val studentName = profile?.displayName?.takeIf { it.isNotBlank() } ?: "NU Student"
    val recentPdfs = remember(pdfs, recentPdfIds) {
        recentPdfIds.mapNotNull { id -> pdfs.firstOrNull { it.id == id } }
    }
    val recentSubjects = remember(subjects, recentSubjectIds) {
        recentSubjectIds.mapNotNull { id -> subjects.firstOrNull { it.id == id } }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header: App Logo, Name, Welcome, Drawer Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_honours_emblem),
                        contentDescription = "Honours Solution Logo",
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Column {
                        Text(
                            text = "Honours Solution",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "স্বাগতম, $studentName • Year ${profile?.selectedYear ?: 1L}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                IconButton(
                    onClick = onOpenDrawer,
                    modifier = Modifier.testTag("home_menu_button")
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "Open menu")
                }
            }
        }

        // Global Search Bar
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSearchClick)
                    .testTag("home_search_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Search course, subject, PDF, chapter (বাংলা / English)...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // AI Question Solver Hero Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .clickable(onClick = onAiSolverClick)
                    .testTag("home_ai_solver_banner")
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_ai_solver_banner),
                    contentDescription = "AI Question Solver Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(168.dp)
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Navy900.copy(alpha = 0.92f),
                                    Navy900.copy(alpha = 0.65f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.CenterStart),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = Gold500,
                            contentColor = Navy900,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "GEMINI AI TUTOR",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Text(
                            text = "AI প্রশ্ন সমাধান",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "টাইপ করুন, ছবি তুলুন বা PDF দিন — পরীক্ষার উপযোগী পূর্ণাঙ্গ উত্তর পান",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.widthIn(max = 260.dp)
                        )
                    }
                }
            }
        }

        // Announcements
        if (announcements.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Announcements & NU Notices",
                    subtitle = "গুরুত্বপূর্ণ নোটিশ ও আপডেট"
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(announcements, key = { it.id }) { ann ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.width(280.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Campaign,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = ann.tag,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = ann.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = ann.body,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Political Science 1st-4th Year Cards
        item {
            SectionHeader(
                title = "রাষ্ট্রবিজ্ঞান (Political Science)",
                subtitle = "Main Structured Academic Solution (1st–4th Year)",
                actionLabel = "All Years",
                onActionClick = onPoliticalScienceClick
            )
            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AcademicSeedCatalog.honoursYears.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { (yearNum, titleBn) ->
                            val courseCount = subjects.count { it.yearNumber == yearNum.toLong() }
                            Card(
                                onClick = { onYearClick(yearNum) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("home_year_${yearNum}_card")
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = "Year $yearNum",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = titleBn,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$courseCount courses • Notes & Q&A",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // All Departments PDF Library Banner
        item {
            Card(
                onClick = onPdfLibraryClick,
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Navy900),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_all_dept_pdf_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        color = Gold500.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.size(50.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = Gold500,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "সকল বিভাগের PDF লাইব্রেরি",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "22+ Honours Departments • Books, Notes, Lecture Sheets & Offline Reader",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open PDF Library",
                        tint = Gold500
                    )
                }
            }
        }

        // Quick Tools Grid (Previous Questions, Suggestions, Notes & Books, Model Test, MCQ, Bookmarks, Downloads)
        item {
            SectionHeader(
                title = "Exam & Study Tools",
                subtitle = "প্রশ্নব্যাংক, সাজেশন, মডেল টেস্ট ও অফলাইন টুলস"
            )
            Spacer(modifier = Modifier.height(10.dp))
            val tools = listOf(
                Triple("Previous Questions", Icons.Default.History, onPreviousQuestionsClick),
                Triple("Suggestions", Icons.Default.Lightbulb, onSuggestionsClick),
                Triple("Notes & Books", Icons.Default.MenuBook, onPdfLibraryClick),
                Triple("Model Test", Icons.Default.Timer, onModelTestClick),
                Triple("MCQ Practice", Icons.Default.Quiz, onMcqPracticeClick),
                Triple("Bookmarks ($bookmarksCount)", Icons.Default.Bookmark, onBookmarksClick),
                Triple("Downloads", Icons.Default.Download, onDownloadsClick),
                Triple("Pol. Science", Icons.Default.School, onPoliticalScienceClick)
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                tools.chunked(2).forEach { rowTools ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowTools.forEach { (label, icon, onClick) ->
                            QuickToolTile(
                                label = label,
                                icon = icon,
                                onClick = onClick,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Recently Viewed
        if (recentSubjects.isNotEmpty() || recentPdfs.isNotEmpty()) {
            item {
                SectionHeader(title = "Recently Viewed", subtitle = "আপনার সাম্প্রতিক পাঠ্যবিষয় ও PDF")
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(recentSubjects, key = { "subj_${it.id}" }) { subj ->
                        Card(
                            onClick = { onSubjectClick(subj.id) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.width(200.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(subj.courseCode, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Text(subj.titleBn, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("Year ${subj.yearNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    items(recentPdfs, key = { "pdf_${it.id}" }) { pdf ->
                        Card(
                            onClick = { onPdfClick(pdf.id) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.width(220.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(pdf.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                Text(pdf.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(pdf.departmentName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // Latest Uploaded PDFs
        item {
            SectionHeader(
                title = "Latest Uploaded PDFs",
                subtitle = "সাম্প্রতিক যুক্ত হওয়া বই ও নোট",
                actionLabel = "See All",
                onActionClick = onPdfLibraryClick
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                pdfs.take(4).forEach { pdf ->
                    Card(
                        onClick = { onPdfClick(pdf.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.PictureAsPdf,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pdf.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${pdf.departmentName} • Year ${pdf.yearNumber} • ${pdf.category}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Developer Info Card
        item {
            Card(
                onClick = onAboutDeveloperClick,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_developer_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text("Developed by Xadi Ahmed", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("National University, Bangladesh • Contact & Info", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "View developer info")
                }
            }
        }
    }
}

@Composable
private fun QuickToolTile(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchScreen(
    departments: List<Department>,
    subjects: List<Subject>,
    chapters: List<Chapter>,
    pdfs: List<PdfResource>,
    questions: List<StudyQuestion>,
    searchHistory: List<String>,
    onBackClick: () -> Unit,
    onRecordSearch: (String) -> Unit,
    onClearHistory: () -> Unit,
    onDepartmentClick: (String) -> Unit,
    onSubjectClick: (String) -> Unit,
    onChapterClick: (String, String) -> Unit,
    onPdfClick: (String) -> Unit,
    onAskAiClick: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Departments", "Subjects", "Chapters", "PDFs", "Questions")

    val q = query.trim().lowercase()
    val matchedDepts = remember(q, departments) {
        if (q.isBlank()) emptyList()
        else departments.filter {
            it.nameBn.lowercase().contains(q) || it.nameEn.lowercase().contains(q) || it.code.lowercase().contains(q)
        }
    }
    val matchedSubjects = remember(q, subjects) {
        if (q.isBlank()) emptyList()
        else subjects.filter {
            it.titleBn.lowercase().contains(q) || it.titleEn.lowercase().contains(q) ||
                it.courseCode.lowercase().contains(q) || "year ${it.yearNumber}".contains(q)
        }
    }
    val matchedChapters = remember(q, chapters) {
        if (q.isBlank()) emptyList()
        else chapters.filter {
            it.titleBn.lowercase().contains(q) || it.titleEn.lowercase().contains(q) ||
                it.summary.lowercase().contains(q)
        }
    }
    val matchedPdfs = remember(q, pdfs) {
        if (q.isBlank()) emptyList()
        else pdfs.filter {
            it.title.lowercase().contains(q) || it.subjectName.lowercase().contains(q) ||
                it.departmentName.lowercase().contains(q) || it.category.lowercase().contains(q) ||
                it.courseCode.lowercase().contains(q)
        }
    }
    val matchedQuestions = remember(q, questions) {
        if (q.isBlank()) emptyList()
        else questions.filter {
            it.prompt.lowercase().contains(q) || it.subjectName.lowercase().contains(q)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        HonoursTopBar(title = "Global Search", subtitle = "Bengali & English Academic Search", onBackClick = onBackClick)

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search course code, subject, chapter, PDF...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("global_search_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) }
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (q.isBlank()) {
                if (searchHistory.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Recent Searches", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            TextButton(onClick = onClearHistory) { Text("Clear") }
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(searchHistory) { term ->
                                AssistChip(
                                    onClick = { query = term },
                                    label = { Text(term) },
                                    leadingIcon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                )
                            }
                        }
                    }
                }
                item {
                    EmptyStateView(
                        icon = Icons.Default.Search,
                        title = "Search Across Honours Solution",
                        message = "Try searching 'রাষ্ট্রবিজ্ঞান', 'সংবিধান', 'PS-101', 'প্লেটো', or any department name."
                    )
                }
            } else {
                val noResults = matchedDepts.isEmpty() && matchedSubjects.isEmpty() &&
                    matchedChapters.isEmpty() && matchedPdfs.isEmpty() && matchedQuestions.isEmpty()

                if (noResults) {
                    item {
                        EmptyStateView(
                            icon = Icons.Default.AutoAwesome,
                            title = "No local match for \"$query\"",
                            message = "Ask our Gemini AI Question Solver for an immediate academic explanation.",
                            actionLabel = "Ask AI Tutor",
                            onActionClick = {
                                onRecordSearch(query)
                                onAskAiClick(query)
                            }
                        )
                    }
                } else {
                    if (selectedFilter in listOf("All", "Departments")) {
                        items(matchedDepts, key = { "d_${it.id}" }) { dept ->
                            SearchResultCard(
                                badge = "Department",
                                title = "${dept.nameBn} (${dept.nameEn})",
                                subtitle = dept.code,
                                onClick = {
                                    onRecordSearch(query)
                                    onDepartmentClick(dept.id)
                                }
                            )
                        }
                    }
                    if (selectedFilter in listOf("All", "Subjects")) {
                        items(matchedSubjects, key = { "s_${it.id}" }) { subj ->
                            SearchResultCard(
                                badge = "Year ${subj.yearNumber} • ${subj.courseCode}",
                                title = subj.titleBn,
                                subtitle = subj.titleEn,
                                onClick = {
                                    onRecordSearch(query)
                                    onSubjectClick(subj.id)
                                }
                            )
                        }
                    }
                    if (selectedFilter in listOf("All", "Chapters")) {
                        items(matchedChapters, key = { "c_${it.id}" }) { chap ->
                            SearchResultCard(
                                badge = "Chapter ${chap.chapterNumber}",
                                title = chap.titleBn,
                                subtitle = chap.titleEn,
                                onClick = {
                                    onRecordSearch(query)
                                    onChapterClick(chap.subjectId, chap.id)
                                }
                            )
                        }
                    }
                    if (selectedFilter in listOf("All", "PDFs")) {
                        items(matchedPdfs, key = { "p_${it.id}" }) { pdf ->
                            SearchResultCard(
                                badge = "PDF • ${pdf.category}",
                                title = pdf.title,
                                subtitle = "${pdf.departmentName} • Year ${pdf.yearNumber}",
                                onClick = {
                                    onRecordSearch(query)
                                    onPdfClick(pdf.id)
                                }
                            )
                        }
                    }
                    if (selectedFilter in listOf("All", "Questions")) {
                        items(matchedQuestions, key = { "q_${it.id}" }) { question ->
                            SearchResultCard(
                                badge = question.questionType.uppercase(),
                                title = question.prompt,
                                subtitle = "${question.subjectName} • Year ${question.yearNumber}",
                                onClick = {
                                    onRecordSearch(query)
                                    onAskAiClick(question.prompt)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    badge: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(badge, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            if (subtitle.isNotBlank()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
