package com.example.ui.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.AdminLockoutState
import com.example.data.catalog.AcademicSeedCatalog
import com.example.data.model.AdminLog
import com.example.data.model.AdminUserUsageSummary
import com.example.data.model.AiRemoteConfig
import com.example.data.model.Announcement
import com.example.data.model.AuthPolicyConfig
import com.example.data.model.Chapter
import com.example.data.model.Department
import com.example.data.model.DeveloperProfileConfig
import com.example.data.model.PdfResource
import com.example.data.model.StudyQuestion
import com.example.data.model.Subject
import com.example.data.model.UiState
import com.example.data.model.UserReport
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ErrorStateCard
import com.example.ui.components.HonoursTopBar
import com.example.ui.components.LoadingStateBox
import com.example.ui.components.SectionHeader
import kotlinx.coroutines.delay

@Composable
fun AdminPasswordScreen(
    isBackendAdminAccount: Boolean,
    hasPinConfigured: Boolean?,
    lockoutState: AdminLockoutState,
    isVerifying: Boolean,
    authError: String?,
    onBack: () -> Unit,
    onRefreshStatus: () -> Unit,
    onVerifyPin: (String) -> Unit,
    onInitializePin: (String) -> Unit,
    onClearError: () -> Unit = {}
) {
    var pinInput by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }
    var resetMode by remember { mutableStateOf(false) }
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val isSettingNewPin = (hasPinConfigured == false) || (isBackendAdminAccount && resetMode)

    LaunchedEffect(Unit) {
        onRefreshStatus()
    }

    LaunchedEffect(lockoutState.lockoutUntilMs) {
        while (lockoutState.lockoutUntilMs > System.currentTimeMillis()) {
            nowMs = System.currentTimeMillis()
            delay(1000L)
        }
        nowMs = System.currentTimeMillis()
    }

    val remainingLockoutSec = ((lockoutState.lockoutUntilMs - nowMs) / 1000L).coerceAtLeast(0L)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HonoursTopBar(
            title = "কনটেন্ট ম্যানেজমেন্ট",
            subtitle = "Secure Admin Access Gate",
            onBackClick = onBack
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Admin Lock",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Text(
                        text = "অ্যাডমিনিস্ট্রেটর যাচাইকরণ",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    if (!isBackendAdminAccount) {
                        ErrorStateCard(
                            message = "এই Google অ্যাকাউন্টটি নির্ধারিত এডমিন তালিকাভুক্ত নয়। শুধুমাত্র অনুমোদিত এডমিন অ্যাকাউন্ট দিয়ে কনটেন্ট ম্যানেজমেন্ট ব্যবহার করা যাবে।"
                        )
                    } else if (isSettingNewPin) {
                        Text(
                            text = if (resetMode) {
                                "যাচাইকৃত অ্যাডমিন অ্যাকাউন্ট থেকে কমপক্ষে ৪ সংখ্যার নতুন অ্যাডমিন পাসওয়ার্ড সেট করুন।"
                            } else {
                                "প্রথমবার সেটআপ: কমপক্ষে ৪ সংখ্যার আপনার নিজের অ্যাডমিন পিন/পাসওয়ার্ড দিয়ে আনলক করুন।"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "কনটেন্ট ম্যানেজমেন্ট প্যানেলে প্রবেশ করতে অ্যাডমিন পাসওয়ার্ড লিখুন। ভুলে গেলে নিচে থেকে রিসেট করতে পারবেন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (remainingLockoutSec > 0) {
                        ErrorStateCard(message = "একাধিক ভুল প্রচেষ্টার কারণে লক করা হয়েছে। অপেক্ষা করুন: ${remainingLockoutSec}s")
                    } else if (!authError.isNullOrBlank()) {
                        ErrorStateCard(message = authError)
                    }

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { pinInput = it },
                        label = {
                            Text(if (isSettingNewPin) "নতুন অ্যাডমিন পাসওয়ার্ড (কমপক্ষে ৪ সংখ্যা)" else "অ্যাডমিন পাসওয়ার্ড")
                        },
                        singleLine = true,
                        visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        trailingIcon = {
                            IconButton(onClick = { showPin = !showPin }) {
                                Icon(
                                    imageVector = if (showPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_password_input")
                    )

                    Button(
                        onClick = {
                            if (isSettingNewPin) {
                                onInitializePin(pinInput.trim())
                            } else {
                                onVerifyPin(pinInput.trim())
                            }
                        },
                        enabled = !isVerifying && remainingLockoutSec == 0L &&
                            pinInput.trim().length >= (if (isSettingNewPin) 4 else 1) &&
                            isBackendAdminAccount,
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("admin_unlock_button")
                    ) {
                        if (isVerifying) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("যাচাই করা হচ্ছে...")
                        } else {
                            Text(if (isSettingNewPin) "নতুন পিন সেট ও আনলক করুন" else "আনলক করুন")
                        }
                    }

                    if (isBackendAdminAccount && hasPinConfigured != false) {
                        TextButton(
                            onClick = {
                                resetMode = !resetMode
                                pinInput = ""
                                onClearError()
                            },
                            modifier = Modifier.testTag("admin_reset_pin_toggle")
                        ) {
                            Text(
                                if (resetMode) "আগের পাসওয়ার্ড দিয়ে আনলক করুন"
                                else "পাসওয়ার্ড ভুলে গেছেন? নতুন পাসওয়ার্ড সেট করুন"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDashboardScreen(
    departmentsCount: Int,
    subjectsCount: Int,
    pdfsCount: Int,
    openReportsCount: Int,
    remoteConfig: AiRemoteConfig,
    adminLogs: List<AdminLog>,
    onBack: () -> Unit,
    onLockPanel: () -> Unit,
    onSeedCatalog: () -> Unit,
    onSaveAiConfig: (AiRemoteConfig) -> Unit,
    onNavigatePdfUpload: () -> Unit,
    onNavigateContentEditor: () -> Unit,
    onNavigateAnnouncements: () -> Unit,
    onNavigateReports: () -> Unit,
    developerProfile: DeveloperProfileConfig = DeveloperProfileConfig(),
    onSaveDeveloperProfile: (DeveloperProfileConfig) -> Unit = {},
    pendingPurchasesCount: Int = 0,
    onNavigateAiLimits: () -> Unit = {},
    onNavigateCollegeEditor: () -> Unit = {},
    userUsageSummary: AdminUserUsageSummary = AdminUserUsageSummary(),
    onSeedAllPdfs: () -> Unit = {},
    authPolicy: AuthPolicyConfig = AuthPolicyConfig(),
    onSaveAuthPolicy: (AuthPolicyConfig) -> Unit = {}
) {
    var modelName by remember(remoteConfig.modelName) { mutableStateOf(remoteConfig.modelName) }
    var sysInstructions by remember(remoteConfig.systemInstructions) { mutableStateOf(remoteConfig.systemInstructions) }
    var maintenanceMode by remember(remoteConfig.maintenanceMode) { mutableStateOf(remoteConfig.maintenanceMode) }
    var maintenanceMsg by remember(remoteConfig.maintenanceMessage) { mutableStateOf(remoteConfig.maintenanceMessage) }

    var showUserDirectory by remember { mutableStateOf(false) }
    var userSearchQuery by remember { mutableStateOf("") }
    var showEmailJsConfig by remember { mutableStateOf(false) }
    var emailJsServiceId by remember(authPolicy.emailJsServiceId) { mutableStateOf(authPolicy.emailJsServiceId) }
    var emailJsTemplateId by remember(authPolicy.emailJsTemplateId) { mutableStateOf(authPolicy.emailJsTemplateId) }
    var emailJsPublicKey by remember(authPolicy.emailJsPublicKey) { mutableStateOf(authPolicy.emailJsPublicKey) }

    var devName by remember(developerProfile.name) { mutableStateOf(developerProfile.name) }
    var devTagline by remember(developerProfile.tagline) { mutableStateOf(developerProfile.tagline) }
    var devDeptFocus by remember(developerProfile.departmentFocus) { mutableStateOf(developerProfile.departmentFocus) }
    var devBio by remember(developerProfile.bio) { mutableStateOf(developerProfile.bio) }
    var devPhone by remember(developerProfile.phone) { mutableStateOf(developerProfile.phone) }
    var devEmail by remember(developerProfile.email) { mutableStateOf(developerProfile.email) }
    var devWebsiteUrl by remember(developerProfile.websiteUrl) { mutableStateOf(developerProfile.websiteUrl) }
    var devWebsiteLabel by remember(developerProfile.websiteLabel) { mutableStateOf(developerProfile.websiteLabel) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HonoursTopBar(
            title = "Admin Dashboard",
            subtitle = "কনটেন্ট ম্যানেজমেন্ট • Verified Session",
            onBackClick = onBack,
            actions = {
                IconButton(onClick = onLockPanel, modifier = Modifier.testTag("lock_admin_button")) {
                    Icon(Icons.Default.Lock, contentDescription = "Lock Admin Panel")
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
                .align(Alignment.CenterHorizontally)
                .testTag("admin_dashboard_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AdminStatCard("বিভাগ", "$departmentsCount", Modifier.weight(1f))
                    AdminStatCard("বিষয়", "$subjectsCount", Modifier.weight(1f))
                    AdminStatCard("PDF", "$pdfsCount", Modifier.weight(1f))
                    AdminStatCard("রিপোর্ট", "$openReportsCount", Modifier.weight(1f))
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_user_usage_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.People, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            SectionHeader(
                                title = "অ্যাপ ব্যবহারকারী ও অ্যাক্টিভিটি পরিসংখ্যান",
                                subtitle = "রিয়েল-টাইম ব্যবহারকারীর সংখ্যা ও সক্রিয়তা"
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AdminStatCard(
                                label = "মোট ইউজার",
                                value = "${userUsageSummary.totalUsers}",
                                modifier = Modifier.weight(1f).testTag("admin_total_users_count")
                            )
                            AdminStatCard(
                                label = "আজ অ্যাক্টিভ",
                                value = "${userUsageSummary.activeTodayCount}",
                                modifier = Modifier.weight(1f)
                            )
                            AdminStatCard(
                                label = "৭ দিনে অ্যাক্টিভ",
                                value = "${userUsageSummary.activeLast7DaysCount}",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("নতুন ইউজার (৭ দিন)", style = MaterialTheme.typography.labelSmall)
                                    Text("+${userUsageSummary.newUsersLast7DaysCount}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("মোট AI সমাধান", style = MaterialTheme.typography.labelSmall)
                                    Text("${userUsageSummary.totalAiQueriesUsed}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        if (userUsageSummary.yearCounts.any { it.second > 0 }) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(userUsageSummary.yearCounts) { (yr, cnt) ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = "${yr}ম/য় বর্ষ: $cnt জন",
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                        if (userUsageSummary.departmentCounts.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("জনপ্রিয় বিভাগ অনুযায়ী শিক্ষার্থী", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                userUsageSummary.departmentCounts.forEach { (deptName, cnt) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(deptName, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                        Text("$cnt জন", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "জিরো-ট্রাস্ট প্রাইভেসি: ইউজারের নাম ও মেইল তালিকা শুধুমাত্র মূল অ্যাডমিন দেখতে পারেন; ব্যক্তিগত নোট ও AI চ্যাট সম্পূর্ণ সুরক্ষিত।",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(
                            onClick = { showUserDirectory = !showUserDirectory },
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("admin_user_directory_toggle")
                        ) {
                            Icon(
                                imageVector = if (showUserDirectory) Icons.Default.VisibilityOff else Icons.Default.People,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (showUserDirectory) {
                                    "ইউজার তালিকা লুকান"
                                } else {
                                    "নিবন্ধিত ইউজার তালিকা (নাম ও মেইল • ${userUsageSummary.users.size} জন)"
                                }
                            )
                        }
                        if (showUserDirectory) {
                            OutlinedTextField(
                                value = userSearchQuery,
                                onValueChange = { userSearchQuery = it },
                                label = { Text("নাম বা ইমেইল দিয়ে খুঁজুন") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_user_directory_search")
                            )
                            val filteredUsers = remember(userUsageSummary.users, userSearchQuery) {
                                val q = userSearchQuery.trim().lowercase()
                                if (q.isBlank()) userUsageSummary.users
                                else userUsageSummary.users.filter {
                                    it.displayName.lowercase().contains(q) ||
                                        it.email.lowercase().contains(q) ||
                                        it.selectedDepartmentName.lowercase().contains(q)
                                }
                            }
                            if (filteredUsers.isEmpty()) {
                                Text(
                                    text = "কোনো নিবন্ধিত ইউজার পাওয়া যায়নি।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("admin_user_directory_list"),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    filteredUsers.take(100).forEach { entry ->
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(10.dp),
                                                verticalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = entry.displayName,
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Text(
                                                        text = "AI: ${entry.aiUsageCount}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                Text(
                                                    text = entry.email.ifBlank { "ইমেইল পাওয়া যায়নি" },
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${entry.selectedDepartmentName} • ${entry.selectedYear}ম/য় বর্ষ",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                    if (filteredUsers.size > 100) {
                                        Text(
                                            text = "প্রথম ১০০ জন ইউজার দেখানো হচ্ছে — বাকিদের দেখতে উপরে সার্চ করুন।",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = "লগইনে ৫ সংখ্যার ইমেইল ভেরিফিকেশন কোড",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "চালু থাকলে প্রতিবার নতুন লগইনের সময় ইউজারের ইমেইলে ৫ সংখ্যার OTP কোড যাবে",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = authPolicy.requireEmailOtp,
                                onCheckedChange = { enabled ->
                                    onSaveAuthPolicy(
                                        authPolicy.copy(
                                            requireEmailOtp = enabled,
                                            emailJsServiceId = emailJsServiceId.trim(),
                                            emailJsTemplateId = emailJsTemplateId.trim(),
                                            emailJsPublicKey = emailJsPublicKey.trim()
                                        )
                                    )
                                },
                                modifier = Modifier.testTag("admin_email_otp_policy_switch")
                            )
                        }
                        Text(
                            text = if (authPolicy.hasEmailJsConfigured) {
                                "স্ট্যাটাস: EmailJS সংযুক্ত আছে — ৫ সংখ্যার ইমেইল OTP সরাসরি কাজ করবে।"
                            } else {
                                "টিপস: সার্ভার/Cloud Function ছাড়াই ৫ সংখ্যার ইমেইল কোড চালু করতে নিচে ফ্রি EmailJS আইডি যুক্ত করুন।"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (authPolicy.hasEmailJsConfigured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = { showEmailJsConfig = !showEmailJsConfig },
                            modifier = Modifier.testTag("admin_emailjs_toggle")
                        ) {
                            Text(
                                if (showEmailJsConfig) "EmailJS সেটআপ লুকান"
                                else "EmailJS ফ্রি সেটআপ (ক্লাউড ফাংশন ছাড়াই OTP)"
                            )
                        }
                        if (showEmailJsConfig) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "emailjs.com-এ ফ্রি অ্যাকাউন্ট খুলে Gmail কানেক্ট করুন এবং টেমপ্লেটে {{to_email}} ও {{otp_code}} ব্যবহার করে নিচের ৩টি তথ্য দিন:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    OutlinedTextField(
                                        value = emailJsServiceId,
                                        onValueChange = { emailJsServiceId = it },
                                        label = { Text("EmailJS Service ID") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("admin_emailjs_service_input")
                                    )
                                    OutlinedTextField(
                                        value = emailJsTemplateId,
                                        onValueChange = { emailJsTemplateId = it },
                                        label = { Text("EmailJS Template ID") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("admin_emailjs_template_input")
                                    )
                                    OutlinedTextField(
                                        value = emailJsPublicKey,
                                        onValueChange = { emailJsPublicKey = it },
                                        label = { Text("EmailJS Public Key") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("admin_emailjs_key_input")
                                    )
                                    Button(
                                        onClick = {
                                            onSaveAuthPolicy(
                                                authPolicy.copy(
                                                    emailJsServiceId = emailJsServiceId.trim(),
                                                    emailJsTemplateId = emailJsTemplateId.trim(),
                                                    emailJsPublicKey = emailJsPublicKey.trim()
                                                )
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .minimumInteractiveComponentSize()
                                            .testTag("admin_save_auth_policy_button")
                                    ) {
                                        Text("EmailJS সেটআপ সংরক্ষণ করুন")
                                    }
                                }
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
                        SectionHeader(
                            title = "একাডেমিক ম্যানেজমেন্ট টুলস",
                            subtitle = "এখানে আপডেট করা সকল বিভাগ, সিলেবাস, PDF ও নোটিশ সবার কাছে অটো সিঙ্ক হয়"
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AdminActionCard("PDF আপলোড", Icons.Default.CloudUpload, onNavigatePdfUpload, Modifier.weight(1f), "admin_nav_pdf_upload")
                            AdminActionCard("সিলেবাস ও প্রশ্ন", Icons.Default.School, onNavigateContentEditor, Modifier.weight(1f), "admin_nav_content_editor")
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AdminActionCard("ঘোষণা / নোটিশ", Icons.Default.Campaign, onNavigateAnnouncements, Modifier.weight(1f), "admin_nav_announcements")
                            AdminActionCard("ইউজার রিপোর্ট ($openReportsCount)", Icons.Default.Flag, onNavigateReports, Modifier.weight(1f), "admin_nav_reports")
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AdminActionCard(
                                "AI লিমিট ও পেমেন্ট ($pendingPurchasesCount)",
                                Icons.Default.Payments,
                                onNavigateAiLimits,
                                Modifier.weight(1f),
                                "admin_nav_ai_limits"
                            )
                            AdminActionCard(
                                "সুজাত আলী কলেজ সাজান",
                                Icons.Default.AccountBalance,
                                onNavigateCollegeEditor,
                                Modifier.weight(1f),
                                "admin_nav_college_editor"
                            )
                        }
                        OutlinedButton(
                            onClick = onSeedCatalog,
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("admin_seed_catalog_button")
                        ) {
                            Text("ডিফল্ট ২২টি বিভাগ ও প্রারম্ভিক সিলেবাস Firestore-এ প্রকাশ করুন")
                        }
                        OutlinedButton(
                            onClick = onSeedAllPdfs,
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("admin_seed_all_pdfs_button")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("সকল বিষয়ের পিডিএফ (২২ বিভাগ) প্রকাশ করুন")
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
                        SectionHeader(title = "Firebase AI ও সিস্টেম কনফিগারেশন")
                        OutlinedTextField(
                            value = modelName,
                            onValueChange = { modelName = it },
                            label = { Text("Gemini Model ID") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = modelName == "gemini-3.8-flash",
                                onClick = { modelName = "gemini-3.8-flash" },
                                label = { Text("gemini-3.8-flash") }
                            )
                            FilterChip(
                                selected = modelName == "gemini-3.5-flash-lite",
                                onClick = { modelName = "gemini-3.5-flash-lite" },
                                label = { Text("gemini-3.5-flash-lite (Fast)") }
                            )
                        }
                        OutlinedTextField(
                            value = sysInstructions,
                            onValueChange = { sysInstructions = it },
                            label = { Text("Custom AI System Instructions (ঐচ্ছিক)") },
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("AI Maintenance Mode", style = MaterialTheme.typography.titleSmall)
                            Switch(checked = maintenanceMode, onCheckedChange = { maintenanceMode = it })
                        }
                        if (maintenanceMode) {
                            OutlinedTextField(
                                value = maintenanceMsg,
                                onValueChange = { maintenanceMsg = it },
                                label = { Text("Maintenance Message") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Button(
                            onClick = {
                                onSaveAiConfig(
                                    AiRemoteConfig(
                                        modelName = modelName.trim(),
                                        systemInstructions = sysInstructions.trim(),
                                        maintenanceMode = maintenanceMode,
                                        maintenanceMessage = maintenanceMsg.trim()
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AI কনফিগারেশন আপডেট করুন")
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
                        SectionHeader(
                            title = "ডেভেলপারের সকল তথ্য পরিবর্তন (Admin Only)",
                            subtitle = "শুধুমাত্র অ্যাডমিন এখান থেকে ডেভেলপারের নাম, নম্বর, ইমেইল, বায়ো ও ওয়েবসাইট পরিবর্তন করতে পারবেন; সাধারণ ইউজার পরিবর্তন করতে পারবে না।"
                        )
                        OutlinedTextField(
                            value = devName,
                            onValueChange = { devName = it },
                            label = { Text("ডেভেলপারের নাম (Developer Name)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = devTagline,
                            onValueChange = { devTagline = it },
                            label = { Text("প্রতিষ্ঠান / ট্যাগলাইন") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = devDeptFocus,
                            onValueChange = { devDeptFocus = it },
                            label = { Text("বিভাগ / ফোকাস") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = devPhone,
                            onValueChange = { devPhone = it },
                            label = { Text("ফোন নম্বর") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = devEmail,
                            onValueChange = { devEmail = it },
                            label = { Text("ইমেইল") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = devWebsiteUrl,
                            onValueChange = { devWebsiteUrl = it },
                            label = { Text("ওয়েবসাইট লিংক (URL)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = devWebsiteLabel,
                            onValueChange = { devWebsiteLabel = it },
                            label = { Text("ওয়েবসাইট বাটনের লেখা") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = devBio,
                            onValueChange = { devBio = it },
                            label = { Text("পরিচিতি / বায়ো") },
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                onSaveDeveloperProfile(
                                    DeveloperProfileConfig(
                                        name = devName.trim(),
                                        tagline = devTagline.trim(),
                                        departmentFocus = devDeptFocus.trim(),
                                        bio = devBio.trim(),
                                        phone = devPhone.trim(),
                                        email = devEmail.trim(),
                                        websiteUrl = devWebsiteUrl.trim(),
                                        websiteLabel = devWebsiteLabel.trim()
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("admin_save_developer_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ডেভেলপার তথ্য সংরক্ষণ করুন")
                        }
                    }
                }
            }

            if (adminLogs.isNotEmpty()) {
                item {
                    SectionHeader(title = "সাম্প্রতিক অ্যাডমিন লগ (${adminLogs.size})")
                }
                items(adminLogs.take(10), key = { it.id }) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(log.action, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(log.summary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AdminActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun AdminPdfUploadScreen(
    departments: List<Department>,
    pdfs: List<PdfResource>,
    onBack: () -> Unit,
    onUploadPdf: (PdfResource, Uri?) -> Unit,
    onDeletePdf: (String) -> Unit
) {
    var editingId by remember { mutableStateOf("") }
    var selectedDeptId by remember { mutableStateOf(departments.firstOrNull()?.id ?: "political_science") }
    var yearNumber by remember { mutableLongStateOf(1L) }
    var subjectName by remember { mutableStateOf("") }
    var courseCode by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(AcademicSeedCatalog.pdfCategories.first()) }
    var description by remember { mutableStateOf("") }
    var pdfUrl by remember { mutableStateOf("") }
    var coverUrl by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("published") }
    var pickedPdfUri by remember { mutableStateOf<Uri?>(null) }

    val pdfPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) pickedPdfUri = uri
    }
    val coverPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) coverUrl = uri.toString()
    }

    val selectedDept = departments.firstOrNull { it.id == selectedDeptId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HonoursTopBar(title = "PDF আপলোড ও ব্যবস্থাপনা", subtitle = "সকল বিভাগের PDF লাইব্রেরি", onBackClick = onBack)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
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
                        SectionHeader(title = if (editingId.isBlank()) "নতুন PDF যোগ করুন" else "PDF সম্পাদনা করুন")
                        Text("বিভাগ নির্বাচন করুন", style = MaterialTheme.typography.labelMedium)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(departments, key = { it.id }) { dept ->
                                FilterChip(
                                    selected = selectedDeptId == dept.id,
                                    onClick = { selectedDeptId = dept.id },
                                    label = { Text(dept.nameBn) }
                                )
                            }
                        }
                        Text("অনার্স বর্ষ", style = MaterialTheme.typography.labelMedium)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listOf(1L, 2L, 3L, 4L)) { yr ->
                                FilterChip(selected = yearNumber == yr, onClick = { yearNumber = yr }, label = { Text("Year $yr") })
                            }
                        }
                        Text("ক্যাটাগরি", style = MaterialTheme.typography.labelMedium)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(AcademicSeedCatalog.pdfCategories) { cat ->
                                FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat) })
                            }
                        }
                        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("PDF শিরোনাম *") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = subjectName, onValueChange = { subjectName = it }, label = { Text("বিষয়ের নাম") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = courseCode, onValueChange = { courseCode = it }, label = { Text("কোর্স কোড (ঐচ্ছিক)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("বিবরণ") }, maxLines = 3, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = pdfUrl, onValueChange = { pdfUrl = it }, label = { Text("PDF HTTPS URL (Firebase Storage / Direct Link)") }, modifier = Modifier.fillMaxWidth())

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { pdfPicker.launch(arrayOf("application/pdf")) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (pickedPdfUri != null) "PDF নির্বাচিত ✓" else "ফাইল বাছুন")
                            }
                            OutlinedButton(
                                onClick = {
                                    coverPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("কভার ছবি")
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = status == "published", onClick = { status = "published" }, label = { Text("Published") })
                            FilterChip(selected = status == "draft", onClick = { status = "draft" }, label = { Text("Draft") })
                        }

                        Button(
                            onClick = {
                                onUploadPdf(
                                    PdfResource(
                                        id = editingId,
                                        departmentId = selectedDeptId,
                                        departmentName = selectedDept?.nameBn ?: selectedDeptId,
                                        yearNumber = yearNumber,
                                        subjectName = subjectName.trim(),
                                        courseCode = courseCode.trim(),
                                        title = title.trim(),
                                        category = category,
                                        description = description.trim(),
                                        pdfUrl = pdfUrl.trim(),
                                        coverImageUrl = coverUrl.trim(),
                                        status = status
                                    ),
                                    pickedPdfUri
                                )
                                editingId = ""
                                title = ""
                                description = ""
                                pdfUrl = ""
                                pickedPdfUri = null
                            },
                            enabled = title.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("admin_save_pdf_button")
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PDF সংরক্ষণ ও প্রকাশ করুন")
                        }
                    }
                }
            }

            item {
                SectionHeader(title = "বিদ্যমান PDF তালিকা (${pdfs.size})")
            }

            items(pdfs, key = { it.id }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f, fill = false))
                                Surface(
                                    color = if (item.status == "published") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (item.status == "published") "LIVE" else "DRAFT",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${item.departmentName} • Year ${item.yearNumber} • ${item.category}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = {
                            editingId = item.id
                            selectedDeptId = item.departmentId
                            yearNumber = item.yearNumber
                            subjectName = item.subjectName
                            courseCode = item.courseCode
                            title = item.title
                            category = item.category
                            description = item.description
                            pdfUrl = item.pdfUrl
                            status = item.status
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit PDF")
                        }
                        IconButton(onClick = { onDeletePdf(item.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete PDF")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminContentEditorScreen(
    departments: List<Department>,
    subjects: List<Subject>,
    chapters: List<Chapter>,
    questions: List<StudyQuestion>,
    onBack: () -> Unit,
    onSaveDepartment: (Department) -> Unit,
    onDeleteDepartment: (String) -> Unit,
    onSaveSubject: (Subject) -> Unit,
    onDeleteSubject: (String) -> Unit,
    onSaveChapter: (Chapter) -> Unit,
    onSaveQuestion: (StudyQuestion) -> Unit,
    onDeleteChapter: (String) -> Unit = {},
    onDeleteQuestion: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("বিভাগ", "বিষয়", "অধ্যায়", "প্রশ্ন ও MCQ")

    // Tab 0 state
    var editingDeptId by remember { mutableStateOf("") }
    var deptNameBn by remember { mutableStateOf("") }
    var deptNameEn by remember { mutableStateOf("") }
    var deptCode by remember { mutableStateOf("") }
    var deptDesc by remember { mutableStateOf("") }
    var deptStatus by remember { mutableStateOf("published") }

    // Tab 1 state
    var editingSubjectId by remember { mutableStateOf("") }
    var subjDeptId by remember { mutableStateOf(departments.firstOrNull()?.id ?: "political_science") }
    var subjYear by remember { mutableLongStateOf(1L) }
    var subjCourseCode by remember { mutableStateOf("") }
    var subjTitleBn by remember { mutableStateOf("") }
    var subjTitleEn by remember { mutableStateOf("") }
    var subjNote by remember { mutableStateOf("") }
    var subjStatus by remember { mutableStateOf("published") }

    // Tab 2 state
    var editingChapterId by remember { mutableStateOf("") }
    var chapSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: "") }
    var chapNum by remember { mutableStateOf("1") }
    var chapTitleBn by remember { mutableStateOf("") }
    var chapSummary by remember { mutableStateOf("") }
    var chapDefinitions by remember { mutableStateOf("") }
    var chapShortQ by remember { mutableStateOf("") }
    var chapBroadQ by remember { mutableStateOf("") }
    var chapStatus by remember { mutableStateOf("published") }

    // Tab 3 state
    var editingQuestionId by remember { mutableStateOf("") }
    var qDeptId by remember { mutableStateOf(departments.firstOrNull()?.id ?: "political_science") }
    var qType by remember { mutableStateOf("previous_year") }
    var qYear by remember { mutableLongStateOf(1L) }
    var qSubjectName by remember { mutableStateOf("রাজনৈতিক তত্ত্ব পরিচিতি") }
    var qExamYear by remember { mutableStateOf("2024") }
    var qPrompt by remember { mutableStateOf("") }
    var qAnswerText by remember { mutableStateOf("") }
    var qOptA by remember { mutableStateOf("") }
    var qOptB by remember { mutableStateOf("") }
    var qOptC by remember { mutableStateOf("") }
    var qOptD by remember { mutableStateOf("") }
    var qCorrectIdx by remember { mutableIntStateOf(0) }
    var qStatus by remember { mutableStateOf("published") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HonoursTopBar(title = "সিলেবাস ও কনটেন্ট এডিটর", subtitle = "Auto-Sync NU Curriculum CRUD", onBackClick = onBack)
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { idx, label ->
                Tab(selected = selectedTab == idx, onClick = { selectedTab = idx }, text = { Text(label) })
            }
        }
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_live_sync_banner")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = "অটো-সিঙ্ক চালু: এখানে যেকোনো পরিবর্তন সেভ করলে সকল শিক্ষার্থীর অ্যাপে রিয়েল-টাইমে আপডেট হয়ে যাবে।",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
                .align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    item {
                        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                SectionHeader(title = if (editingDeptId.isBlank()) "নতুন বিভাগ যুক্ত করুন" else "বিভাগ সম্পাদনা করুন")
                                OutlinedTextField(value = deptNameBn, onValueChange = { deptNameBn = it }, label = { Text("বিভাগের বাংলা নাম *") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = deptNameEn, onValueChange = { deptNameEn = it }, label = { Text("English Name") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = deptCode, onValueChange = { deptCode = it }, label = { Text("Short Code (e.g., SOC)") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = deptDesc, onValueChange = { deptDesc = it }, label = { Text("বিবরণ") }, modifier = Modifier.fillMaxWidth())
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(selected = deptStatus == "published", onClick = { deptStatus = "published" }, label = { Text("Published (Live)") })
                                    FilterChip(selected = deptStatus == "draft", onClick = { deptStatus = "draft" }, label = { Text("Draft (Hidden)") })
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    if (editingDeptId.isNotBlank()) {
                                        OutlinedButton(
                                            onClick = {
                                                editingDeptId = ""; deptNameBn = ""; deptNameEn = ""; deptCode = ""; deptDesc = ""; deptStatus = "published"
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) { Text("বাতিল") }
                                    }
                                    Button(
                                        onClick = {
                                            val computedId = editingDeptId.ifBlank {
                                                deptNameEn.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
                                            }
                                            val existingOrder = departments.firstOrNull { it.id == computedId }?.sortOrder
                                            val existingStructured = departments.firstOrNull { it.id == computedId }?.hasStructuredSolution ?: false
                                            onSaveDepartment(
                                                Department(
                                                    id = computedId,
                                                    nameBn = deptNameBn.trim(),
                                                    nameEn = deptNameEn.trim(),
                                                    code = deptCode.trim(),
                                                    description = deptDesc.trim(),
                                                    hasStructuredSolution = existingStructured,
                                                    sortOrder = existingOrder ?: (departments.size + 1).toLong(),
                                                    status = deptStatus
                                                )
                                            )
                                            editingDeptId = ""; deptNameBn = ""; deptNameEn = ""; deptCode = ""; deptDesc = ""; deptStatus = "published"
                                        },
                                        enabled = deptNameBn.isNotBlank(),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (editingDeptId.isBlank()) "বিভাগ প্রকাশ করুন" else "আপডেট ও সিঙ্ক করুন")
                                    }
                                }
                            }
                        }
                    }

                    items(departments, key = { it.id }) { dept ->
                        Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("${dept.nameBn} (${dept.nameEn})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f, fill = false))
                                        Surface(
                                            color = if (dept.status == "published") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(if (dept.status == "published") "LIVE" else "DRAFT", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                    Text(dept.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = {
                                    editingDeptId = dept.id
                                    deptNameBn = dept.nameBn
                                    deptNameEn = dept.nameEn
                                    deptCode = dept.code
                                    deptDesc = dept.description
                                    deptStatus = dept.status
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit department")
                                }
                                IconButton(onClick = { onDeleteDepartment(dept.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete department")
                                }
                            }
                        }
                    }
                }
                1 -> {
                    item {
                        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                SectionHeader(title = if (editingSubjectId.isBlank()) "নতুন বিষয় / কোর্স যুক্ত করুন" else "বিষয় / কোর্স সম্পাদনা করুন")
                                Text("বিভাগ নির্বাচন করুন", style = MaterialTheme.typography.labelMedium)
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.testTag("admin_subject_dept_picker")
                                ) {
                                    items(departments, key = { it.id }) { dept ->
                                        FilterChip(
                                            selected = subjDeptId == dept.id,
                                            onClick = { subjDeptId = dept.id },
                                            label = { Text(dept.nameBn) }
                                        )
                                    }
                                }
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(listOf(1L, 2L, 3L, 4L)) { yr ->
                                        FilterChip(selected = subjYear == yr, onClick = { subjYear = yr }, label = { Text("Year $yr") })
                                    }
                                }
                                OutlinedTextField(value = subjCourseCode, onValueChange = { subjCourseCode = it }, label = { Text("NU Course Code *") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = subjTitleBn, onValueChange = { subjTitleBn = it }, label = { Text("বাংলা শিরোনাম *") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = subjTitleEn, onValueChange = { subjTitleEn = it }, label = { Text("English Title") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = subjNote, onValueChange = { subjNote = it }, label = { Text("Syllabus Note") }, modifier = Modifier.fillMaxWidth())
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(selected = subjStatus == "published", onClick = { subjStatus = "published" }, label = { Text("Published (Live)") })
                                    FilterChip(selected = subjStatus == "draft", onClick = { subjStatus = "draft" }, label = { Text("Draft (Hidden)") })
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    if (editingSubjectId.isNotBlank()) {
                                        OutlinedButton(
                                            onClick = {
                                                editingSubjectId = ""; subjCourseCode = ""; subjTitleBn = ""; subjTitleEn = ""; subjNote = ""; subjStatus = "published"
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) { Text("বাতিল") }
                                    }
                                    Button(
                                        onClick = {
                                            onSaveSubject(
                                                Subject(
                                                    id = editingSubjectId,
                                                    departmentId = subjDeptId,
                                                    yearNumber = subjYear,
                                                    courseCode = subjCourseCode.trim(),
                                                    titleBn = subjTitleBn.trim(),
                                                    titleEn = subjTitleEn.trim(),
                                                    syllabusNote = subjNote.trim(),
                                                    status = subjStatus
                                                )
                                            )
                                            editingSubjectId = ""; subjCourseCode = ""; subjTitleBn = ""; subjTitleEn = ""; subjNote = ""; subjStatus = "published"
                                        },
                                        enabled = subjCourseCode.isNotBlank() && subjTitleBn.isNotBlank(),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (editingSubjectId.isBlank()) "বিষয় প্রকাশ করুন" else "আপডেট ও সিঙ্ক করুন")
                                    }
                                }
                            }
                        }
                    }
                    items(subjects, key = { it.id }) { subj ->
                        val deptName = departments.firstOrNull { it.id == subj.departmentId }?.nameBn ?: subj.departmentId
                        Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("${subj.courseCode} • ${subj.titleBn}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f, fill = false))
                                        Surface(
                                            color = if (subj.status == "published") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(if (subj.status == "published") "LIVE" else "DRAFT", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                    Text("$deptName • অনার্স ${subj.yearNumber} বর্ষ • ${subj.titleEn}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = {
                                    editingSubjectId = subj.id
                                    subjDeptId = subj.departmentId.ifBlank { "political_science" }
                                    subjYear = subj.yearNumber
                                    subjCourseCode = subj.courseCode
                                    subjTitleBn = subj.titleBn
                                    subjTitleEn = subj.titleEn
                                    subjNote = subj.syllabusNote
                                    subjStatus = subj.status
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit subject")
                                }
                                IconButton(onClick = { onDeleteSubject(subj.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete subject")
                                }
                            }
                        }
                    }
                }
                2 -> {
                    item {
                        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                SectionHeader(title = if (editingChapterId.isBlank()) "অধ্যায় ও পাঠ্য বিষয়বস্তু যুক্ত করুন" else "অধ্যায় সম্পাদনা করুন")
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(subjects, key = { it.id }) { subj ->
                                        FilterChip(selected = chapSubjectId == subj.id, onClick = { chapSubjectId = subj.id }, label = { Text("${subj.courseCode} (${subj.titleBn.take(14)})") })
                                    }
                                }
                                OutlinedTextField(value = chapNum, onValueChange = { chapNum = it }, label = { Text("Chapter Number") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = chapTitleBn, onValueChange = { chapTitleBn = it }, label = { Text("অধ্যায়ের নাম *") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = chapSummary, onValueChange = { chapSummary = it }, label = { Text("অধ্যায়ের সারসংক্ষেপ") }, maxLines = 3, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = chapDefinitions, onValueChange = { chapDefinitions = it }, label = { Text("গুরুত্বপূর্ণ সংজ্ঞা") }, maxLines = 3, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = chapShortQ, onValueChange = { chapShortQ = it }, label = { Text("সংক্ষিপ্ত প্রশ্ন ও উত্তর") }, maxLines = 3, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = chapBroadQ, onValueChange = { chapBroadQ = it }, label = { Text("রচনামূলক প্রশ্ন ও উত্তর") }, maxLines = 3, modifier = Modifier.fillMaxWidth())
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(selected = chapStatus == "published", onClick = { chapStatus = "published" }, label = { Text("Published (Live)") })
                                    FilterChip(selected = chapStatus == "draft", onClick = { chapStatus = "draft" }, label = { Text("Draft (Hidden)") })
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    if (editingChapterId.isNotBlank()) {
                                        OutlinedButton(
                                            onClick = {
                                                editingChapterId = ""; chapTitleBn = ""; chapSummary = ""; chapDefinitions = ""; chapShortQ = ""; chapBroadQ = ""; chapStatus = "published"
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) { Text("বাতিল") }
                                    }
                                    Button(
                                        onClick = {
                                            val subj = subjects.firstOrNull { it.id == chapSubjectId }
                                            onSaveChapter(
                                                Chapter(
                                                    id = editingChapterId,
                                                    subjectId = chapSubjectId,
                                                    yearNumber = subj?.yearNumber ?: 1L,
                                                    chapterNumber = chapNum.toLongOrNull() ?: 1L,
                                                    titleBn = chapTitleBn.trim(),
                                                    summary = chapSummary.trim(),
                                                    definitions = chapDefinitions.trim(),
                                                    shortQuestions = chapShortQ.trim(),
                                                    broadQuestions = chapBroadQ.trim(),
                                                    status = chapStatus
                                                )
                                            )
                                            editingChapterId = ""; chapTitleBn = ""; chapSummary = ""; chapDefinitions = ""; chapShortQ = ""; chapBroadQ = ""; chapStatus = "published"
                                        },
                                        enabled = chapSubjectId.isNotBlank() && chapTitleBn.isNotBlank(),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (editingChapterId.isBlank()) "অধ্যায় সংরক্ষণ করুন" else "আপডেট ও সিঙ্ক করুন")
                                    }
                                }
                            }
                        }
                    }
                    items(chapters, key = { it.id }) { ch ->
                        Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("অধ্যায় ${ch.chapterNumber}: ${ch.titleBn}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f, fill = false))
                                        Surface(
                                            color = if (ch.status == "published") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(if (ch.status == "published") "LIVE" else "DRAFT", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                    Text(ch.summary, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                                IconButton(onClick = {
                                    editingChapterId = ch.id
                                    chapSubjectId = ch.subjectId
                                    chapNum = ch.chapterNumber.toString()
                                    chapTitleBn = ch.titleBn
                                    chapSummary = ch.summary
                                    chapDefinitions = ch.definitions
                                    chapShortQ = ch.shortQuestions
                                    chapBroadQ = ch.broadQuestions
                                    chapStatus = ch.status
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit chapter")
                                }
                                IconButton(onClick = { onDeleteChapter(ch.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete chapter")
                                }
                            }
                        }
                    }
                }
                else -> {
                    item {
                        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                SectionHeader(title = if (editingQuestionId.isBlank()) "প্রশ্ন / সাজেশন / MCQ যোগ করুন" else "প্রশ্ন সম্পাদনা করুন")
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(departments, key = { it.id }) { dept ->
                                        FilterChip(selected = qDeptId == dept.id, onClick = { qDeptId = dept.id }, label = { Text(dept.nameBn) })
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(selected = qType == "previous_year", onClick = { qType = "previous_year" }, label = { Text("Previous") })
                                    FilterChip(selected = qType == "suggestion", onClick = { qType = "suggestion" }, label = { Text("Suggestion") })
                                    FilterChip(selected = qType == "mcq", onClick = { qType = "mcq" }, label = { Text("MCQ") })
                                }
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(listOf(1L, 2L, 3L, 4L)) { yr ->
                                        FilterChip(selected = qYear == yr, onClick = { qYear = yr }, label = { Text("Year $yr") })
                                    }
                                }
                                OutlinedTextField(value = qSubjectName, onValueChange = { qSubjectName = it }, label = { Text("বিষয়ের নাম") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = qExamYear, onValueChange = { qExamYear = it }, label = { Text("Exam Year / Tag") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = qPrompt, onValueChange = { qPrompt = it }, label = { Text("প্রশ্ন *") }, maxLines = 3, modifier = Modifier.fillMaxWidth())
                                if (qType == "mcq") {
                                    OutlinedTextField(value = qOptA, onValueChange = { qOptA = it }, label = { Text("Option A") }, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = qOptB, onValueChange = { qOptB = it }, label = { Text("Option B") }, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = qOptC, onValueChange = { qOptC = it }, label = { Text("Option C") }, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = qOptD, onValueChange = { qOptD = it }, label = { Text("Option D") }, modifier = Modifier.fillMaxWidth())
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf(0, 1, 2, 3).forEach { idx ->
                                            FilterChip(selected = qCorrectIdx == idx, onClick = { qCorrectIdx = idx }, label = { Text("Ans: ${'A' + idx}") })
                                        }
                                    }
                                }
                                OutlinedTextField(value = qAnswerText, onValueChange = { qAnswerText = it }, label = { Text("উত্তর / ব্যাখ্যা") }, maxLines = 4, modifier = Modifier.fillMaxWidth())
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(selected = qStatus == "published", onClick = { qStatus = "published" }, label = { Text("Published (Live)") })
                                    FilterChip(selected = qStatus == "draft", onClick = { qStatus = "draft" }, label = { Text("Draft (Hidden)") })
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    if (editingQuestionId.isNotBlank()) {
                                        OutlinedButton(
                                            onClick = {
                                                editingQuestionId = ""; qPrompt = ""; qAnswerText = ""; qOptA = ""; qOptB = ""; qOptC = ""; qOptD = ""; qStatus = "published"
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) { Text("বাতিল") }
                                    }
                                    Button(
                                        onClick = {
                                            val opts = if (qType == "mcq") listOf(qOptA, qOptB, qOptC, qOptD).filter { it.isNotBlank() } else emptyList()
                                            onSaveQuestion(
                                                StudyQuestion(
                                                    id = editingQuestionId,
                                                    departmentId = qDeptId,
                                                    yearNumber = qYear,
                                                    subjectName = qSubjectName.trim(),
                                                    questionType = qType,
                                                    examYear = qExamYear.trim(),
                                                    prompt = qPrompt.trim(),
                                                    answerText = qAnswerText.trim(),
                                                    options = opts,
                                                    correctOptionIndex = qCorrectIdx.toLong(),
                                                    explanation = qAnswerText.trim(),
                                                    status = qStatus
                                                )
                                            )
                                            editingQuestionId = ""; qPrompt = ""; qAnswerText = ""; qOptA = ""; qOptB = ""; qOptC = ""; qOptD = ""; qStatus = "published"
                                        },
                                        enabled = qPrompt.isNotBlank(),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (editingQuestionId.isBlank()) "প্রশ্ন প্রকাশ করুন" else "আপডেট ও সিঙ্ক করুন")
                                    }
                                }
                            }
                        }
                    }
                    items(questions, key = { it.id }) { q ->
                        Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("${q.questionType.uppercase()} • ${q.subjectName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f, fill = false))
                                        Surface(
                                            color = if (q.status == "published") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(if (q.status == "published") "LIVE" else "DRAFT", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                    Text(q.prompt, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                }
                                IconButton(onClick = {
                                    editingQuestionId = q.id
                                    qDeptId = q.departmentId.ifBlank { "political_science" }
                                    qType = q.questionType
                                    qYear = q.yearNumber
                                    qSubjectName = q.subjectName
                                    qExamYear = q.examYear
                                    qPrompt = q.prompt
                                    qAnswerText = q.answerText.ifBlank { q.explanation }
                                    qOptA = q.options.getOrNull(0).orEmpty()
                                    qOptB = q.options.getOrNull(1).orEmpty()
                                    qOptC = q.options.getOrNull(2).orEmpty()
                                    qOptD = q.options.getOrNull(3).orEmpty()
                                    qCorrectIdx = q.correctOptionIndex.toInt().coerceIn(0, 3)
                                    qStatus = q.status
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit question")
                                }
                                IconButton(onClick = { onDeleteQuestion(q.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete question")
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
fun AdminAnnouncementScreen(
    announcements: List<Announcement>,
    onBack: () -> Unit,
    onSaveAnnouncement: (Announcement) -> Unit,
    onDeleteAnnouncement: (String) -> Unit
) {
    var editingId by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf("NU Notice") }
    var pinned by remember { mutableStateOf(true) }
    var status by remember { mutableStateOf("published") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HonoursTopBar(title = "ঘোষণা ও নোটিশ ম্যানেজার", subtitle = "Announcements Auto-Sync", onBackClick = onBack)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionHeader(title = if (editingId.isBlank()) "নতুন ঘোষণা প্রকাশ করুন" else "ঘোষণা সম্পাদনা করুন")
                        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("শিরোনাম *") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("বিস্তারিত বার্তা *") }, maxLines = 4, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = tag, onValueChange = { tag = it }, label = { Text("ট্যাগ (e.g., NU Notice, Exam Update)") }, modifier = Modifier.fillMaxWidth())
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("হোম স্ক্রিনে পিন করুন")
                            Switch(checked = pinned, onCheckedChange = { pinned = it })
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = status == "published", onClick = { status = "published" }, label = { Text("Published (Live)") })
                            FilterChip(selected = status == "draft", onClick = { status = "draft" }, label = { Text("Draft (Hidden)") })
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            if (editingId.isNotBlank()) {
                                OutlinedButton(
                                    onClick = { editingId = ""; title = ""; body = ""; tag = "NU Notice"; pinned = true; status = "published" },
                                    modifier = Modifier.weight(1f)
                                ) { Text("বাতিল") }
                            }
                            Button(
                                onClick = {
                                    onSaveAnnouncement(
                                        Announcement(
                                            id = editingId,
                                            title = title.trim(),
                                            body = body.trim(),
                                            tag = tag.trim(),
                                            pinned = pinned,
                                            status = status
                                        )
                                    )
                                    editingId = ""; title = ""; body = ""; status = "published"
                                },
                                enabled = title.isNotBlank() && body.isNotBlank(),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Campaign, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (editingId.isBlank()) "ঘোষণা প্রকাশ করুন" else "আপডেট ও সিঙ্ক করুন")
                            }
                        }
                    }
                }
            }

            items(announcements, key = { it.id }) { ann ->
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (ann.pinned) Icon(Icons.Default.PushPin, contentDescription = "Pinned", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                Text(ann.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f, fill = false))
                                Surface(
                                    color = if (ann.status == "published") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(if (ann.status == "published") "LIVE" else "DRAFT", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Text(ann.body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = {
                            editingId = ann.id
                            title = ann.title
                            body = ann.body
                            tag = ann.tag
                            pinned = ann.pinned
                            status = ann.status
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit announcement")
                        }
                        IconButton(onClick = { onDeleteAnnouncement(ann.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete announcement")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminReportScreen(
    reportsState: UiState<List<UserReport>>,
    onBack: () -> Unit,
    onUpdateReport: (reportId: String, status: String, note: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HonoursTopBar(title = "শিক্ষার্থীদের রিপোর্ট", subtitle = "User Content & AI Report Moderation", onBackClick = onBack)

        when (reportsState) {
            is UiState.Loading -> LoadingStateBox("রিপোর্ট লোড হচ্ছে...")
            is UiState.Error -> Box(modifier = Modifier.padding(16.dp)) { ErrorStateCard(reportsState.message) }
            is UiState.Success -> {
                val reports = reportsState.data
                if (reports.isEmpty()) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        EmptyStateView(
                            icon = Icons.Default.Flag,
                            title = "কোনো রিপোর্ট নেই",
                            message = "শিক্ষার্থীরা কোনো প্রশ্ন, উত্তর বা PDF নিয়ে রিপোর্ট করলে এখানে দেখা যাবে।"
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 680.dp)
                            .align(Alignment.CenterHorizontally),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(reports, key = { it.id }) { report ->
                            var adminNote by remember(report.id) { mutableStateOf(report.adminNote) }
                            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${report.targetType} • ${report.reporterName}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                        Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(8.dp)) {
                                            Text(report.status.uppercase(), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                        }
                                    }
                                    Text(report.reason, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    if (report.details.isNotBlank()) {
                                        Text(report.details, style = MaterialTheme.typography.bodySmall)
                                    }
                                    HorizontalDivider()
                                    OutlinedTextField(
                                        value = adminNote,
                                        onValueChange = { adminNote = it },
                                        label = { Text("Admin Moderation Note") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(onClick = { onUpdateReport(report.id, "reviewed", adminNote) }, modifier = Modifier.weight(1f)) {
                                            Text("Reviewed")
                                        }
                                        Button(onClick = { onUpdateReport(report.id, "resolved", adminNote) }, modifier = Modifier.weight(1f)) {
                                            Text("Resolved")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else -> Unit
        }
    }
}
