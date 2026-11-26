package com.example.ui.screens.college

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CollegeProfileConfig
import com.example.data.model.CollegeSectionBlock
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HonoursTopBar
import com.example.ui.theme.CrimsonContainerLight
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Gold200
import com.example.ui.theme.Gold50
import com.example.ui.theme.Gold500
import com.example.ui.theme.Gold600
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.Teal100
import com.example.ui.theme.Teal600

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SujatAliCollegeScreen(
    config: CollegeProfileConfig,
    sections: List<CollegeSectionBlock>,
    isBackendAdmin: Boolean,
    onBackClick: () -> Unit,
    onCustomizeClick: () -> Unit
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("all") }

    val filterOptions = remember {
        listOf(
            "all" to "সকল তথ্য",
            "notice" to "নোটিশ",
            "routine" to "রুটিন",
            "department" to "বিভাগ",
            "faculty" to "শিক্ষক",
            "principal" to "প্রশাসন",
            "admission" to "ভর্তি ও হেল্প"
        )
    }

    val filteredSections = remember(sections, selectedFilter) {
        if (selectedFilter == "all") {
            sections
        } else {
            sections.filter { it.blockType == selectedFilter }
        }
    }

    val groupedRows = remember(filteredSections, config.layoutMode) {
        buildSectionRows(filteredSections, config.layoutMode)
    }

    fun launchAction(rawTarget: String) {
        val target = rawTarget.trim()
        if (target.isBlank()) return
        val formattedUri = when {
            target.startsWith("http://", ignoreCase = true) ||
                target.startsWith("https://", ignoreCase = true) ||
                target.startsWith("tel:", ignoreCase = true) ||
                target.startsWith("mailto:", ignoreCase = true) -> target
            target.contains("@") -> "mailto:$target"
            target.all { it.isDigit() || it == '+' || it == '-' || it == ' ' } -> "tel:${target.replace(" ", "")}"
            else -> "https://$target"
        }
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formattedUri)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HonoursTopBar(
            title = "সুজাত আলী অনার্স কলেজ",
            subtitle = config.collegeNameEn.ifBlank { "Sujat Ali Honours College, Pingna" },
            onBackClick = onBackClick,
            actions = {
                if (isBackendAdmin) {
                    IconButton(
                        onClick = onCustomizeClick,
                        modifier = Modifier.testTag("college_admin_edit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "কলেজ ইন্টারফেস সাজান"
                        )
                    }
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
                .align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero Banner
            item {
                CollegeHeroBanner(
                    config = config,
                    isBackendAdmin = isBackendAdmin,
                    onCustomizeClick = onCustomizeClick
                )
            }

            // Highlighted Notice Ticker
            if (config.noticeTicker.isNotBlank()) {
                item {
                    Surface(
                        color = Gold50,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Gold500.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("college_notice_ticker")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                color = Gold500.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Campaign,
                                        contentDescription = null,
                                        tint = Gold600,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Text(
                                text = config.noticeTicker,
                                style = MaterialTheme.typography.bodySmall,
                                color = Navy900,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Quick Info Strip
            if (config.showQuickInfoStrip) {
                val stats = listOfNotNull(
                    config.quickStat1Label.takeIf { it.isNotBlank() }?.let { it to config.quickStat1Value },
                    config.quickStat2Label.takeIf { it.isNotBlank() }?.let { it to config.quickStat2Value },
                    config.quickStat3Label.takeIf { it.isNotBlank() }?.let { it to config.quickStat3Value }
                )
                if (stats.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("college_quick_stats_row"),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            stats.forEach { (label, value) ->
                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = value.ifBlank { "—" },
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Contact & Office Hours Bar
            val hasContactActions = config.contactPhone.isNotBlank() ||
                config.contactEmail.isNotBlank() ||
                config.websiteUrl.isNotBlank() ||
                config.officeHours.isNotBlank()
            if (hasContactActions) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (config.officeHours.isNotBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = config.officeHours,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            if (config.contactPhone.isNotBlank() || config.contactEmail.isNotBlank() || config.websiteUrl.isNotBlank()) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (config.contactPhone.isNotBlank()) {
                                        OutlinedButton(
                                            onClick = { launchAction("tel:${config.contactPhone}") },
                                            modifier = Modifier.minimumInteractiveComponentSize()
                                        ) {
                                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(config.contactPhone)
                                        }
                                    }
                                    if (config.contactEmail.isNotBlank()) {
                                        OutlinedButton(
                                            onClick = { launchAction("mailto:${config.contactEmail}") },
                                            modifier = Modifier.minimumInteractiveComponentSize()
                                        ) {
                                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("ইমেইল")
                                        }
                                    }
                                    if (config.websiteUrl.isNotBlank()) {
                                        OutlinedButton(
                                            onClick = { launchAction(config.websiteUrl) },
                                            modifier = Modifier.minimumInteractiveComponentSize()
                                        ) {
                                            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("ওয়েবসাইট")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Filter Chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filterOptions, key = { it.first }) { (key, label) ->
                        FilterChip(
                            selected = selectedFilter == key,
                            onClick = { selectedFilter = key },
                            label = { Text(label) }
                        )
                    }
                }
            }

            // Dynamic Admin-Arranged Sections
            if (filteredSections.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.AccountBalance,
                        title = "এই ক্যাটাগরিতে কোনো কার্ড নেই",
                        message = if (isBackendAdmin) {
                            "উপরের সাজান বাটনে ক্লিক করে নতুন কার্ড বা তথ্য যোগ করুন।"
                        } else {
                            "অন্য ক্যাটাগরি বেছে নিন অথবা অ্যাডমিনের নতুন আপডেটের জন্য অপেক্ষা করুন।"
                        }
                    )
                }
            } else {
                items(groupedRows.size, key = { idx -> groupedRows[idx].joinToString("_") { it.id } }) { idx ->
                    val rowBlocks = groupedRows[idx]
                    if (config.layoutMode == "compact" && rowBlocks.size == 1) {
                        CollegeCompactCard(
                            block = rowBlocks.first(),
                            onActionClick = { launchAction(it) }
                        )
                    } else if (rowBlocks.size == 1) {
                        CollegeSectionCard(
                            block = rowBlocks.first(),
                            isCompactGrid = false,
                            onActionClick = { launchAction(it) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowBlocks.forEach { block ->
                                CollegeSectionCard(
                                    block = block,
                                    isCompactGrid = true,
                                    onActionClick = { launchAction(it) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowBlocks.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CollegeHeroBanner(
    config: CollegeProfileConfig,
    isBackendAdmin: Boolean,
    onCustomizeClick: () -> Unit
) {
    val gradientColors = when (config.heroThemePreset) {
        "emerald_teal" -> listOf(Color(0xFF064E3B), Color(0xFF0F766E))
        "royal_indigo" -> listOf(Color(0xFF1E1B4B), Color(0xFF3730A3))
        "crimson_gold" -> listOf(Color(0xFF7F1D1D), Color(0xFF991B1B))
        else -> listOf(Navy900, Navy700)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.linearGradient(gradientColors))
            .padding(20.dp)
            .testTag("college_hero_banner")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Gold500.copy(alpha = 0.2f),
                    contentColor = Gold200,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Gold500.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(
                            text = config.affiliationText.ifBlank { "জাতীয় বিশ্ববিদ্যালয় অধিভুক্ত" },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (isBackendAdmin) {
                    Surface(
                        color = Color.White.copy(alpha = 0.16f),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .clickable(onClick = onCustomizeClick)
                            .testTag("college_hero_customize_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(15.dp))
                            Text("সাজান", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Text(
                text = config.collegeNameBn,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = config.collegeNameEn,
                style = MaterialTheme.typography.titleSmall,
                color = Gold200,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(16.dp))
                Text(
                    text = config.locationText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            if (config.collegeCode.isNotBlank()) {
                Text(
                    text = config.collegeCode,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.82f)
                )
            }

            if (config.mottoText.isNotBlank()) {
                Text(
                    text = config.mottoText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.92f)
                )
            }
        }
    }
}

@Composable
private fun CollegeSectionCard(
    block: CollegeSectionBlock,
    isCompactGrid: Boolean,
    onActionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = resolveBlockAccent(block.accentStyle)
    Card(
        modifier = modifier.testTag("college_section_card_${block.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, palette.badgeContainer.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = palette.badgeContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = resolveBlockIcon(block.iconKey),
                                contentDescription = null,
                                tint = palette.iconTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    if (block.badgeText.isNotBlank()) {
                        Surface(
                            color = palette.badgeContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = block.badgeText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = palette.badgeText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                if (block.pinned) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        tint = palette.iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = block.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = if (isCompactGrid) 2 else 3,
                overflow = TextOverflow.Ellipsis
            )

            if (block.subtitle.isNotBlank()) {
                Text(
                    text = block.subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.iconTint,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (block.body.isNotBlank()) {
                Text(
                    text = block.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (isCompactGrid) 5 else 16,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (block.actionLabel.isNotBlank() && block.actionUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Button(
                    onClick = { onActionClick(block.actionUrl) },
                    colors = ButtonDefaults.buttonColors(containerColor = palette.iconTint),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                ) {
                    Text(block.actionLabel, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun CollegeCompactCard(
    block: CollegeSectionBlock,
    onActionClick: (String) -> Unit
) {
    val palette = resolveBlockAccent(block.accentStyle)
    var expanded by remember(block.id) { mutableStateOf(block.pinned) }

    Card(
        onClick = { expanded = !expanded },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("college_compact_card_${block.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = palette.badgeContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = resolveBlockIcon(block.iconKey),
                            contentDescription = null,
                            tint = palette.iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(block.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    if (block.subtitle.isNotBlank()) {
                        Text(block.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (block.badgeText.isNotBlank()) {
                    Surface(color = palette.badgeContainer, shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = block.badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.badgeText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            if (expanded && block.body.isNotBlank()) {
                Text(
                    text = block.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (expanded && block.actionLabel.isNotBlank() && block.actionUrl.isNotBlank()) {
                OutlinedButton(
                    onClick = { onActionClick(block.actionUrl) },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text(block.actionLabel)
                }
            }
        }
    }
}

private data class BlockAccentPalette(
    val badgeContainer: Color,
    val badgeText: Color,
    val iconTint: Color
)

@Composable
private fun resolveBlockAccent(accentStyle: String): BlockAccentPalette {
    return when (accentStyle) {
        "gold" -> BlockAccentPalette(
            badgeContainer = Gold50,
            badgeText = Color(0xFF92400E),
            iconTint = Gold600
        )
        "emerald" -> BlockAccentPalette(
            badgeContainer = Emerald100,
            badgeText = Color(0xFF065F46),
            iconTint = Emerald600
        )
        "teal" -> BlockAccentPalette(
            badgeContainer = Teal100,
            badgeText = Color(0xFF115E59),
            iconTint = Teal600
        )
        "crimson" -> BlockAccentPalette(
            badgeContainer = CrimsonContainerLight,
            badgeText = Color(0xFF991B1B),
            iconTint = CrimsonError
        )
        else -> BlockAccentPalette(
            badgeContainer = MaterialTheme.colorScheme.primaryContainer,
            badgeText = MaterialTheme.colorScheme.onPrimaryContainer,
            iconTint = MaterialTheme.colorScheme.primary
        )
    }
}

private fun resolveBlockIcon(iconKey: String): ImageVector {
    return when (iconKey) {
        "campaign" -> Icons.Default.Campaign
        "schedule" -> Icons.Default.Schedule
        "groups" -> Icons.Default.Groups
        "book" -> Icons.Default.MenuBook
        "info" -> Icons.Default.Info
        "phone" -> Icons.Default.Call
        "link" -> Icons.Default.Link
        "star" -> Icons.Default.Star
        else -> Icons.Default.School
    }
}

private fun buildSectionRows(
    sections: List<CollegeSectionBlock>,
    layoutMode: String
): List<List<CollegeSectionBlock>> {
    if (layoutMode == "compact") {
        return sections.map { listOf(it) }
    }
    val rows = mutableListOf<List<CollegeSectionBlock>>()
    var pendingHalf: CollegeSectionBlock? = null

    for (block in sections) {
        val forceHalf = layoutMode == "grid" && !block.pinned && block.layoutSpan != "full"
        val isHalf = (block.layoutSpan == "half" || forceHalf) && !block.pinned
        if (isHalf) {
            if (pendingHalf == null) {
                pendingHalf = block
            } else {
                rows.add(listOf(pendingHalf, block))
                pendingHalf = null
            }
        } else {
            if (pendingHalf != null) {
                rows.add(listOf(pendingHalf))
                pendingHalf = null
            }
            rows.add(listOf(block))
        }
    }
    if (pendingHalf != null) {
        rows.add(listOf(pendingHalf))
    }
    return rows
}
