package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.catalog.CollegeSeedCatalog
import com.example.data.model.CollegeProfileConfig
import com.example.data.model.CollegeSectionBlock
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HonoursTopBar
import com.example.ui.components.SectionHeader

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminCollegeEditorScreen(
    config: CollegeProfileConfig,
    sections: List<CollegeSectionBlock>,
    onBack: () -> Unit,
    onPreviewCollege: () -> Unit,
    onSaveProfile: (CollegeProfileConfig) -> Unit,
    onSaveSection: (CollegeSectionBlock) -> Unit,
    onDeleteSection: (String) -> Unit,
    onMoveSection: (index: Int, direction: Int) -> Unit,
    onToggleSectionStatus: (CollegeSectionBlock) -> Unit,
    onSeedStarterTemplate: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("কার্ড ও বিন্যাস", "হেডার ও থিম", "তথ্য ও যোগাযোগ")

    var editingBlock by remember { mutableStateOf<CollegeSectionBlock?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HonoursTopBar(
            title = "সুজাত আলী কলেজ সাজান",
            subtitle = "Sujat Ali Honours College, Pingna • Customizer",
            onBackClick = onBack,
            actions = {
                IconButton(
                    onClick = onPreviewCollege,
                    modifier = Modifier.testTag("admin_college_preview_button")
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = "প্রিভিউ দেখুন")
                }
            }
        )

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { idx, label ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                )
            }
        }

        when (selectedTab) {
            0 -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 760.dp)
                        .align(Alignment.CenterHorizontally),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                SectionHeader(
                                    title = "কলেজের কার্ড ও ক্রম সাজান",
                                    subtitle = "নতুন নোটিশ, রুটিন, বিভাগ, শিক্ষকমণ্ডলী বা কাস্টম কার্ড যোগ করুন এবং ▲/▼ দিয়ে নিজের ইচ্ছেমতো সাজান।"
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val nextOrder = (sections.maxOfOrNull { it.sortOrder } ?: 0L) + 1L
                                            editingBlock = CollegeSectionBlock(
                                                blockType = "custom",
                                                iconKey = "school",
                                                accentStyle = "primary",
                                                layoutSpan = "full",
                                                sortOrder = nextOrder,
                                                status = "published"
                                            )
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .minimumInteractiveComponentSize()
                                            .testTag("admin_college_add_block_button")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("নতুন কার্ড")
                                    }
                                    OutlinedButton(
                                        onClick = onSeedStarterTemplate,
                                        modifier = Modifier
                                            .weight(1f)
                                            .minimumInteractiveComponentSize()
                                            .testTag("admin_college_seed_button")
                                    ) {
                                        Text("স্টার্টার টেমপ্লেট", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }

                    if (sections.isEmpty()) {
                        item {
                            EmptyStateView(
                                icon = Icons.Default.AccountBalance,
                                title = "এখনো কোনো কাস্টম কার্ড Firestore-এ নেই",
                                message = "'স্টার্টার টেমপ্লেট' বাটনে চাপ দিন অথবা 'নতুন কার্ড' দিয়ে নিজের মতো করে সাজানো শুরু করুন।"
                            )
                        }
                    } else {
                        itemsIndexed(sections, key = { _, item -> item.id }) { index, block ->
                            val isPublished = block.status == "published"
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isPublished) {
                                        MaterialTheme.colorScheme.surface
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_college_block_${block.id}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = "#${index + 1}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                            Surface(
                                                color = MaterialTheme.colorScheme.secondaryContainer,
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = if (block.layoutSpan == "half") "Half" else "Full",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                                )
                                            }
                                            if (block.pinned) {
                                                Icon(
                                                    Icons.Default.PushPin,
                                                    contentDescription = "Pinned",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            if (!isPublished) {
                                                Surface(
                                                    color = MaterialTheme.colorScheme.errorContainer,
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(
                                                        text = "লুকানো (Draft)",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = { onMoveSection(index, -1) },
                                                enabled = index > 0
                                            ) {
                                                Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up")
                                            }
                                            IconButton(
                                                onClick = { onMoveSection(index, 1) },
                                                enabled = index < sections.lastIndex
                                            ) {
                                                Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down")
                                            }
                                        }
                                    }

                                    Text(
                                        text = block.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (block.subtitle.isNotBlank()) {
                                        Text(
                                            text = block.subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    if (block.body.isNotBlank()) {
                                        Text(
                                            text = block.body,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(onClick = { onToggleSectionStatus(block) }) {
                                            Icon(
                                                imageVector = if (isPublished) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = if (isPublished) "লুকান" else "প্রকাশ করুন"
                                            )
                                        }
                                        IconButton(onClick = { editingBlock = block }) {
                                            Icon(Icons.Default.Edit, contentDescription = "সম্পাদনা")
                                        }
                                        IconButton(onClick = { onDeleteSection(block.id) }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "মুছুন",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                CollegeHeaderThemeEditor(
                    config = config,
                    onSaveProfile = onSaveProfile,
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 760.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }

            else -> {
                CollegeStatsContactEditor(
                    config = config,
                    onSaveProfile = onSaveProfile,
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 760.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }

    editingBlock?.let { initialBlock ->
        CollegeSectionBlockDialog(
            initialBlock = initialBlock,
            onDismiss = { editingBlock = null },
            onSave = { updated ->
                onSaveSection(updated)
                editingBlock = null
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CollegeHeaderThemeEditor(
    config: CollegeProfileConfig,
    onSaveProfile: (CollegeProfileConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    var collegeNameBn by remember(config.collegeNameBn) { mutableStateOf(config.collegeNameBn) }
    var collegeNameEn by remember(config.collegeNameEn) { mutableStateOf(config.collegeNameEn) }
    var locationText by remember(config.locationText) { mutableStateOf(config.locationText) }
    var affiliationText by remember(config.affiliationText) { mutableStateOf(config.affiliationText) }
    var collegeCode by remember(config.collegeCode) { mutableStateOf(config.collegeCode) }
    var mottoText by remember(config.mottoText) { mutableStateOf(config.mottoText) }
    var noticeTicker by remember(config.noticeTicker) { mutableStateOf(config.noticeTicker) }
    var heroThemePreset by remember(config.heroThemePreset) { mutableStateOf(config.heroThemePreset) }
    var layoutMode by remember(config.layoutMode) { mutableStateOf(config.layoutMode) }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader(
                        title = "কলেজ ব্যানার, থিম ও লেআউট",
                        subtitle = "হেডার ব্যানারের রঙ, কার্ড সাজানোর স্টাইল ও মূল তথ্য পরিবর্তন করুন"
                    )

                    Text("ব্যানারের রঙের থিম (Hero Theme)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CollegeSeedCatalog.heroThemePresets.forEach { (key, label) ->
                            FilterChip(
                                selected = heroThemePreset == key,
                                onClick = { heroThemePreset = key },
                                label = { Text(label) }
                            )
                        }
                    }

                    Text("কার্ড প্রদর্শনের ধরন (Layout Style)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CollegeSeedCatalog.layoutModes.forEach { (key, label) ->
                            FilterChip(
                                selected = layoutMode == key,
                                onClick = { layoutMode = key },
                                label = { Text(label) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = collegeNameBn,
                        onValueChange = { collegeNameBn = it },
                        label = { Text("কলেজের নাম (বাংলা)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = collegeNameEn,
                        onValueChange = { collegeNameEn = it },
                        label = { Text("College Name (English)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = locationText,
                        onValueChange = { locationText = it },
                        label = { Text("ঠিকানা / অবস্থান") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = affiliationText,
                        onValueChange = { affiliationText = it },
                        label = { Text("অধিভুক্তি ব্যাজ (e.g., জাতীয় বিশ্ববিদ্যালয় অধিভুক্ত)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = collegeCode,
                        onValueChange = { collegeCode = it },
                        label = { Text("কলেজ কোড / EIIN (ঐচ্ছিক)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = mottoText,
                        onValueChange = { mottoText = it },
                        label = { Text("কলেজের মূলবাণী / সাবটাইটেল") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = noticeTicker,
                        onValueChange = { noticeTicker = it },
                        label = { Text("হাইলাইট নোটিশ ব্যানার (Notice Ticker)") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            onSaveProfile(
                                config.copy(
                                    collegeNameBn = collegeNameBn.trim(),
                                    collegeNameEn = collegeNameEn.trim(),
                                    locationText = locationText.trim(),
                                    affiliationText = affiliationText.trim(),
                                    collegeCode = collegeCode.trim(),
                                    mottoText = mottoText.trim(),
                                    noticeTicker = noticeTicker.trim(),
                                    heroThemePreset = heroThemePreset,
                                    layoutMode = layoutMode
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("admin_college_save_theme_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("থিম ও হেডার সংরক্ষণ করুন")
                    }
                }
            }
        }
    }
}

@Composable
private fun CollegeStatsContactEditor(
    config: CollegeProfileConfig,
    onSaveProfile: (CollegeProfileConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    var showQuickInfoStrip by remember(config.showQuickInfoStrip) { mutableStateOf(config.showQuickInfoStrip) }
    var stat1Label by remember(config.quickStat1Label) { mutableStateOf(config.quickStat1Label) }
    var stat1Value by remember(config.quickStat1Value) { mutableStateOf(config.quickStat1Value) }
    var stat2Label by remember(config.quickStat2Label) { mutableStateOf(config.quickStat2Label) }
    var stat2Value by remember(config.quickStat2Value) { mutableStateOf(config.quickStat2Value) }
    var stat3Label by remember(config.quickStat3Label) { mutableStateOf(config.quickStat3Label) }
    var stat3Value by remember(config.quickStat3Value) { mutableStateOf(config.quickStat3Value) }
    var officeHours by remember(config.officeHours) { mutableStateOf(config.officeHours) }
    var contactPhone by remember(config.contactPhone) { mutableStateOf(config.contactPhone) }
    var contactEmail by remember(config.contactEmail) { mutableStateOf(config.contactEmail) }
    var websiteUrl by remember(config.websiteUrl) { mutableStateOf(config.websiteUrl) }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader(
                        title = "তথ্য কার্ড ও যোগাযোগ মাধ্যম",
                        subtitle = "কলেজের দ্রুত তথ্য চিপস, অফিস সময়, ফোন, ইমেইল ও ওয়েবসাইট সেট করুন"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("কুইক স্ট্যাটাস স্ট্রিপ দেখান", style = MaterialTheme.typography.titleSmall)
                        Switch(checked = showQuickInfoStrip, onCheckedChange = { showQuickInfoStrip = it })
                    }

                    if (showQuickInfoStrip) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = stat1Label, onValueChange = { stat1Label = it }, label = { Text("লেবেল ১") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = stat1Value, onValueChange = { stat1Value = it }, label = { Text("মান ১") }, modifier = Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = stat2Label, onValueChange = { stat2Label = it }, label = { Text("লেবেল ২") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = stat2Value, onValueChange = { stat2Value = it }, label = { Text("মান ২") }, modifier = Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = stat3Label, onValueChange = { stat3Label = it }, label = { Text("লেবেল ৩") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = stat3Value, onValueChange = { stat3Value = it }, label = { Text("মান ৩") }, modifier = Modifier.weight(1f))
                        }
                    }

                    OutlinedTextField(
                        value = officeHours,
                        onValueChange = { officeHours = it },
                        label = { Text("অফিস ও ক্লাসের সময়সূচি") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        label = { Text("কলেজ হেল্পলাইন / ফোন নম্বর") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = contactEmail,
                        onValueChange = { contactEmail = it },
                        label = { Text("অফিশিয়াল ইমেইল") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = websiteUrl,
                        onValueChange = { websiteUrl = it },
                        label = { Text("ওয়েবসাইট / ফেসবুক পেজ লিংক") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            onSaveProfile(
                                config.copy(
                                    showQuickInfoStrip = showQuickInfoStrip,
                                    quickStat1Label = stat1Label.trim(),
                                    quickStat1Value = stat1Value.trim(),
                                    quickStat2Label = stat2Label.trim(),
                                    quickStat2Value = stat2Value.trim(),
                                    quickStat3Label = stat3Label.trim(),
                                    quickStat3Value = stat3Value.trim(),
                                    officeHours = officeHours.trim(),
                                    contactPhone = contactPhone.trim(),
                                    contactEmail = contactEmail.trim(),
                                    websiteUrl = websiteUrl.trim()
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("admin_college_save_contact_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("তথ্য ও যোগাযোগ সংরক্ষণ করুন")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CollegeSectionBlockDialog(
    initialBlock: CollegeSectionBlock,
    onDismiss: () -> Unit,
    onSave: (CollegeSectionBlock) -> Unit
) {
    var blockType by remember(initialBlock) { mutableStateOf(initialBlock.blockType) }
    var title by remember(initialBlock) { mutableStateOf(initialBlock.title) }
    var subtitle by remember(initialBlock) { mutableStateOf(initialBlock.subtitle) }
    var body by remember(initialBlock) { mutableStateOf(initialBlock.body) }
    var badgeText by remember(initialBlock) { mutableStateOf(initialBlock.badgeText) }
    var iconKey by remember(initialBlock) { mutableStateOf(initialBlock.iconKey) }
    var accentStyle by remember(initialBlock) { mutableStateOf(initialBlock.accentStyle) }
    var layoutSpan by remember(initialBlock) { mutableStateOf(initialBlock.layoutSpan) }
    var actionLabel by remember(initialBlock) { mutableStateOf(initialBlock.actionLabel) }
    var actionUrl by remember(initialBlock) { mutableStateOf(initialBlock.actionUrl) }
    var pinned by remember(initialBlock) { mutableStateOf(initialBlock.pinned) }
    var isPublished by remember(initialBlock) { mutableStateOf(initialBlock.status == "published") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialBlock.id.isBlank()) "নতুন কলেজ কার্ড যোগ করুন" else "কার্ড সম্পাদনা করুন",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("কার্ডের ধরন", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(CollegeSeedCatalog.blockTypeOptions, key = { it.first }) { (key, label) ->
                        FilterChip(selected = blockType == key, onClick = { blockType = key }, label = { Text(label) })
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("শিরোনাম *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("সাবটাইটেল (ঐচ্ছিক)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = badgeText,
                    onValueChange = { badgeText = it },
                    label = { Text("ব্যাজ / ট্যাগ (যেমন: জরুরি, রুটিন)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("বিস্তারিত বিবরণ") },
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("রঙ ও আইকন", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(CollegeSeedCatalog.accentOptions, key = { it.first }) { (key, label) ->
                        FilterChip(selected = accentStyle == key, onClick = { accentStyle = key }, label = { Text(label) })
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(CollegeSeedCatalog.iconOptions, key = { it.first }) { (key, label) ->
                        FilterChip(selected = iconKey == key, onClick = { iconKey = key }, label = { Text(label) })
                    }
                }

                Text("কার্ডের প্রস্থ (Width)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = layoutSpan == "full", onClick = { layoutSpan = "full" }, label = { Text("Full Width") })
                    FilterChip(selected = layoutSpan == "half", onClick = { layoutSpan = "half" }, label = { Text("Half Width (২ কলাম)") })
                }

                OutlinedTextField(
                    value = actionLabel,
                    onValueChange = { actionLabel = it },
                    label = { Text("বাটনের লেখা (ঐচ্ছিক, যেমন: নোটিশ দেখুন)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = actionUrl,
                    onValueChange = { actionUrl = it },
                    label = { Text("বাটন লিংক / ফোন নম্বর (ঐচ্ছিক)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("উপরে পিন করুন (Pinned)")
                    Switch(checked = pinned, onCheckedChange = { pinned = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("শিক্ষার্থীদের জন্য প্রকাশ করুন")
                    Switch(checked = isPublished, onCheckedChange = { isPublished = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        initialBlock.copy(
                            blockType = blockType,
                            title = title.trim(),
                            subtitle = subtitle.trim(),
                            body = body.trim(),
                            badgeText = badgeText.trim(),
                            iconKey = iconKey,
                            accentStyle = accentStyle,
                            layoutSpan = layoutSpan,
                            actionLabel = actionLabel.trim(),
                            actionUrl = actionUrl.trim(),
                            pinned = pinned,
                            status = if (isPublished) "published" else "draft"
                        )
                    )
                },
                enabled = title.trim().isNotBlank()
            ) {
                Text("সংরক্ষণ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
