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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.School
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
import com.example.data.model.AiRemoteConfig
import com.example.data.model.Announcement
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
    onInitializePin: (String) -> Unit
) {
    var pinInput by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

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
                    } else if (hasPinConfigured == false) {
                        Text(
                            text = "প্রথমবার সেটআপ: আপনার নির্ধারিত অ্যাডমিন পিন/পাসওয়ার্ড ব্যাকএন্ড Firestore-এ নিরাপদ SHA-256 হ্যাশ হিসেবে সংরক্ষণ করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "কনটেন্ট ম্যানেজমেন্ট প্যানেলে প্রবেশ করতে অ্যাডমিন পাসওয়ার্ড লিখুন। পাসওয়ার্ড ব্যাকএন্ড থেকে যাচাই করা হয়।",
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
                            Text(if (hasPinConfigured == false) "নতুন অ্যাডমিন পাসওয়ার্ড সেট করুন" else "অ্যাডমিন পাসওয়ার্ড")
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
                            if (hasPinConfigured == false) {
                                onInitializePin(pinInput.trim())
                            } else {
                                onVerifyPin(pinInput.trim())
                            }
                        },
                        enabled = !isVerifying && remainingLockoutSec == 0L && pinInput.isNotBlank() && isBackendAdminAccount,
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
                            Text(if (hasPinConfigured == false) "ব্যাকএন্ড পিন সেট ও আনলক করুন" else "আনলক করুন")
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
    onNavigateAiLimits: () -> Unit = {}
) {
    var modelName by remember(remoteConfig.modelName) { mutableStateOf(remoteConfig.modelName) }
    var sysInstructions by remember(remoteConfig.systemInstructions) { mutableStateOf(remoteConfig.systemInstructions) }
    var maintenanceMode by remember(remoteConfig.maintenanceMode) { mutableStateOf(remoteConfig.maintenanceMode) }
    var maintenanceMsg by remember(remoteConfig.maintenanceMessage) { mutableStateOf(remoteConfig.maintenanceMessage) }

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
                .align(Alignment.CenterHorizontally),
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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionHeader(
                            title = "একাডেমিক ম্যানেজমেন্ট টুলস",
                            subtitle = "ডায়নামিকভাবে সকল বিভাগ, সিলেবাস, PDF ও ঘোষণা পরিচালনা করুন"
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
                            Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
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
    onSaveQuestion: (StudyQuestion) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("বিভাগ", "বিষয়", "অধ্যায়", "প্রশ্ন ও MCQ")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HonoursTopBar(title = "সিলেবাস ও কনটেন্ট এডিটর", subtitle = "Dynamic NU Curriculum CRUD", onBackClick = onBack)
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { idx, label ->
                Tab(selected = selectedTab == idx, onClick = { selectedTab = idx }, text = { Text(label) })
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
                        var nameBn by remember { mutableStateOf("") }
                        var nameEn by remember { mutableStateOf("") }
                        var code by remember { mutableStateOf("") }
                        var desc by remember { mutableStateOf("") }
                        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                SectionHeader(title = "নতুন বিভাগ যুক্ত করুন")
                                OutlinedTextField(value = nameBn, onValueChange = { nameBn = it }, label = { Text("বিভাগের বাংলা নাম *") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = nameEn, onValueChange = { nameEn = it }, label = { Text("English Name") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Short Code (e.g., SOC)") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("বিবরণ") }, modifier = Modifier.fillMaxWidth())
                                Button(
                                    onClick = {
                                        onSaveDepartment(
                                            Department(
                                                id = nameEn.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_'),
                                                nameBn = nameBn.trim(),
                                                nameEn = nameEn.trim(),
                                                code = code.trim(),
                                                description = desc.trim(),
                                                sortOrder = (departments.size + 1).toLong()
                                            )
                                        )
                                        nameBn = ""; nameEn = ""; code = ""; desc = ""
                                    },
                                    enabled = nameBn.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("বিভাগ সংরক্ষণ করুন")
                                }
                            }
                        }
                    }
                    items(departments, key = { it.id }) { dept ->
                        Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${dept.nameBn} (${dept.nameEn})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(dept.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        var year by remember { mutableLongStateOf(1L) }
                        var courseCode by remember { mutableStateOf("") }
                        var titleBn by remember { mutableStateOf("") }
                        var titleEn by remember { mutableStateOf("") }
                        var note by remember { mutableStateOf("") }
                        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                SectionHeader(title = "নতুন বিষয় / কোর্স যুক্ত করুন (রাষ্ট্রবিজ্ঞান)")
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(listOf(1L, 2L, 3L, 4L)) { yr ->
                                        FilterChip(selected = year == yr, onClick = { year = yr }, label = { Text("Year $yr") })
                                    }
                                }
                                OutlinedTextField(value = courseCode, onValueChange = { courseCode = it }, label = { Text("NU Course Code *") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = titleBn, onValueChange = { titleBn = it }, label = { Text("বাংলা শিরোনাম *") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = titleEn, onValueChange = { titleEn = it }, label = { Text("English Title") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Syllabus Note") }, modifier = Modifier.fillMaxWidth())
                                Button(
                                    onClick = {
                                        onSaveSubject(
                                            Subject(
                                                departmentId = "political_science",
                                                yearNumber = year,
                                                courseCode = courseCode.trim(),
                                                titleBn = titleBn.trim(),
                                                titleEn = titleEn.trim(),
                                                syllabusNote = note.trim()
                                            )
                                        )
                                        courseCode = ""; titleBn = ""; titleEn = ""; note = ""
                                    },
                                    enabled = courseCode.isNotBlank() && titleBn.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("বিষয় প্রকাশ করুন")
                                }
                            }
                        }
                    }
                    items(subjects, key = { it.id }) { subj ->
                        Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${subj.courseCode} • ${subj.titleBn}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("অনার্স ${subj.yearNumber} বর্ষ • ${subj.titleEn}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        var selectedSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: "") }
                        var chapNum by remember { mutableStateOf("1") }
                        var titleBn by remember { mutableStateOf("") }
                        var summary by remember { mutableStateOf("") }
                        var definitions by remember { mutableStateOf("") }
                        var shortQ by remember { mutableStateOf("") }
                        var broadQ by remember { mutableStateOf("") }
                        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                SectionHeader(title = "অধ্যায় ও পাঠ্য বিষয়বস্তু যুক্ত করুন")
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(subjects, key = { it.id }) { subj ->
                                        FilterChip(selected = selectedSubjectId == subj.id, onClick = { selectedSubjectId = subj.id }, label = { Text(subj.courseCode) })
                                    }
                                }
                                OutlinedTextField(value = chapNum, onValueChange = { chapNum = it }, label = { Text("Chapter Number") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = titleBn, onValueChange = { titleBn = it }, label = { Text("অধ্যায়ের নাম *") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = summary, onValueChange = { summary = it }, label = { Text("অধ্যায়ের সারসংক্ষেপ") }, maxLines = 3, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = definitions, onValueChange = { definitions = it }, label = { Text("গুরুত্বপূর্ণ সংজ্ঞা") }, maxLines = 3, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = shortQ, onValueChange = { shortQ = it }, label = { Text("সংক্ষিপ্ত প্রশ্ন ও উত্তর") }, maxLines = 3, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = broadQ, onValueChange = { broadQ = it }, label = { Text("রচনামূলক প্রশ্ন ও উত্তর") }, maxLines = 3, modifier = Modifier.fillMaxWidth())
                                Button(
                                    onClick = {
                                        val subj = subjects.firstOrNull { it.id == selectedSubjectId }
                                        onSaveChapter(
                                            Chapter(
                                                subjectId = selectedSubjectId,
                                                yearNumber = subj?.yearNumber ?: 1L,
                                                chapterNumber = chapNum.toLongOrNull() ?: 1L,
                                                titleBn = titleBn.trim(),
                                                summary = summary.trim(),
                                                definitions = definitions.trim(),
                                                shortQuestions = shortQ.trim(),
                                                broadQuestions = broadQ.trim()
                                            )
                                        )
                                        titleBn = ""; summary = ""; definitions = ""; shortQ = ""; broadQ = ""
                                    },
                                    enabled = selectedSubjectId.isNotBlank() && titleBn.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("অধ্যায় সংরক্ষণ করুন")
                                }
                            }
                        }
                    }
                    items(chapters, key = { it.id }) { ch ->
                        Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("অধ্যায় ${ch.chapterNumber}: ${ch.titleBn}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(ch.summary, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
                else -> {
                    item {
                        var qType by remember { mutableStateOf("previous_year") }
                        var year by remember { mutableLongStateOf(1L) }
                        var subjectName by remember { mutableStateOf("রাজনৈতিক তত্ত্ব পরিচিতি") }
                        var examYear by remember { mutableStateOf("2024") }
                        var prompt by remember { mutableStateOf("") }
                        var answerText by remember { mutableStateOf("") }
                        var optA by remember { mutableStateOf("") }
                        var optB by remember { mutableStateOf("") }
                        var optC by remember { mutableStateOf("") }
                        var optD by remember { mutableStateOf("") }
                        var correctIdx by remember { mutableIntStateOf(0) }

                        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                SectionHeader(title = "প্রশ্ন / সাজেশন / MCQ যোগ করুন")
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(selected = qType == "previous_year", onClick = { qType = "previous_year" }, label = { Text("Previous") })
                                    FilterChip(selected = qType == "suggestion", onClick = { qType = "suggestion" }, label = { Text("Suggestion") })
                                    FilterChip(selected = qType == "mcq", onClick = { qType = "mcq" }, label = { Text("MCQ") })
                                }
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(listOf(1L, 2L, 3L, 4L)) { yr ->
                                        FilterChip(selected = year == yr, onClick = { year = yr }, label = { Text("Year $yr") })
                                    }
                                }
                                OutlinedTextField(value = subjectName, onValueChange = { subjectName = it }, label = { Text("বিষয়ের নাম") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = examYear, onValueChange = { examYear = it }, label = { Text("Exam Year / Tag") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = prompt, onValueChange = { prompt = it }, label = { Text("প্রশ্ন *") }, maxLines = 3, modifier = Modifier.fillMaxWidth())
                                if (qType == "mcq") {
                                    OutlinedTextField(value = optA, onValueChange = { optA = it }, label = { Text("Option A") }, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = optB, onValueChange = { optB = it }, label = { Text("Option B") }, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = optC, onValueChange = { optC = it }, label = { Text("Option C") }, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = optD, onValueChange = { optD = it }, label = { Text("Option D") }, modifier = Modifier.fillMaxWidth())
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf(0, 1, 2, 3).forEach { idx ->
                                            FilterChip(selected = correctIdx == idx, onClick = { correctIdx = idx }, label = { Text("Ans: ${'A' + idx}") })
                                        }
                                    }
                                }
                                OutlinedTextField(value = answerText, onValueChange = { answerText = it }, label = { Text("উত্তর / ব্যাখ্যা") }, maxLines = 4, modifier = Modifier.fillMaxWidth())
                                Button(
                                    onClick = {
                                        val opts = if (qType == "mcq") listOf(optA, optB, optC, optD).filter { it.isNotBlank() } else emptyList()
                                        onSaveQuestion(
                                            StudyQuestion(
                                                yearNumber = year,
                                                subjectName = subjectName.trim(),
                                                questionType = qType,
                                                examYear = examYear.trim(),
                                                prompt = prompt.trim(),
                                                answerText = answerText.trim(),
                                                options = opts,
                                                correctOptionIndex = correctIdx.toLong(),
                                                explanation = answerText.trim()
                                            )
                                        )
                                        prompt = ""; answerText = ""; optA = ""; optB = ""; optC = ""; optD = ""
                                    },
                                    enabled = prompt.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("প্রশ্ন প্রকাশ করুন")
                                }
                            }
                        }
                    }
                    items(questions, key = { it.id }) { q ->
                        Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("${q.questionType.uppercase()} • ${q.subjectName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Text(q.prompt, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
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
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf("NU Notice") }
    var pinned by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HonoursTopBar(title = "ঘোষণা ও নোটিশ ম্যানেজার", subtitle = "Announcements Management", onBackClick = onBack)

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
                        SectionHeader(title = "নতুন ঘোষণা প্রকাশ করুন")
                        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("শিরোনাম *") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("বিস্তারিত বার্তা *") }, maxLines = 4, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = tag, onValueChange = { tag = it }, label = { Text("ট্যাগ (e.g., NU Notice, Exam Update)") }, modifier = Modifier.fillMaxWidth())
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("হোম স্ক্রিনে পিন করুন")
                            Switch(checked = pinned, onCheckedChange = { pinned = it })
                        }
                        Button(
                            onClick = {
                                onSaveAnnouncement(Announcement(title = title.trim(), body = body.trim(), tag = tag.trim(), pinned = pinned))
                                title = ""; body = ""
                            },
                            enabled = title.isNotBlank() && body.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ঘোষণা প্রকাশ করুন")
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
                                Text(ann.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                            Text(ann.body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
