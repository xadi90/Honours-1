package com.example

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.data.catalog.AcademicSeedCatalog
import com.example.data.model.AiLimitConfig
import com.example.data.model.AiQuotaState
import com.example.data.model.AiRemoteConfig
import com.example.data.model.DeveloperProfileConfig
import com.example.data.model.UiState
import com.example.ui.screens.admin.AdminAiLimitScreen
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.ai.AiLimitPurchaseSheet
import com.example.ui.screens.ai.AiQuotaBannerCard
import com.example.ui.screens.auth.FirebaseConfigErrorScreen
import com.example.ui.screens.home.HomeDashboardScreen
import com.example.ui.screens.profile.AboutDeveloperScreen
import com.example.ui.theme.HonoursSolutionTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AppStartupRobolectricTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `main activity transitions from splash to onboarding to auth without crashing`() {
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(2500L)
        composeRule.waitForIdle()
        val skipNodes = composeRule.onAllNodes(hasTestTag("onboarding_skip_button")).fetchSemanticsNodes()
        if (skipNodes.isNotEmpty()) {
            composeRule.onNodeWithTag("onboarding_skip_button").performClick()
            composeRule.waitForIdle()
        }
        composeRule.onNodeWithTag("google_sign_in_button").assertExists()
        composeRule.onNodeWithTag("offline_syllabus_preview_button").performClick()
        composeRule.waitForIdle()
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HomeAndFallbackRobolectricTest {

    @get:Rule
    val composeRule = androidx.compose.ui.test.junit4.createComposeRule()

    @Test
    fun `home dashboard renders starter content cleanly`() {
        composeRule.setContent {
            HonoursSolutionTheme {
                HomeDashboardScreen(
                    profile = null,
                    subjects = AcademicSeedCatalog.starterSubjects,
                    pdfs = AcademicSeedCatalog.starterPdfs,
                    announcements = AcademicSeedCatalog.starterAnnouncements,
                    recentPdfIds = emptyList(),
                    recentSubjectIds = emptyList(),
                    bookmarksCount = 0,
                    onOpenDrawer = {},
                    onSearchClick = {},
                    onAiSolverClick = {},
                    onPoliticalScienceClick = {},
                    onYearClick = {},
                    onPdfLibraryClick = {},
                    onPreviousQuestionsClick = {},
                    onSuggestionsClick = {},
                    onMcqPracticeClick = {},
                    onModelTestClick = {},
                    onBookmarksClick = {},
                    onDownloadsClick = {},
                    onSubjectClick = {},
                    onPdfClick = {},
                    onAboutDeveloperClick = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_search_bar").assertExists()
        composeRule.onNodeWithTag("home_ai_solver_banner").assertExists()
    }

    @Test
    fun `firebase fallback screen renders cleanly`() {
        composeRule.setContent {
            HonoursSolutionTheme {
                FirebaseConfigErrorScreen(onRetry = {})
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun `about developer and admin developer editor render cleanly`() {
        val customDev = DeveloperProfileConfig(
            name = "Xadi Ahmed",
            tagline = "NU Bangladesh",
            phone = "01878864913",
            email = "xadiahmed23@gmail.com"
        )
        composeRule.setContent {
            HonoursSolutionTheme {
                AboutDeveloperScreen(onBack = {}, developerProfile = customDev)
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("developer_call_button").assertExists()
        composeRule.onNodeWithTag("developer_email_button").assertExists()
    }

    @Test
    fun `admin dashboard renders developer info editor`() {
        composeRule.setContent {
            HonoursSolutionTheme {
                AdminDashboardScreen(
                    departmentsCount = 22,
                    subjectsCount = 10,
                    pdfsCount = 5,
                    openReportsCount = 0,
                    remoteConfig = AiRemoteConfig(),
                    adminLogs = emptyList(),
                    onBack = {},
                    onLockPanel = {},
                    onSeedCatalog = {},
                    onSaveAiConfig = {},
                    onNavigatePdfUpload = {},
                    onNavigateContentEditor = {},
                    onNavigateAnnouncements = {},
                    onNavigateReports = {},
                    developerProfile = DeveloperProfileConfig(),
                    onSaveDeveloperProfile = {},
                    pendingPurchasesCount = 2,
                    onNavigateAiLimits = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("admin_nav_ai_limits").assertExists()
    }

    @Test
    fun `ai quota banner renders cleanly`() {
        composeRule.setContent {
            HonoursSolutionTheme {
                AiQuotaBannerCard(
                    quota = AiQuotaState(freeLimit = 30L, purchasedCredits = 0L, usedCount = 10L),
                    onBuyLimitClick = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("buy_ai_limit_button").assertExists()
    }

    @Test
    fun `ai limit purchase sheet renders cleanly`() {
        composeRule.setContent {
            HonoursSolutionTheme {
                AiLimitPurchaseSheet(
                    quota = AiQuotaState(freeLimit = 30L, purchasedCredits = 0L, usedCount = 30L),
                    config = AiLimitConfig(),
                    myRequests = emptyList(),
                    onDismiss = {},
                    onSubmitRequest = { _, _, _, _, _, _ -> }
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("ai_package_pack_1").assertExists()
    }

    @Test
    fun `admin limit manager renders cleanly`() {
        composeRule.setContent {
            HonoursSolutionTheme {
                AdminAiLimitScreen(
                    config = AiLimitConfig(),
                    purchaseRequestsState = UiState.Success(emptyList()),
                    onBack = {},
                    onSaveConfig = {},
                    onReviewRequest = { _, _, _ -> }
                )
            }
        }
        composeRule.waitForIdle()
    }
}

