package com.example.ui.screens.profile

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.BuildConfig
import com.example.R
import com.example.core.AppUpdateInfo
import com.example.core.PdfUtils
import com.example.data.model.BookmarkItem
import com.example.data.model.Department
import com.example.data.model.ModelTestResult
import com.example.data.model.StructuredAiAnswer
import com.example.data.model.UiState
import com.example.data.model.UserProfile
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ErrorStateCard
import com.example.ui.components.HonoursTopBar
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.Emerald600

@Composable
fun BookmarkScreen(
    bookmarks: List<BookmarkItem>,
    savedAnswers: List<StructuredAiAnswer>,
    onOpenSavedAnswer: (StructuredAiAnswer) -> Unit,
    onOpenPdf: (String) -> Unit,
    onRemoveBookmark: (String) -> Unit,
    onDeleteSavedAnswer: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("AI উত্তর", "PDF", "অধ্যায় ও অন্যান্য")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HonoursTopBar(
            title = "বুকমার্ক ও সংরক্ষিত নোট",
            subtitle = "${savedAnswers.size} AI উত্তর • ${bookmarks.size} বুকমার্ক"
        )

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    val displayed = savedAnswers
                    if (displayed.isEmpty()) {
                        item {
                            EmptyStateView(
                                icon = Icons.Default.AutoAwesome,
                                title = "কোনো AI উত্তর সংরক্ষিত নেই",
                                message = "AI প্রশ্ন সমাধান ব্যবহার করে যেকোনো প্রশ্নের উত্তর এখানে সংরক্ষণ ও বুকমার্ক করতে পারবেন।"
                            )
                        }
                    } else {
                        items(displayed, key = { it.id }) { ans ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenSavedAnswer(ans) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = ans.question,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${ans.subjectName} • ${ans.answerLength}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (ans.bookmarked) {
                                        Icon(
                                            Icons.Default.Bookmark,
                                            contentDescription = "Bookmarked",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteSavedAnswer(ans.id) },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete saved answer")
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    val pdfBookmarks = bookmarks.filter { it.itemType == "pdf" || it.itemType == "pdf_page" }
                    if (pdfBookmarks.isEmpty()) {
                        item {
                            EmptyStateView(
                                icon = Icons.Default.PictureAsPdf,
                                title = "কোনো PDF বুকমার্ক নেই",
                                message = "PDF রিডার বা লাইব্রেরি থেকে যেকোনো PDF অথবা নির্দিষ্ট পৃষ্ঠা বুকমার্ক করুন।"
                            )
                        }
                    } else {
                        items(pdfBookmarks, key = { it.id }) { item ->
                            BookmarkRowCard(
                                item = item,
                                onClick = { onOpenPdf(item.targetId) },
                                onDelete = { onRemoveBookmark(item.id) }
                            )
                        }
                    }
                }
                else -> {
                    val otherBookmarks = bookmarks.filter { it.itemType != "pdf" && it.itemType != "pdf_page" }
                    if (otherBookmarks.isEmpty()) {
                        item {
                            EmptyStateView(
                                icon = Icons.Default.MenuBook,
                                title = "কোনো অধ্যায় বা সাজেশন বুকমার্ক নেই",
                                message = "অধ্যায় বা সাজেশন কার্ডের বুকমার্ক আইকনে ট্যাপ করে এখানে সংরক্ষণ করুন।"
                            )
                        }
                    } else {
                        items(otherBookmarks, key = { it.id }) { item ->
                            BookmarkRowCard(
                                item = item,
                                onClick = {},
                                onDelete = { onRemoveBookmark(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookmarkRowCard(
    item: BookmarkItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Bookmark,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    text = if (item.pageNumber > 0) "${item.subtitle} • Page ${item.pageNumber}" else item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.minimumInteractiveComponentSize()) {
                Icon(Icons.Default.Delete, contentDescription = "Remove bookmark")
            }
        }
    }
}

@Composable
fun ProfileScreen(
    profile: UserProfile?,
    departments: List<Department>,
    bookmarksCount: Int,
    savedAnswersCount: Int,
    testResults: List<ModelTestResult>,
    isBackendAdmin: Boolean,
    onSavePreferences: (name: String, deptId: String, deptName: String, year: Long, lang: String) -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAdminPanel: () -> Unit,
    onOpenAboutDeveloper: () -> Unit,
    onSignOut: () -> Unit
) {
    var nameInput by remember(profile?.displayName) { mutableStateOf(profile?.displayName ?: "") }
    var selectedYear by remember(profile?.selectedYear) { mutableLongStateOf(profile?.selectedYear ?: 1L) }
    var selectedDeptId by remember(profile?.selectedDepartmentId) { mutableStateOf(profile?.selectedDepartmentId ?: "political_science") }
    var selectedLang by remember(profile?.languagePreference) { mutableStateOf(profile?.languagePreference ?: "bn_en") }

    val selectedDeptName = departments.firstOrNull { it.id == selectedDeptId }
        ?.let { "${it.nameBn} (${it.nameEn})" }
        ?: profile?.selectedDepartmentName
        ?: "রাষ্ট্রবিজ্ঞান (Political Science)"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .widthIn(max = 680.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            HonoursTopBar(title = "শিক্ষার্থী প্রোফাইল", subtitle = "National University • Honours Solution")
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (!profile?.photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = profile?.photoUrl,
                            contentDescription = "Profile photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile?.displayName?.ifBlank { "NU Student" } ?: "NU Student",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = profile?.email ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "অনার্স ${selectedYear}ম/য়/র্থ বর্ষ".replace("1ম/য়/র্থ", "১ম").replace("2ম/য়/র্থ", "২য়").replace("3ম/য়/র্থ", "৩য়").replace("4ম/য়/র্থ", "৪র্থ") + " • $selectedDeptName",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileStatBox("AI উত্তর", savedAnswersCount.toString(), Modifier.weight(1f))
                ProfileStatBox("বুকমার্ক", bookmarksCount.toString(), Modifier.weight(1f))
                ProfileStatBox("মডেল টেস্ট", testResults.size.toString(), Modifier.weight(1f))
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader(title = "একাডেমিক তথ্য সম্পাদনা")
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("শিক্ষার্থীর নাম") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("অনার্স বর্ষ নির্বাচন করুন", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf(1L, 2L, 3L, 4L)) { yr ->
                            val lbl = when (yr) {
                                1L -> "১ম বর্ষ"
                                2L -> "২য় বর্ষ"
                                3L -> "৩য় বর্ষ"
                                else -> "৪র্থ বর্ষ"
                            }
                            FilterChip(
                                selected = selectedYear == yr,
                                onClick = { selectedYear = yr },
                                label = { Text(lbl) }
                            )
                        }
                    }
                    Text("পছন্দের বিভাগ", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(departments.take(10), key = { it.id }) { dept ->
                            FilterChip(
                                selected = selectedDeptId == dept.id,
                                onClick = { selectedDeptId = dept.id },
                                label = { Text(dept.nameBn) }
                            )
                        }
                    }
                    Text("অ্যাপের ভাষা", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = selectedLang == "bn_en", onClick = { selectedLang = "bn_en" }, label = { Text("বাংলা + English") })
                        FilterChip(selected = selectedLang == "bn", onClick = { selectedLang = "bn" }, label = { Text("শুধু বাংলা") })
                        FilterChip(selected = selectedLang == "en", onClick = { selectedLang = "en" }, label = { Text("English") })
                    }
                    Button(
                        onClick = { onSavePreferences(nameInput, selectedDeptId, selectedDeptName, selectedYear, selectedLang) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("save_profile_prefs_button")
                    ) {
                        Text("তথ্য সংরক্ষণ করুন")
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    ProfileActionRow(
                        icon = Icons.Default.Download,
                        title = "ডাউনলোড ম্যানেজার",
                        subtitle = "অফলাইন সংরক্ষিত PDF ফাইলসমূহ",
                        testTag = "profile_downloads_row",
                        onClick = onOpenDownloads
                    )
                    HorizontalDivider()
                    ProfileActionRow(
                        icon = Icons.Default.Settings,
                        title = "সেটিংস ও আপডেট চেকার",
                        subtitle = "থিম, ভাষা এবং অ্যাপের সর্বশেষ ভার্সন যাচাই",
                        testTag = "profile_settings_row",
                        onClick = onOpenSettings
                    )
                    HorizontalDivider()
                    ProfileActionRow(
                        icon = Icons.Default.AdminPanelSettings,
                        title = "কনটেন্ট ম্যানেজমেন্ট (Admin Panel)",
                        subtitle = if (isBackendAdmin) "এডমিন অনুমোদিত অ্যাকাউন্ট • পাসওয়ার্ড দিয়ে প্রবেশ করুন" else "শুধুমাত্র নির্ধারিত এডমিনের জন্য",
                        testTag = "profile_admin_row",
                        onClick = onOpenAdminPanel
                    )
                    HorizontalDivider()
                    ProfileActionRow(
                        icon = Icons.Default.Info,
                        title = "ডেভেলপার পরিচিতি",
                        subtitle = "Xadi Ahmed • জাতীয় বিশ্ববিদ্যালয়",
                        testTag = "profile_about_row",
                        onClick = onOpenAboutDeveloper
                    )
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onSignOut,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("sign_out_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out (লগ আউট করুন)")
            }
        }
    }
}

@Composable
private fun ProfileStatBox(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ProfileActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SettingsScreen(
    themeMode: AppThemeMode,
    languagePref: String,
    updateState: UiState<AppUpdateInfo>,
    onBack: () -> Unit,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onLanguageChange: (String) -> Unit,
    onCheckUpdates: () -> Unit
) {
    val context = LocalContext.current
    val offlineFiles = remember {
        PdfUtils.getPdfDirectory(context).listFiles()?.filter { it.extension.equals("pdf", true) } ?: emptyList()
    }
    val totalMb = remember(offlineFiles) {
        val bytes = offlineFiles.sumOf { it.length() }
        String.format("%.2f MB", bytes / (1024.0 * 1024.0))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HonoursTopBar(title = "সেটিংস ও আপডেট", subtitle = "Preferences & App Updates", onBackClick = onBack)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionHeader(title = "অ্যাপ থিম (Theme Mode)")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppThemeMode.entries.forEach { mode ->
                                FilterChip(
                                    selected = themeMode == mode,
                                    onClick = { onThemeModeChange(mode) },
                                    label = { Text(mode.label) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionHeader(title = "ভাষার পছন্দ (Language)")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = languagePref == "bn_en", onClick = { onLanguageChange("bn_en") }, label = { Text("বাংলা + English") })
                            FilterChip(selected = languagePref == "bn", onClick = { onLanguageChange("bn") }, label = { Text("বাংলা") })
                            FilterChip(selected = languagePref == "en", onClick = { onLanguageChange("en") }, label = { Text("English") })
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader(title = "অফলাইন PDF ক্যাশ")
                        Text(
                            text = "${offlineFiles.size}টি PDF অফলাইনে সংরক্ষিত ($totalMb)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionHeader(
                            title = "অ্যাপ আপডেট চেকার",
                            subtitle = "Current Version: v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
                        )
                        Button(
                            onClick = onCheckUpdates,
                            enabled = updateState !is UiState.Loading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("check_updates_button")
                        ) {
                            if (updateState is UiState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Checking...")
                            } else {
                                Icon(Icons.Default.SystemUpdate, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("নতুন আপডেট যাচাই করুন")
                            }
                        }

                        when (updateState) {
                            is UiState.Success -> {
                                val info = updateState.data
                                Surface(
                                    color = if (info.updateAvailable) MaterialTheme.colorScheme.primaryContainer else Emerald600.copy(alpha = 0.14f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald600, modifier = Modifier.size(18.dp))
                                            Text(
                                                text = if (info.updateAvailable) {
                                                    "New Update Available: v${info.latestVersionName}"
                                                } else {
                                                    "আপনি সর্বশেষ সংস্করণ (v${info.currentVersionName}) ব্যবহার করছেন।"
                                                },
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        if (info.releaseNotes.isNotBlank()) {
                                            Text(info.releaseNotes, style = MaterialTheme.typography.bodySmall)
                                        }
                                        Text(
                                            text = "Source: ${info.sourceLabel}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (info.updateAvailable) {
                                            Button(
                                                onClick = {
                                                    runCatching {
                                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(info.downloadUrl)))
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(Icons.Default.OpenInBrowser, contentDescription = null)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Download Latest APK")
                                            }
                                        }
                                    }
                                }
                            }
                            is UiState.Error -> {
                                ErrorStateCard(message = updateState.message, onRetry = onCheckUpdates)
                            }
                            else -> Unit
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AboutDeveloperScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HonoursTopBar(title = "ডেভেলপার ও অ্যাপ পরিচিতি", subtitle = "Honours Solution", onBackClick = onBack)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_honours_emblem),
                            contentDescription = "Honours Solution Emblem",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(88.dp)
                                .clip(RoundedCornerShape(22.dp))
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Honours Solution", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                        Text(
                            text = "National University (NU), Bangladesh",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "জাতীয় বিশ্ববিদ্যালয়ের রাষ্ট্রবিজ্ঞান অনার্স ১ম থেকে ৪র্থ বর্ষের শিক্ষার্থীদের জন্য আধুনিক একাডেমিক সহায়িকা, AI প্রশ্ন সমাধান এবং সকল বিভাগের PDF লাইব্রেরি।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader(title = "Developer Information")
                        Text("Developer Name: Xadi Ahmed", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Department Focus: Political Science (অনার্স ১ম–৪র্থ বর্ষ)", style = MaterialTheme.typography.bodySmall)
                        Text("App Version: v${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodySmall)

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    runCatching {
                                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:01878864913")))
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("developer_call_button")
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("01878864913")
                            }
                            OutlinedButton(
                                onClick = {
                                    runCatching {
                                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:xadiahmed23@gmail.com")).apply {
                                            putExtra(Intent.EXTRA_SUBJECT, "Honours Solution — Feedback / Support")
                                        }
                                        context.startActivity(intent)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("developer_email_button")
                            ) {
                                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Email")
                            }
                        }
                        Text(
                            text = "Email: xadiahmed23@gmail.com",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader(title = "Official Web & Releases")
                        OutlinedButton(
                            onClick = {
                                runCatching {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://honours-solution.pages.dev")))
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Official Website (Cloudflare Pages)")
                        }
                    }
                }
            }
        }
    }
}
