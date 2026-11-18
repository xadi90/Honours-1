package com.example.ui.screens.exams

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AiRemoteConfig
import com.example.data.model.BookmarkItem
import com.example.data.model.GeneratedMcqItem
import com.example.data.model.ModelTest
import com.example.data.model.ModelTestResult
import com.example.data.model.StudyQuestion
import com.example.data.model.UiState
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ErrorStateCard
import com.example.ui.components.HonoursTopBar
import com.example.ui.components.LoadingStateBox
import com.example.ui.components.ReportIssueDialog
import com.example.ui.components.SectionHeader
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.Emerald600
import kotlinx.coroutines.delay

@Composable
fun PreviousQuestionScreen(
    questions: List<StudyQuestion>,
    onBack: () -> Unit,
    onSolveWithAi: (String) -> Unit,
    onSubmitReport: (targetType: String, targetId: String, reason: String, details: String) -> Unit
) {
    var selectedYear by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var reportTarget by remember { mutableStateOf<StudyQuestion?>(null) }

    val prevQuestions = remember(questions, selectedYear, searchQuery) {
        questions.filter { q ->
            q.questionType == "previous_year" &&
                (selectedYear == 0 || q.yearNumber.toInt() == selectedYear) &&
                (searchQuery.isBlank() ||
                    q.prompt.contains(searchQuery, ignoreCase = true) ||
                    q.subjectName.contains(searchQuery, ignoreCase = true) ||
                    q.examYear.contains(searchQuery, ignoreCase = true))
        }
    }

    if (reportTarget != null) {
        ReportIssueDialog(
            onDismiss = { reportTarget = null },
            onSubmit = { reason, details ->
                onSubmitReport("previous_question", reportTarget?.id ?: "", reason, details)
                reportTarget = null
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HonoursTopBar(
            title = "বিগত সালের প্রশ্ন",
            subtitle = "NU Previous-Year Exam Questions",
            onBackClick = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("বিষয়, সাল বা প্রশ্ন খুঁজুন...") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prev_question_search")
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedYear == 0,
                            onClick = { selectedYear = 0 },
                            label = { Text("সকল বর্ষ") }
                        )
                    }
                    items(listOf(1, 2, 3, 4)) { yr ->
                        FilterChip(
                            selected = selectedYear == yr,
                            onClick = { selectedYear = yr },
                            label = { Text("${yr}ম/য়/র্থ বর্ষ".replace("1ম/য়/র্থ", "১ম").replace("2ম/য়/র্থ", "২য়").replace("3ম/য়/র্থ", "৩য়").replace("4ম/য়/র্থ", "৪র্থ")) }
                        )
                    }
                }
            }

            if (prevQuestions.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.School,
                        title = "কোনো প্রশ্ন পাওয়া যায়নি",
                        message = "অন্য বর্ষ বেছে নিন অথবা নতুন প্রশ্ন অ্যাডমিন প্যানেল থেকে যুক্ত করুন।"
                    )
                }
            } else {
                items(prevQuestions, key = { it.id }) { q ->
                    var expanded by remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${q.subjectName} • ${q.examYear.ifBlank { "NU" }}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                if (q.partLabel.isNotBlank()) {
                                    Text(
                                        text = q.partLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = q.prompt,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            AnimatedVisibility(visible = expanded && q.answerText.isNotBlank()) {
                                Column {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = q.answerText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (q.answerText.isNotBlank()) {
                                    TextButton(
                                        onClick = { expanded = !expanded },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(
                                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (expanded) "উত্তর লুকান" else "উত্তর দেখুন")
                                    }
                                }
                                OutlinedButton(
                                    onClick = { onSolveWithAi("${q.subjectName} (${q.examYear}): ${q.prompt}") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .minimumInteractiveComponentSize()
                                        .testTag("solve_prev_ai_${q.id}")
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Solve with AI")
                                }
                                IconButton(
                                    onClick = { reportTarget = q },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(Icons.Default.Flag, contentDescription = "Report issue")
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
fun SuggestionScreen(
    questions: List<StudyQuestion>,
    bookmarks: List<BookmarkItem>,
    onBack: () -> Unit,
    onToggleBookmark: (StudyQuestion) -> Unit,
    onSolveWithAi: (String) -> Unit
) {
    var selectedYear by remember { mutableIntStateOf(0) }
    val suggestions = remember(questions, selectedYear) {
        questions.filter { q ->
            q.questionType == "suggestion" &&
                (selectedYear == 0 || q.yearNumber.toInt() == selectedYear)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HonoursTopBar(
            title = "গুরুত্বপূর্ণ সাজেশন",
            subtitle = "Exam-Focused Honours Suggestions",
            onBackClick = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedYear == 0,
                            onClick = { selectedYear = 0 },
                            label = { Text("সকল বর্ষ") }
                        )
                    }
                    items(listOf(1, 2, 3, 4)) { yr ->
                        val yrBn = when (yr) {
                            1 -> "১ম বর্ষ"
                            2 -> "২য় বর্ষ"
                            3 -> "৩য় বর্ষ"
                            else -> "৪র্থ বর্ষ"
                        }
                        FilterChip(
                            selected = selectedYear == yr,
                            onClick = { selectedYear = yr },
                            label = { Text(yrBn) }
                        )
                    }
                }
            }

            if (suggestions.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.Lightbulb,
                        title = "সাজেশন তালিকা খালি",
                        message = "এডমিন প্যানেল থেকে নতুন পরীক্ষার সাজেশন যুক্ত করা যাবে।"
                    )
                }
            } else {
                items(suggestions, key = { it.id }) { item ->
                    val isBookmarked = bookmarks.any { it.itemType == "suggestion" && it.targetId == item.id }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = item.priority.ifBlank { "Important" },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onToggleBookmark(item) },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(
                                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Bookmark suggestion",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Text(
                                text = "${item.subjectName} (${item.courseCode})",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.prompt,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (item.answerText.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = item.answerText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { onSolveWithAi("Prepare an exam-ready Honours answer for: ${item.prompt} (Subject: ${item.subjectName})") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .testTag("suggestion_ai_btn_${item.id}")
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AI দিয়ে পূর্ণাঙ্গ উত্তর তৈরি করুন")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun McqPracticeScreen(
    questions: List<StudyQuestion>,
    generatedMcqsState: UiState<List<GeneratedMcqItem>>,
    remoteConfig: AiRemoteConfig,
    onBack: () -> Unit,
    onGenerateAiMcqs: (topic: String, yearLabel: String, count: Int, lang: String, remoteConfig: AiRemoteConfig) -> Unit
) {
    val staticMcqs = remember(questions) {
        questions.filter { it.questionType == "mcq" && it.options.isNotEmpty() }.map {
            GeneratedMcqItem(
                question = it.prompt,
                options = it.options,
                correctIndex = it.correctOptionIndex.toInt(),
                explanation = it.explanation.ifBlank { it.answerText }
            )
        }
    }
    var customTopic by remember { mutableStateOf("রাষ্ট্রবিজ্ঞান ও বাংলাদেশের সংবিধান") }
    var selectedYearLabel by remember { mutableStateOf("অনার্স ১ম বর্ষ") }
    val selectedOptions = remember { mutableStateMapOf<Int, Int>() }

    val aiList = (generatedMcqsState as? UiState.Success)?.data ?: emptyList()
    val combinedList = remember(staticMcqs, aiList) { aiList + staticMcqs }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HonoursTopBar(
            title = "MCQ অনুশীলন",
            subtitle = "Interactive Quiz & Gemini AI MCQ Generator",
            onBackClick = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Gemini AI দিয়ে নতুন MCQ তৈরি করুন",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = customTopic,
                            onValueChange = { customTopic = it },
                            label = { Text("বিষয় বা অধ্যায়ের নাম লিখুন") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listOf("অনার্স ১ম বর্ষ", "অনার্স ২য় বর্ষ", "অনার্স ৩য় বর্ষ", "অনার্স ৪র্থ বর্ষ")) { yr ->
                                FilterChip(
                                    selected = selectedYearLabel == yr,
                                    onClick = { selectedYearLabel = yr },
                                    label = { Text(yr) }
                                )
                            }
                        }
                        Button(
                            onClick = {
                                selectedOptions.clear()
                                onGenerateAiMcqs(customTopic, selectedYearLabel, 5, "bn", remoteConfig)
                            },
                            enabled = generatedMcqsState !is UiState.Loading && customTopic.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("generate_mcq_ai_button")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (generatedMcqsState is UiState.Loading) "MCQ তৈরি হচ্ছে..." else "Generate 5 Practice MCQs with AI")
                        }
                    }
                }
            }

            if (generatedMcqsState is UiState.Loading) {
                item { LoadingStateBox("Gemini AI অনুশীলনমূলক MCQ তৈরি করছে...") }
            } else if (generatedMcqsState is UiState.Error) {
                item { ErrorStateCard(message = generatedMcqsState.message) }
            }

            item {
                val answeredCount = selectedOptions.size
                val correctCount = selectedOptions.entries.count { (idx, chosen) ->
                    combinedList.getOrNull(idx)?.correctIndex == chosen
                }
                SectionHeader(
                    title = "প্রশ্নমালা (${combinedList.size})",
                    subtitle = "উত্তর প্রদত্ত: $answeredCount • সঠিক: $correctCount"
                )
            }

            itemsIndexed(combinedList) { index, item ->
                val chosenIndex = selectedOptions[index]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "${index + 1}. ${item.question}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        item.options.forEachIndexed { optIdx, optText ->
                            val isSelected = chosenIndex == optIdx
                            val isCorrect = item.correctIndex == optIdx
                            val hasAnswered = chosenIndex != null

                            val containerColor = when {
                                hasAnswered && isCorrect -> Emerald600.copy(alpha = 0.16f)
                                hasAnswered && isSelected && !isCorrect -> CrimsonError.copy(alpha = 0.14f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                            }
                            Surface(
                                color = containerColor,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !hasAnswered) {
                                        selectedOptions[index] = optIdx
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${('A' + optIdx)}. $optText",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (hasAnswered && isCorrect) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Correct", tint = Emerald600, modifier = Modifier.size(18.dp))
                                    } else if (hasAnswered && isSelected && !isCorrect) {
                                        Icon(Icons.Default.Close, contentDescription = "Incorrect", tint = CrimsonError, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                        if (chosenIndex != null && item.explanation.isNotBlank()) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "ব্যাখ্যা: ${item.explanation}",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModelTestScreen(
    modelTests: List<ModelTest>,
    questions: List<StudyQuestion>,
    testHistory: List<ModelTestResult>,
    onBack: () -> Unit,
    onSaveResult: (ModelTestResult) -> Unit
) {
    val mcqBank = remember(questions) {
        questions.filter { it.questionType == "mcq" && it.options.isNotEmpty() }
    }
    var activeTest by remember { mutableStateOf<ModelTest?>(null) }
    var testQuestions by remember { mutableStateOf<List<StudyQuestion>>(emptyList()) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var remainingSeconds by remember { mutableIntStateOf(0) }
    var startTimeMs by remember { mutableStateOf(0L) }
    var isFinished by remember { mutableStateOf(false) }
    val selectedAnswers = remember { mutableStateMapOf<Int, Int>() }

    fun finishExam(test: ModelTest) {
        if (isFinished) return
        isFinished = true
        val total = testQuestions.size.coerceAtLeast(1)
        val correct = testQuestions.indices.count { idx ->
            selectedAnswers[idx] == testQuestions[idx].correctOptionIndex.toInt()
        }
        val percent = (correct * 100L) / total
        val elapsedSec = ((System.currentTimeMillis() - startTimeMs) / 1000L).coerceAtLeast(1L)
        onSaveResult(
            ModelTestResult(
                testId = test.id,
                testTitle = test.title,
                yearNumber = test.yearNumber,
                subjectName = test.subjectName,
                totalQuestions = total.toLong(),
                correctCount = correct.toLong(),
                scorePercent = percent,
                durationSeconds = elapsedSec
            )
        )
    }

    LaunchedEffect(activeTest, isFinished) {
        val current = activeTest ?: return@LaunchedEffect
        while (!isFinished && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1
            if (remainingSeconds <= 0) {
                finishExam(current)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HonoursTopBar(
            title = activeTest?.title ?: "মডেল টেস্ট",
            subtitle = if (activeTest != null && !isFinished) {
                val mins = remainingSeconds / 60
                val secs = remainingSeconds % 60
                "সময় বাকি: %02d:%02d".format(mins, secs)
            } else {
                "Timed NU Honours Model Tests"
            },
            onBackClick = {
                if (activeTest != null) {
                    activeTest = null
                    isFinished = false
                    selectedAnswers.clear()
                } else {
                    onBack()
                }
            }
        )

        val currentTest = activeTest
        if (currentTest == null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
                    .align(Alignment.CenterHorizontally),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    SectionHeader(
                        title = "উপলব্ধ মডেল টেস্ট (${modelTests.size})",
                        subtitle = "সময় ধরে পরীক্ষা দিন এবং ফলাফল প্রোফাইলে সংরক্ষণ করুন"
                    )
                }
                items(modelTests, key = { it.id }) { test ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(test.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${test.subjectName} • ${test.durationMinutes} মিনিট • ${mcqBank.size} প্রশ্ন",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val pool = mcqBank.filter { it.yearNumber == test.yearNumber }.ifEmpty { mcqBank }
                                    if (pool.isNotEmpty()) {
                                        testQuestions = pool
                                        currentIndex = 0
                                        selectedAnswers.clear()
                                        remainingSeconds = (test.durationMinutes.toInt().coerceAtLeast(1)) * 60
                                        startTimeMs = System.currentTimeMillis()
                                        isFinished = false
                                        activeTest = test
                                    }
                                },
                                enabled = mcqBank.isNotEmpty(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .testTag("start_model_test_${test.id}")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("পরীক্ষা শুরু করুন")
                            }
                        }
                    }
                }

                if (testHistory.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader(title = "পূর্ববর্তী ফলাফল (${testHistory.size})")
                    }
                    items(testHistory.take(8), key = { it.id }) { res ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(res.testTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = "${res.correctCount}/${res.totalQuestions} সঠিক • ${res.durationSeconds}s",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    color = if (res.scorePercent >= 60) Emerald600.copy(alpha = 0.15f) else MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "${res.scorePercent}%",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (res.scorePercent >= 60) Emerald600 else MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else if (!isFinished) {
            val q = testQuestions.getOrNull(currentIndex)
            if (q != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 680.dp)
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { (currentIndex + 1).toFloat() / testQuestions.size.coerceAtLeast(1) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "প্রশ্ন ${currentIndex + 1} / ${testQuestions.size}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "%02d:%02d".format(remainingSeconds / 60, remainingSeconds % 60),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(q.prompt, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            q.options.forEachIndexed { optIdx, optText ->
                                val selected = selectedAnswers[currentIndex] == optIdx
                                Surface(
                                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedAnswers[currentIndex] = optIdx }
                                ) {
                                    Text(
                                        text = "${('A' + optIdx)}. $optText",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { if (currentIndex > 0) currentIndex -= 1 },
                            enabled = currentIndex > 0,
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("পূর্ববর্তী")
                        }
                        if (currentIndex < testQuestions.lastIndex) {
                            Button(
                                onClick = { currentIndex += 1 },
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                            ) {
                                Text("পরবর্তী")
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                            }
                        } else {
                            Button(
                                onClick = { finishExam(currentTest) },
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("submit_model_test_button")
                            ) {
                                Text("জমা দিন")
                            }
                        }
                    }
                }
            }
        } else {
            val total = testQuestions.size.coerceAtLeast(1)
            val correct = testQuestions.indices.count { selectedAnswers[it] == testQuestions[it].correctOptionIndex.toInt() }
            val percent = (correct * 100) / total
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
                    .align(Alignment.CenterHorizontally),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("ফলাফল সারাংশ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$correct / $total ($percent%)",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = {
                                        selectedAnswers.clear()
                                        currentIndex = 0
                                        remainingSeconds = (currentTest.durationMinutes.toInt().coerceAtLeast(1)) * 60
                                        startTimeMs = System.currentTimeMillis()
                                        isFinished = false
                                    }
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("আবার অনুশীলন")
                                }
                                OutlinedButton(onClick = { activeTest = null; isFinished = false }) {
                                    Text("সব টেস্ট")
                                }
                            }
                        }
                    }
                }

                itemsIndexed(testQuestions) { idx, q ->
                    val chosen = selectedAnswers[idx]
                    val right = q.correctOptionIndex.toInt()
                    val ok = chosen == right
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "${idx + 1}. ${q.prompt}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "আপনার উত্তর: ${chosen?.let { q.options.getOrNull(it) } ?: "উত্তর দেওয়া হয়নি"}",
                                color = if (ok) Emerald600 else CrimsonError,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "সঠিক উত্তর: ${q.options.getOrNull(right).orEmpty()}",
                                color = Emerald600,
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (q.explanation.isNotBlank()) {
                                Text(
                                    text = q.explanation,
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
}
