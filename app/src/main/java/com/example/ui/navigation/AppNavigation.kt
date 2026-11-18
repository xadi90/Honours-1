package com.example.ui.navigation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.core.NetworkMonitor
import com.example.core.PreferencesManager
import com.example.core.signOut
import com.example.data.catalog.AcademicSeedCatalog
import com.example.data.model.AnswerLength
import com.example.data.model.UiState
import com.example.data.repository.AiRepository
import com.example.data.repository.FirestoreRepository
import com.example.ui.components.OfflineStatusBanner
import com.example.ui.screens.admin.AdminAnnouncementScreen
import com.example.ui.screens.admin.AdminContentEditorScreen
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.admin.AdminPasswordScreen
import com.example.ui.screens.admin.AdminPdfUploadScreen
import com.example.ui.screens.admin.AdminReportScreen
import com.example.ui.screens.ai.AiChatScreen
import com.example.ui.screens.ai.AiQuestionSolverScreen
import com.example.ui.screens.ai.AnswerDetailsScreen
import com.example.ui.screens.ai.QuestionImageUploadScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.auth.OnboardingScreen
import com.example.ui.screens.auth.SplashScreen
import com.example.ui.screens.exams.McqPracticeScreen
import com.example.ui.screens.exams.ModelTestScreen
import com.example.ui.screens.exams.PreviousQuestionScreen
import com.example.ui.screens.exams.SuggestionScreen
import com.example.ui.screens.home.GlobalSearchScreen
import com.example.ui.screens.home.HomeDashboardScreen
import com.example.ui.screens.library.AllDepartmentsPdfLibraryScreen
import com.example.ui.screens.library.DepartmentPdfScreen
import com.example.ui.screens.library.DownloadManagerScreen
import com.example.ui.screens.library.PdfReaderScreen
import com.example.ui.screens.political.ChapterDetailsScreen
import com.example.ui.screens.political.HonoursYearScreen
import com.example.ui.screens.political.PoliticalScienceDepartmentScreen
import com.example.ui.screens.political.SubjectDetailsScreen
import com.example.ui.screens.profile.AboutDeveloperScreen
import com.example.ui.screens.profile.BookmarkScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.profile.SettingsScreen
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.HonoursSolutionTheme
import com.example.ui.viewmodel.AcademicViewModel
import com.example.ui.viewmodel.AdminViewModel
import com.example.ui.viewmodel.AiViewModel
import com.example.ui.viewmodel.MainViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

private data class BottomNavTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

private fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    trySend(currentUser)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun HonoursSolutionApp() {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val prefs = remember(appContext) { PreferencesManager(appContext) }
    val scope = rememberCoroutineScope()

    val themeMode by prefs.themeMode.collectAsStateWithLifecycle(initialValue = AppThemeMode.SYSTEM)
    val onboardingDone by prefs.onboardingCompleted.collectAsStateWithLifecycle(initialValue = true)
    val currentUser by remember { Firebase.auth.authStateFlow() }
        .collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)

    var splashFinished by remember { mutableStateOf(false) }

    HonoursSolutionTheme(themeMode = themeMode) {
        when {
            !splashFinished -> {
                SplashScreen(onFinished = { splashFinished = true })
            }
            !onboardingDone -> {
                OnboardingScreen(
                    onComplete = {
                        scope.launch { prefs.setOnboardingCompleted(true) }
                    }
                )
            }
            currentUser == null -> {
                AuthScreen(onAuthSuccess = {})
            }
            else -> {
                val activeUser = currentUser!!
                key(activeUser.uid) {
                    MainAuthenticatedShell(
                        currentUserId = activeUser.uid,
                        prefs = prefs
                    )
                }
            }
        }
    }
}

@Composable
private fun MainAuthenticatedShell(
    currentUserId: String,
    prefs: PreferencesManager
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val firestoreRepo = remember(appContext) { FirestoreRepository(appContext) }
    val aiRepo = remember { AiRepository() }
    val networkMonitor = remember(appContext) { NetworkMonitor(appContext) }

    val mainViewModel: MainViewModel = viewModel(
        key = "main_$currentUserId",
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(firestoreRepo, prefs, networkMonitor, currentUserId) as T
            }
        }
    )

    val academicViewModel: AcademicViewModel = viewModel(
        key = "academic_$currentUserId",
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AcademicViewModel(firestoreRepo, prefs, appContext) as T
            }
        }
    )

    val aiViewModel: AiViewModel = viewModel(
        key = "ai_$currentUserId",
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AiViewModel(aiRepo, firestoreRepo, currentUserId) as T
            }
        }
    )

    val adminViewModel: AdminViewModel = viewModel(
        key = "admin_$currentUserId",
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AdminViewModel(firestoreRepo, prefs, appContext) as T
            }
        }
    )

    // Collect Live StateFlows
    val isOnline by mainViewModel.isOnline.collectAsStateWithLifecycle()
    val themeMode by mainViewModel.themeMode.collectAsStateWithLifecycle()
    val languagePref by mainViewModel.languagePref.collectAsStateWithLifecycle()
    val searchHistory by mainViewModel.searchHistory.collectAsStateWithLifecycle()
    val recentPdfIds by mainViewModel.recentPdfIds.collectAsStateWithLifecycle()
    val recentSubjectIds by mainViewModel.recentSubjectIds.collectAsStateWithLifecycle()
    val userProfileState by mainViewModel.userProfile.collectAsStateWithLifecycle()
    val bookmarksState by mainViewModel.bookmarks.collectAsStateWithLifecycle()
    val testResultsState by mainViewModel.testResults.collectAsStateWithLifecycle()
    val announcements by mainViewModel.announcements.collectAsStateWithLifecycle()
    val aiRemoteConfig by mainViewModel.aiRemoteConfig.collectAsStateWithLifecycle()
    val isBackendAdmin by mainViewModel.isAdmin.collectAsStateWithLifecycle()
    val updateState by mainViewModel.updateState.collectAsStateWithLifecycle()
    val mainActionMsg by mainViewModel.actionMessage.collectAsStateWithLifecycle()

    val departmentsState by academicViewModel.departments.collectAsStateWithLifecycle()
    val subjectsState by academicViewModel.subjects.collectAsStateWithLifecycle()
    val chaptersState by academicViewModel.chapters.collectAsStateWithLifecycle()
    val pdfsState by academicViewModel.pdfs.collectAsStateWithLifecycle()
    val questionsState by academicViewModel.questions.collectAsStateWithLifecycle()
    val modelTestsState by academicViewModel.modelTests.collectAsStateWithLifecycle()
    val downloadStates by academicViewModel.downloadStates.collectAsStateWithLifecycle()
    val offlineRefreshTick by academicViewModel.offlineRefreshTick.collectAsStateWithLifecycle()

    val savedAnswersState by aiViewModel.savedAnswers.collectAsStateWithLifecycle()
    val solveState by aiViewModel.solveState.collectAsStateWithLifecycle()
    val selectedAnswer by aiViewModel.selectedAnswer.collectAsStateWithLifecycle()
    val attachedImages by aiViewModel.attachedImages.collectAsStateWithLifecycle()
    val conversations by aiViewModel.conversations.collectAsStateWithLifecycle()
    val activeMessages by aiViewModel.activeMessages.collectAsStateWithLifecycle()
    val streamingChatText by aiViewModel.streamingChatText.collectAsStateWithLifecycle()
    val isChatStreaming by aiViewModel.isChatStreaming.collectAsStateWithLifecycle()
    val chatError by aiViewModel.chatError.collectAsStateWithLifecycle()
    val generatedMcqsState by aiViewModel.generatedMcqs.collectAsStateWithLifecycle()
    val aiStatusBanner by aiViewModel.statusBanner.collectAsStateWithLifecycle()

    val lockoutState by adminViewModel.lockoutState.collectAsStateWithLifecycle()
    val isAdminAuthorized by adminViewModel.isAdminAuthorized.collectAsStateWithLifecycle()
    val isAdminAccount by adminViewModel.isBackendAdminAccount.collectAsStateWithLifecycle()
    val hasPinConfigured by adminViewModel.hasPinConfigured.collectAsStateWithLifecycle()
    val adminAuthError by adminViewModel.authError.collectAsStateWithLifecycle()
    val isAdminVerifying by adminViewModel.isVerifying.collectAsStateWithLifecycle()
    val adminToast by adminViewModel.adminToast.collectAsStateWithLifecycle()
    val reportsState by adminViewModel.reports.collectAsStateWithLifecycle()
    val adminLogs by adminViewModel.adminLogs.collectAsStateWithLifecycle()

    val userProfile = (userProfileState as? UiState.Success)?.data
    val departments = (departmentsState as? UiState.Success)?.data ?: AcademicSeedCatalog.defaultDepartments
    val subjects = (subjectsState as? UiState.Success)?.data ?: AcademicSeedCatalog.starterSubjects
    val chapters = (chaptersState as? UiState.Success)?.data ?: AcademicSeedCatalog.starterChapters
    val pdfs = (pdfsState as? UiState.Success)?.data ?: AcademicSeedCatalog.starterPdfs
    val questions = (questionsState as? UiState.Success)?.data ?: AcademicSeedCatalog.starterQuestions
    val modelTests = (modelTestsState as? UiState.Success)?.data ?: AcademicSeedCatalog.starterModelTests
    val bookmarks = (bookmarksState as? UiState.Success)?.data ?: emptyList()
    val savedAnswers = (savedAnswersState as? UiState.Success)?.data ?: emptyList()
    val testResults = (testResultsState as? UiState.Success)?.data ?: emptyList()

    var pendingAiPrompt by remember { mutableStateOf("") }
    var showExitDialog by remember { mutableStateOf(false) }

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(mainActionMsg) {
        mainActionMsg?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            mainViewModel.clearActionMessage()
        }
    }
    LaunchedEffect(aiStatusBanner) {
        aiStatusBanner?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            aiViewModel.clearStatusBanner()
        }
    }
    LaunchedEffect(adminToast) {
        adminToast?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            adminViewModel.clearAdminToast()
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomTabs = remember {
        listOf(
            BottomNavTab(AppRoutes.HOME, "হোম", Icons.Default.Home, "nav_home"),
            BottomNavTab(AppRoutes.AI_SOLVER, "AI সমাধান", Icons.Default.AutoAwesome, "nav_ai_solver"),
            BottomNavTab(AppRoutes.PDF_LIBRARY, "লাইব্রেরি", Icons.Default.PictureAsPdf, "nav_pdf_library"),
            BottomNavTab(AppRoutes.BOOKMARKS, "বুকমার্ক", Icons.Default.Bookmark, "nav_bookmarks"),
            BottomNavTab(AppRoutes.PROFILE, "প্রোফাইল", Icons.Default.Person, "nav_profile")
        )
    }

    val isTopLevelRoute = currentRoute in bottomTabs.map { it.route }

    if (currentRoute == AppRoutes.HOME) {
        BackHandler(enabled = true) {
            if (drawerState.isOpen) {
                scope.launch { drawerState.close() }
            } else {
                showExitDialog = true
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Honours Solution বন্ধ করবেন?") },
            text = { Text("আপনি কি অ্যাপ থেকে বের হয়ে যেতে চান?") },
            confirmButton = {
                TextButton(onClick = { (context as? Activity)?.finish() }) {
                    Text("হ্যাঁ, বন্ধ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("থাকুন")
                }
            }
        )
    }

    fun navigateTopTab(route: String) {
        navController.navigate(route) {
            popUpTo(AppRoutes.HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateToAiSolverWithPrompt(prompt: String) {
        pendingAiPrompt = prompt
        navigateTopTab(AppRoutes.AI_SOLVER)
    }

    fun navigateToAdminGate() {
        if (isAdminAuthorized) {
            navController.navigate(AppRoutes.ADMIN_DASHBOARD)
        } else {
            navController.navigate(AppRoutes.ADMIN_PASSWORD)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isTopLevelRoute,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                        Text(
                            text = "Honours Solution",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "National University, Bangladesh",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(6.dp))

                    NavigationDrawerItem(
                        label = { Text("হোম ড্যাশবোর্ড") },
                        selected = currentRoute == AppRoutes.HOME,
                        onClick = { scope.launch { drawerState.close() }; navigateTopTab(AppRoutes.HOME) },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text("রাষ্ট্রবিজ্ঞান বিভাগ (১ম–৪র্থ বর্ষ)") },
                        selected = currentRoute == AppRoutes.POLITICAL_SCIENCE,
                        onClick = { scope.launch { drawerState.close() }; navController.navigate(AppRoutes.POLITICAL_SCIENCE) },
                        icon = { Icon(Icons.Default.School, contentDescription = null) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text("সকল বিভাগের PDF লাইব্রেরি") },
                        selected = currentRoute == AppRoutes.PDF_LIBRARY,
                        onClick = { scope.launch { drawerState.close() }; navigateTopTab(AppRoutes.PDF_LIBRARY) },
                        icon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text("AI প্রশ্ন সমাধান") },
                        selected = currentRoute == AppRoutes.AI_SOLVER,
                        onClick = { scope.launch { drawerState.close() }; navigateTopTab(AppRoutes.AI_SOLVER) },
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text("AI টিউটর চ্যাট") },
                        selected = currentRoute == AppRoutes.AI_CHAT,
                        onClick = { scope.launch { drawerState.close() }; navController.navigate(AppRoutes.AI_CHAT) },
                        icon = { Icon(Icons.Default.Chat, contentDescription = null) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    NavigationDrawerItem(
                        label = { Text("বিগত সালের প্রশ্ন") },
                        selected = currentRoute == AppRoutes.PREVIOUS_QUESTIONS,
                        onClick = { scope.launch { drawerState.close() }; navController.navigate(AppRoutes.PREVIOUS_QUESTIONS) },
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text("পরীক্ষার সাজেশন") },
                        selected = currentRoute == AppRoutes.SUGGESTIONS,
                        onClick = { scope.launch { drawerState.close() }; navController.navigate(AppRoutes.SUGGESTIONS) },
                        icon = { Icon(Icons.Default.Lightbulb, contentDescription = null) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text("MCQ অনুশীলন") },
                        selected = currentRoute == AppRoutes.MCQ_PRACTICE,
                        onClick = { scope.launch { drawerState.close() }; navController.navigate(AppRoutes.MCQ_PRACTICE) },
                        icon = { Icon(Icons.Default.Quiz, contentDescription = null) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text("মডেল টেস্ট") },
                        selected = currentRoute == AppRoutes.MODEL_TEST,
                        onClick = { scope.launch { drawerState.close() }; navController.navigate(AppRoutes.MODEL_TEST) },
                        icon = { Icon(Icons.Default.Timer, contentDescription = null) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text("ডাউনলোড ম্যানেজার") },
                        selected = currentRoute == AppRoutes.DOWNLOAD_MANAGER,
                        onClick = { scope.launch { drawerState.close() }; navController.navigate(AppRoutes.DOWNLOAD_MANAGER) },
                        icon = { Icon(Icons.Default.Download, contentDescription = null) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    NavigationDrawerItem(
                        label = { Text("কনটেন্ট ম্যানেজমেন্ট") },
                        selected = currentRoute?.startsWith("admin") == true,
                        onClick = { scope.launch { drawerState.close() }; navigateToAdminGate() },
                        icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text("সেটিংস ও আপডেট") },
                        selected = currentRoute == AppRoutes.SETTINGS,
                        onClick = { scope.launch { drawerState.close() }; navController.navigate(AppRoutes.SETTINGS) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text("ডেভেলপার পরিচিতি") },
                        selected = currentRoute == AppRoutes.ABOUT_DEVELOPER,
                        onClick = { scope.launch { drawerState.close() }; navController.navigate(AppRoutes.ABOUT_DEVELOPER) },
                        icon = { Icon(Icons.Default.Info, contentDescription = null) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            contentWindowInsets = WindowInsets.navigationBars,
            bottomBar = {
                if (isTopLevelRoute) {
                    NavigationBar {
                        bottomTabs.forEach { tab ->
                            NavigationBarItem(
                                selected = currentRoute == tab.route,
                                onClick = { navigateTopTab(tab.route) },
                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                label = { Text(tab.label, maxLines = 1) },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Box(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                    OfflineStatusBanner(isOnline = isOnline)
                }

                NavHost(
                    navController = navController,
                    startDestination = AppRoutes.HOME,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(AppRoutes.HOME) {
                        HomeDashboardScreen(
                            profile = userProfile,
                            subjects = subjects,
                            pdfs = pdfs,
                            announcements = announcements,
                            recentPdfIds = recentPdfIds,
                            recentSubjectIds = recentSubjectIds,
                            bookmarksCount = bookmarks.size + savedAnswers.count { it.bookmarked },
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            onSearchClick = { navController.navigate(AppRoutes.GLOBAL_SEARCH) },
                            onAiSolverClick = { navigateTopTab(AppRoutes.AI_SOLVER) },
                            onPoliticalScienceClick = { navController.navigate(AppRoutes.POLITICAL_SCIENCE) },
                            onYearClick = { yr -> navController.navigate(AppRoutes.honoursYear(yr)) },
                            onPdfLibraryClick = { navigateTopTab(AppRoutes.PDF_LIBRARY) },
                            onPreviousQuestionsClick = { navController.navigate(AppRoutes.PREVIOUS_QUESTIONS) },
                            onSuggestionsClick = { navController.navigate(AppRoutes.SUGGESTIONS) },
                            onMcqPracticeClick = { navController.navigate(AppRoutes.MCQ_PRACTICE) },
                            onModelTestClick = { navController.navigate(AppRoutes.MODEL_TEST) },
                            onBookmarksClick = { navigateTopTab(AppRoutes.BOOKMARKS) },
                            onDownloadsClick = { navController.navigate(AppRoutes.DOWNLOAD_MANAGER) },
                            onSubjectClick = { subjId -> navController.navigate(AppRoutes.subjectDetails(subjId)) },
                            onPdfClick = { pdfId -> navController.navigate(AppRoutes.pdfReader(pdfId)) },
                            onAboutDeveloperClick = { navController.navigate(AppRoutes.ABOUT_DEVELOPER) }
                        )
                    }

                    composable(AppRoutes.GLOBAL_SEARCH) {
                        GlobalSearchScreen(
                            departments = departments,
                            subjects = subjects,
                            chapters = chapters,
                            pdfs = pdfs,
                            questions = questions,
                            searchHistory = searchHistory,
                            onBackClick = { navController.popBackStack() },
                            onRecordSearch = { mainViewModel.addSearchQuery(it) },
                            onClearHistory = { mainViewModel.clearSearchHistory() },
                            onDepartmentClick = { deptId -> navController.navigate(AppRoutes.departmentPdfs(deptId)) },
                            onSubjectClick = { subjId -> navController.navigate(AppRoutes.subjectDetails(subjId)) },
                            onChapterClick = { sId, cId -> navController.navigate(AppRoutes.chapterDetails(sId, cId)) },
                            onPdfClick = { pdfId -> navController.navigate(AppRoutes.pdfReader(pdfId)) },
                            onAskAiClick = { q -> navigateToAiSolverWithPrompt(q) }
                        )
                    }

                    composable(AppRoutes.POLITICAL_SCIENCE) {
                        PoliticalScienceDepartmentScreen(
                            subjects = subjects,
                            onBackClick = { navController.popBackStack() },
                            onYearClick = { yr -> navController.navigate(AppRoutes.honoursYear(yr)) },
                            onAskAiClick = { navigateTopTab(AppRoutes.AI_SOLVER) },
                            onPdfLibraryClick = { navController.navigate(AppRoutes.departmentPdfs("political_science")) }
                        )
                    }

                    composable(
                        route = AppRoutes.HONOURS_YEAR,
                        arguments = listOf(navArgument("year") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val year = backStackEntry.arguments?.getInt("year") ?: 1
                        HonoursYearScreen(
                            yearNumber = year,
                            subjects = subjects,
                            pdfs = pdfs,
                            questions = questions,
                            onBackClick = { navController.popBackStack() },
                            onSubjectClick = { subjId -> navController.navigate(AppRoutes.subjectDetails(subjId)) },
                            onPdfClick = { pdfId -> navController.navigate(AppRoutes.pdfReader(pdfId)) },
                            onPreviousQuestionsClick = { navController.navigate(AppRoutes.PREVIOUS_QUESTIONS) },
                            onSuggestionsClick = { navController.navigate(AppRoutes.SUGGESTIONS) },
                            onMcqClick = { navController.navigate(AppRoutes.MCQ_PRACTICE) },
                            onAskAiClick = { navigateToAiSolverWithPrompt("Political Science Honours Year $year question: ") }
                        )
                    }

                    composable(
                        route = AppRoutes.SUBJECT_DETAILS,
                        arguments = listOf(navArgument("subjectId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val subjectId = backStackEntry.arguments?.getString("subjectId") ?: ""
                        val subject = subjects.firstOrNull { it.id == subjectId }
                        SubjectDetailsScreen(
                            subject = subject,
                            chapters = chapters,
                            pdfs = pdfs,
                            onBackClick = { navController.popBackStack() },
                            onRecordRecent = { mainViewModel.recordRecentSubject(it) },
                            onChapterClick = { sId, cId -> navController.navigate(AppRoutes.chapterDetails(sId, cId)) },
                            onPdfClick = { pdfId -> navController.navigate(AppRoutes.pdfReader(pdfId)) },
                            onAskAiForSubject = { subj ->
                                navigateToAiSolverWithPrompt("Subject: ${subj.titleBn} (${subj.courseCode}) — ")
                            },
                            onStartMcqClick = { navController.navigate(AppRoutes.MCQ_PRACTICE) }
                        )
                    }

                    composable(
                        route = AppRoutes.CHAPTER_DETAILS,
                        arguments = listOf(
                            navArgument("subjectId") { type = NavType.StringType },
                            navArgument("chapterId") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val subjectId = backStackEntry.arguments?.getString("subjectId") ?: ""
                        val chapterId = backStackEntry.arguments?.getString("chapterId") ?: ""
                        val subject = subjects.firstOrNull { it.id == subjectId }
                        val chapter = chapters.firstOrNull { it.id == chapterId }
                        val isBookmarked = bookmarks.any { it.itemType == "chapter" && it.targetId == chapterId }

                        ChapterDetailsScreen(
                            subject = subject,
                            chapter = chapter,
                            isBookmarked = isBookmarked,
                            onBackClick = { navController.popBackStack() },
                            onToggleBookmark = { ch ->
                                mainViewModel.toggleBookmark(
                                    itemType = "chapter",
                                    targetId = ch.id,
                                    title = ch.titleBn,
                                    subtitle = subject?.titleBn ?: "Political Science"
                                )
                            },
                            onExplainWithAi = { ch ->
                                navigateToAiSolverWithPrompt("Explain chapter '${ch.titleBn}' (${subject?.titleBn.orEmpty()}) for NU Honours exam.")
                            },
                            onSubmitReport = { targetId, reason, details ->
                                mainViewModel.submitReport("chapter", targetId, reason, details)
                            }
                        )
                    }

                    composable(AppRoutes.AI_SOLVER) {
                        AiQuestionSolverScreen(
                            subjects = subjects,
                            savedAnswersState = savedAnswersState,
                            solveState = solveState,
                            attachedImages = attachedImages,
                            remoteConfig = aiRemoteConfig,
                            isOnline = isOnline,
                            initialPrompt = pendingAiPrompt,
                            onAttachImages = { aiViewModel.setAttachedBitmaps(it) },
                            onClearImages = { aiViewModel.clearAttachedImages() },
                            onSolveQuestion = { q, yrLabel, subjName, len, lang ->
                                val contextSnippet = buildString {
                                    subjects.firstOrNull { it.titleBn == subjName }?.let { s ->
                                        append("Course: ${s.titleBn} (${s.courseCode}). ${s.syllabusNote}")
                                    }
                                }
                                aiViewModel.solveQuestion(
                                    questionText = q,
                                    yearLabel = yrLabel,
                                    subjectName = subjName,
                                    answerLength = len,
                                    language = lang,
                                    verifiedContext = contextSnippet,
                                    remoteConfig = aiRemoteConfig,
                                    onSolved = { navController.navigate(AppRoutes.ANSWER_DETAILS) }
                                )
                            },
                            onOpenAnswerDetails = { ans ->
                                aiViewModel.selectAnswer(ans)
                                navController.navigate(AppRoutes.ANSWER_DETAILS)
                            },
                            onDeleteSavedAnswer = { aiViewModel.deleteSavedAnswer(it) },
                            onOpenAiChat = { navController.navigate(AppRoutes.AI_CHAT) },
                            onOpenImageUploadScreen = { navController.navigate(AppRoutes.QUESTION_IMAGE_UPLOAD) }
                        )
                    }

                    composable(AppRoutes.QUESTION_IMAGE_UPLOAD) {
                        QuestionImageUploadScreen(
                            attachedImages = attachedImages,
                            solveState = solveState,
                            isOnline = isOnline,
                            onBackClick = { navController.popBackStack() },
                            onAttachImages = { aiViewModel.setAttachedBitmaps(it) },
                            onClearImages = { aiViewModel.clearAttachedImages() },
                            onSolveMultimodal = { notes, yrLabel, subjName, len ->
                                aiViewModel.solveQuestion(
                                    questionText = notes,
                                    yearLabel = yrLabel,
                                    subjectName = subjName,
                                    answerLength = len,
                                    language = "bn",
                                    verifiedContext = "",
                                    remoteConfig = aiRemoteConfig,
                                    onSolved = { navController.navigate(AppRoutes.ANSWER_DETAILS) }
                                )
                            }
                        )
                    }

                    composable(AppRoutes.AI_CHAT) {
                        AiChatScreen(
                            conversations = conversations,
                            activeMessages = activeMessages,
                            streamingText = streamingChatText,
                            isStreaming = isChatStreaming,
                            chatError = chatError,
                            isOnline = isOnline,
                            initialPrompt = pendingAiPrompt,
                            onBackClick = { navController.popBackStack() },
                            onSelectConversation = { aiViewModel.selectConversation(it) },
                            onDeleteConversation = { aiViewModel.deleteConversation(it) },
                            onSendMessage = { prompt, mode, yrLabel, yrNum, subjName ->
                                pendingAiPrompt = ""
                                aiViewModel.sendTutorMessage(
                                    promptText = prompt,
                                    mode = mode,
                                    yearLabel = yrLabel,
                                    yearNumber = yrNum,
                                    subjectName = subjName,
                                    remoteConfig = aiRemoteConfig
                                )
                            }
                        )
                    }

                    composable(AppRoutes.ANSWER_DETAILS) {
                        AnswerDetailsScreen(
                            answer = selectedAnswer,
                            onBackClick = { navController.popBackStack() },
                            onToggleBookmark = { aiViewModel.toggleBookmarkOnSelectedAnswer() },
                            onExportPdf = { aiViewModel.exportSelectedAnswerPdf(context) },
                            onFollowUpClick = { followUp ->
                                pendingAiPrompt = "Follow up on: $followUp"
                                navController.navigate(AppRoutes.AI_CHAT)
                            },
                            onSubmitReport = { targetId, reason, details ->
                                mainViewModel.submitReport("ai_answer", targetId, reason, details)
                            }
                        )
                    }

                    composable(AppRoutes.PDF_LIBRARY) {
                        AllDepartmentsPdfLibraryScreen(
                            departments = departments,
                            pdfs = pdfs,
                            onDepartmentClick = { deptId -> navController.navigate(AppRoutes.departmentPdfs(deptId)) },
                            onOpenDownloads = { navController.navigate(AppRoutes.DOWNLOAD_MANAGER) },
                            onOpenPdfReader = { pdfId -> navController.navigate(AppRoutes.pdfReader(pdfId)) }
                        )
                    }

                    composable(
                        route = AppRoutes.DEPARTMENT_PDFS,
                        arguments = listOf(navArgument("departmentId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val deptId = backStackEntry.arguments?.getString("departmentId") ?: ""
                        val dept = departments.firstOrNull { it.id == deptId }
                        DepartmentPdfScreen(
                            department = dept,
                            pdfs = pdfs,
                            downloadStates = downloadStates,
                            isDownloadedCheck = { academicViewModel.isPdfDownloaded(it) },
                            offlineRefreshTick = offlineRefreshTick,
                            onBackClick = { navController.popBackStack() },
                            onOpenPdfReader = { pdfId -> navController.navigate(AppRoutes.pdfReader(pdfId)) },
                            onDownloadPdf = { academicViewModel.startPdfDownload(it) },
                            onCancelDownload = { academicViewModel.cancelPdfDownload(it) }
                        )
                    }

                    composable(
                        route = AppRoutes.PDF_READER,
                        arguments = listOf(navArgument("pdfId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val pdfId = backStackEntry.arguments?.getString("pdfId") ?: ""
                        val pdf = pdfs.firstOrNull { it.id == pdfId }
                        val isBookmarked = bookmarks.any { it.targetId == pdfId && (it.itemType == "pdf" || it.itemType == "pdf_page") }
                        PdfReaderScreen(
                            pdf = pdf,
                            isBookmarked = isBookmarked,
                            ensureLocalPdf = { academicViewModel.ensureLocalPdfReady(it) },
                            getInitialPage = { academicViewModel.getSavedPageProgress(it) },
                            onSavePageProgress = { id, page -> academicViewModel.savePageProgress(id, page) },
                            onRecordRecentPdf = { mainViewModel.recordRecentPdf(it) },
                            onTogglePageBookmark = { res, pageNum ->
                                mainViewModel.toggleBookmark(
                                    itemType = "pdf_page",
                                    targetId = res.id,
                                    title = res.title,
                                    subtitle = res.departmentName,
                                    pageNumber = pageNum.toLong()
                                )
                            },
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable(AppRoutes.DOWNLOAD_MANAGER) {
                        DownloadManagerScreen(
                            pdfs = pdfs,
                            downloadStates = downloadStates,
                            isDownloadedCheck = { academicViewModel.isPdfDownloaded(it) },
                            offlineRefreshTick = offlineRefreshTick,
                            onBackClick = { navController.popBackStack() },
                            onOpenPdf = { pdfId -> navController.navigate(AppRoutes.pdfReader(pdfId)) },
                            onRetryDownload = { academicViewModel.startPdfDownload(it) },
                            onCancelDownload = { academicViewModel.cancelPdfDownload(it) },
                            onDeleteOfflinePdf = { academicViewModel.deleteOfflinePdf(it) }
                        )
                    }

                    composable(AppRoutes.PREVIOUS_QUESTIONS) {
                        PreviousQuestionScreen(
                            questions = questions,
                            onBack = { navController.popBackStack() },
                            onSolveWithAi = { prompt -> navigateToAiSolverWithPrompt(prompt) },
                            onSubmitReport = { type, id, reason, details ->
                                mainViewModel.submitReport(type, id, reason, details)
                            }
                        )
                    }

                    composable(AppRoutes.SUGGESTIONS) {
                        SuggestionScreen(
                            questions = questions,
                            bookmarks = bookmarks,
                            onBack = { navController.popBackStack() },
                            onToggleBookmark = { item ->
                                mainViewModel.toggleBookmark(
                                    itemType = "suggestion",
                                    targetId = item.id,
                                    title = item.prompt,
                                    subtitle = item.subjectName
                                )
                            },
                            onSolveWithAi = { prompt -> navigateToAiSolverWithPrompt(prompt) }
                        )
                    }

                    composable(AppRoutes.MCQ_PRACTICE) {
                        McqPracticeScreen(
                            questions = questions,
                            generatedMcqsState = generatedMcqsState,
                            remoteConfig = aiRemoteConfig,
                            onBack = { navController.popBackStack() },
                            onGenerateAiMcqs = { topic, yrLabel, count, lang, cfg ->
                                aiViewModel.generateMcqs(topic, yrLabel, count, lang, cfg)
                            }
                        )
                    }

                    composable(AppRoutes.MODEL_TEST) {
                        ModelTestScreen(
                            modelTests = modelTests,
                            questions = questions,
                            testHistory = testResults,
                            onBack = { navController.popBackStack() },
                            onSaveResult = { academicViewModel.recordModelTestResult(it) }
                        )
                    }

                    composable(AppRoutes.BOOKMARKS) {
                        BookmarkScreen(
                            bookmarks = bookmarks,
                            savedAnswers = savedAnswers,
                            onOpenSavedAnswer = { ans ->
                                aiViewModel.selectAnswer(ans)
                                navController.navigate(AppRoutes.ANSWER_DETAILS)
                            },
                            onOpenPdf = { pdfId -> navController.navigate(AppRoutes.pdfReader(pdfId)) },
                            onRemoveBookmark = { mainViewModel.removeBookmarkById(it) },
                            onDeleteSavedAnswer = { aiViewModel.deleteSavedAnswer(it) }
                        )
                    }

                    composable(AppRoutes.PROFILE) {
                        ProfileScreen(
                            profile = userProfile,
                            departments = departments,
                            bookmarksCount = bookmarks.size,
                            savedAnswersCount = savedAnswers.size,
                            testResults = testResults,
                            isBackendAdmin = isBackendAdmin,
                            onSavePreferences = { name, dId, dName, yr, lang ->
                                mainViewModel.saveProfilePreferences(name, dId, dName, yr, lang)
                            },
                            onOpenDownloads = { navController.navigate(AppRoutes.DOWNLOAD_MANAGER) },
                            onOpenSettings = { navController.navigate(AppRoutes.SETTINGS) },
                            onOpenAdminPanel = { navigateToAdminGate() },
                            onOpenAboutDeveloper = { navController.navigate(AppRoutes.ABOUT_DEVELOPER) },
                            onSignOut = {
                                signOut(credentialManager, scope)
                            }
                        )
                    }

                    composable(AppRoutes.SETTINGS) {
                        SettingsScreen(
                            themeMode = themeMode,
                            languagePref = languagePref,
                            updateState = updateState,
                            onBack = { navController.popBackStack() },
                            onThemeModeChange = { mainViewModel.setThemeMode(it) },
                            onLanguageChange = { mainViewModel.setLanguagePref(it) },
                            onCheckUpdates = { mainViewModel.checkForAppUpdates() }
                        )
                    }

                    composable(AppRoutes.ABOUT_DEVELOPER) {
                        AboutDeveloperScreen(onBack = { navController.popBackStack() })
                    }

                    composable(AppRoutes.ADMIN_PASSWORD) {
                        LaunchedEffect(isAdminAuthorized) {
                            if (isAdminAuthorized) {
                                navController.navigate(AppRoutes.ADMIN_DASHBOARD) {
                                    popUpTo(AppRoutes.ADMIN_PASSWORD) { inclusive = true }
                                }
                            }
                        }
                        AdminPasswordScreen(
                            isBackendAdminAccount = isAdminAccount,
                            hasPinConfigured = hasPinConfigured,
                            lockoutState = lockoutState,
                            isVerifying = isAdminVerifying,
                            authError = adminAuthError,
                            onBack = { navController.popBackStack() },
                            onRefreshStatus = { adminViewModel.refreshAdminStatus() },
                            onVerifyPin = { adminViewModel.verifyAndUnlock(it) },
                            onInitializePin = { adminViewModel.initializeBackendPin(it) }
                        )
                    }

                    composable(AppRoutes.ADMIN_DASHBOARD) {
                        LaunchedEffect(isAdminAuthorized) {
                            if (!isAdminAuthorized) {
                                navController.popBackStack()
                            }
                        }
                        val openReportsCount = (reportsState as? UiState.Success)?.data?.count { it.status == "open" } ?: 0
                        AdminDashboardScreen(
                            departmentsCount = departments.size,
                            subjectsCount = subjects.size,
                            pdfsCount = pdfs.size,
                            openReportsCount = openReportsCount,
                            remoteConfig = aiRemoteConfig,
                            adminLogs = adminLogs,
                            onBack = { navController.popBackStack() },
                            onLockPanel = {
                                adminViewModel.lockAdminPanel()
                                navController.popBackStack()
                            },
                            onSeedCatalog = { adminViewModel.seedStarterCatalog() },
                            onSaveAiConfig = { adminViewModel.saveAiConfig(it) },
                            onNavigatePdfUpload = { navController.navigate(AppRoutes.ADMIN_PDF_UPLOAD) },
                            onNavigateContentEditor = { navController.navigate(AppRoutes.ADMIN_CONTENT_EDITOR) },
                            onNavigateAnnouncements = { navController.navigate(AppRoutes.ADMIN_ANNOUNCEMENTS) },
                            onNavigateReports = { navController.navigate(AppRoutes.ADMIN_REPORTS) }
                        )
                    }

                    composable(AppRoutes.ADMIN_PDF_UPLOAD) {
                        AdminPdfUploadScreen(
                            departments = departments,
                            pdfs = pdfs,
                            onBack = { navController.popBackStack() },
                            onUploadPdf = { pdf, uri -> adminViewModel.uploadPdfResource(pdf, uri) },
                            onDeletePdf = { adminViewModel.deletePdf(it) }
                        )
                    }

                    composable(AppRoutes.ADMIN_CONTENT_EDITOR) {
                        AdminContentEditorScreen(
                            departments = departments,
                            subjects = subjects,
                            chapters = chapters,
                            questions = questions,
                            onBack = { navController.popBackStack() },
                            onSaveDepartment = { adminViewModel.saveDepartment(it) },
                            onDeleteDepartment = { adminViewModel.deleteDepartment(it) },
                            onSaveSubject = { adminViewModel.saveSubject(it) },
                            onDeleteSubject = { adminViewModel.deleteSubject(it) },
                            onSaveChapter = { adminViewModel.saveChapter(it) },
                            onSaveQuestion = { adminViewModel.saveQuestion(it) }
                        )
                    }

                    composable(AppRoutes.ADMIN_ANNOUNCEMENTS) {
                        AdminAnnouncementScreen(
                            announcements = announcements,
                            onBack = { navController.popBackStack() },
                            onSaveAnnouncement = { adminViewModel.saveAnnouncement(it) },
                            onDeleteAnnouncement = { adminViewModel.deleteAnnouncement(it) }
                        )
                    }

                    composable(AppRoutes.ADMIN_REPORTS) {
                        AdminReportScreen(
                            reportsState = reportsState,
                            onBack = { navController.popBackStack() },
                            onUpdateReport = { id, st, note -> adminViewModel.updateReportStatus(id, st, note) }
                        )
                    }
                }
            }
        }
    }
}
