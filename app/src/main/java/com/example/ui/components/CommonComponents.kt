package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.theme.Gold500
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy900

@Composable
fun OfflineStatusBanner(isOnline: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = !isOnline,
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = modifier
    ) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("offline_status_banner")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = "Offline status",
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Offline Mode • Cached notes, bookmarks & downloaded PDFs remain available.",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HonoursTopBar(
    title: String,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        navigationIcon = {
            when {
                onBackClick != null -> {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("top_bar_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                }
                onMenuClick != null -> {
                    IconButton(
                        onClick = onMenuClick,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("top_bar_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open navigation menu"
                        )
                    }
                }
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (actionLabel != null && onActionClick != null) {
            TextButton(
                onClick = onActionClick,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun GradientFeatureBanner(
    badge: String,
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Navy900, Navy700)
                )
            )
            .clickable(onClick = onClick)
            .padding(20.dp)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = Gold500.copy(alpha = 0.2f),
                    contentColor = Gold500,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
            Surface(
                color = Color.White.copy(alpha = 0.14f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Gold500,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    message: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (actionLabel != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onActionClick) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
fun ErrorStateCard(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            if (onRetry != null) {
                IconButton(onClick = onRetry) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Retry",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingStateBox(label: String = "Loading...", modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(strokeWidth = 3.dp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ReportIssueDialog(
    title: String = "Report Incorrect Content / AI Answer",
    onDismiss: () -> Unit,
    onSubmit: (reason: String, details: String) -> Unit
) {
    var reason by remember { mutableStateOf("Inaccurate academic information") }
    var details by remember { mutableStateOf("") }
    val presetReasons = listOf(
        "Inaccurate academic information",
        "Syllabus or course code mismatch",
        "Broken or unreadable PDF",
        "Formatting or Bengali spelling issue"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Info, contentDescription = null) },
        title = { Text(title, style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                presetReasons.forEach { preset ->
                    Surface(
                        color = if (reason == preset) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { reason = preset }
                    ) {
                        Text(
                            text = preset,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (reason == preset) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Additional details (optional)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(reason, details) },
                modifier = Modifier.testTag("submit_report_button")
            ) {
                Text("Submit Report")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
