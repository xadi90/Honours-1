package com.example

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import com.example.data.catalog.AcademicSeedCatalog
import com.example.data.model.AdminUserDirectoryEntry
import com.example.data.model.AdminUserUsageSummary
import com.example.data.model.AiRemoteConfig
import com.example.data.model.AuthPolicyConfig
import com.example.data.model.CatalogMetaConfig
import com.example.data.model.Department
import com.example.data.repository.mergeCatalogItems
import com.example.ui.screens.admin.AdminContentEditorScreen
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.theme.HonoursSolutionTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HonoursPdfAndAdminStatsTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `all 22 honours departments have pdf resources across years 1 to 4`() {
        val depts = AcademicSeedCatalog.defaultDepartments
        val pdfs = AcademicSeedCatalog.starterPdfs

        assertEquals(22, depts.size)
        for (dept in depts) {
            val deptPdfs = pdfs.filter { it.departmentId == dept.id }
            assertTrue("Expected PDFs for department ${dept.id}", deptPdfs.isNotEmpty())
            val coveredYears = deptPdfs.map { it.yearNumber }.toSet()
            assertTrue(
                "Expected Years 1..4 PDFs for ${dept.id}, found $coveredYears",
                coveredYears.containsAll(listOf(1L, 2L, 3L, 4L))
            )
        }
    }

    @Test
    fun `mergeCatalogItems auto syncs live edits and hides drafts or deleted starter items`() {
        val starter = listOf(
            Department(id = "dept_1", nameBn = "বিভাগ ১", nameEn = "Dept 1"),
            Department(id = "dept_2", nameBn = "বিভাগ ২", nameEn = "Dept 2"),
            Department(id = "dept_3", nameBn = "বিভাগ ৩", nameEn = "Dept 3")
        )
        val livePublished = listOf(
            Department(id = "dept_1", nameBn = "আপডেটেড বিভাগ ১", nameEn = "Updated Dept 1"),
            Department(id = "dept_4", nameBn = "নতুন বিভাগ ৪", nameEn = "New Dept 4")
        )
        val meta = CatalogMetaConfig(
            draftIds = listOf("dept_2"),
            deletedIds = listOf("dept_3")
        )

        val studentView = mergeCatalogItems(livePublished, starter, meta, includeDrafts = false) { it.id }
        assertEquals(listOf("dept_1", "dept_4"), studentView.map { it.id })
        assertEquals("আপডেটেড বিভাগ ১", studentView.first().nameBn)
        assertFalse(studentView.any { it.id == "dept_2" || it.id == "dept_3" })

        val adminLiveAll = livePublished + Department(id = "dept_2", nameBn = "বিভাগ ২", nameEn = "Dept 2", status = "draft")
        val adminView = mergeCatalogItems(adminLiveAll, starter, meta, includeDrafts = true) { it.id }
        assertEquals(listOf("dept_1", "dept_4", "dept_2"), adminView.map { it.id })
    }

    @Test
    fun `admin dashboard displays user usage analytics card and seed all pdfs action`() {
        val summary = AdminUserUsageSummary(
            totalUsers = 148,
            activeTodayCount = 42,
            activeLast7DaysCount = 97,
            newUsersLast7DaysCount = 19,
            totalAiQueriesUsed = 620L,
            departmentCounts = listOf("রাষ্ট্রবিজ্ঞান" to 92, "বাংলা" to 24),
            yearCounts = listOf(1L to 60, 2L to 40, 3L to 30, 4L to 18)
        )
        composeRule.setContent {
            HonoursSolutionTheme {
                AdminDashboardScreen(
                    departmentsCount = 22,
                    subjectsCount = 9,
                    pdfsCount = 90,
                    openReportsCount = 1,
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
                    userUsageSummary = summary
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("admin_user_usage_card", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag("admin_total_users_count", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText("148", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText("42", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag("admin_dashboard_list")
            .performScrollToNode(hasTestTag("admin_seed_all_pdfs_button"))
        composeRule.onNodeWithTag("admin_seed_all_pdfs_button", useUnmergedTree = true).assertExists()
    }

    @Test
    fun `admin content editor renders auto sync banner and department picker for subjects`() {
        composeRule.setContent {
            HonoursSolutionTheme {
                AdminContentEditorScreen(
                    departments = AcademicSeedCatalog.defaultDepartments,
                    subjects = AcademicSeedCatalog.starterSubjects,
                    chapters = AcademicSeedCatalog.starterChapters,
                    questions = AcademicSeedCatalog.starterQuestions,
                    onBack = {},
                    onSaveDepartment = {},
                    onDeleteDepartment = {},
                    onSaveSubject = {},
                    onDeleteSubject = {},
                    onSaveChapter = {},
                    onSaveQuestion = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("admin_live_sync_banner", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText("বিষয়").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("admin_subject_dept_picker", useUnmergedTree = true).assertExists()
    }

    @Test
    fun `admin dashboard reveals registered user names and emails with search and otp policy toggle`() {
        val directoryUsers = listOf(
            AdminUserDirectoryEntry(
                userId = "u1",
                displayName = "Rafiqul Islam",
                email = "rafiq.nu@gmail.com",
                selectedDepartmentName = "রাষ্ট্রবিজ্ঞান",
                selectedYear = 2L,
                aiUsageCount = 12L
            ),
            AdminUserDirectoryEntry(
                userId = "u2",
                displayName = "Nusrat Jahan",
                email = "nusrat.honours@gmail.com",
                selectedDepartmentName = "বাংলা",
                selectedYear = 3L,
                aiUsageCount = 5L
            )
        )
        val summary = AdminUserUsageSummary(
            totalUsers = 2,
            activeTodayCount = 2,
            activeLast7DaysCount = 2,
            newUsersLast7DaysCount = 2,
            totalAiQueriesUsed = 17L,
            users = directoryUsers
        )
        var savedPolicy: AuthPolicyConfig? = null
        composeRule.setContent {
            HonoursSolutionTheme {
                AdminDashboardScreen(
                    departmentsCount = 22,
                    subjectsCount = 9,
                    pdfsCount = 90,
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
                    userUsageSummary = summary,
                    authPolicy = AuthPolicyConfig(requireEmailOtp = true),
                    onSaveAuthPolicy = { savedPolicy = it }
                )
            }
        }
        composeRule.waitForIdle()
        // Directory list is initially collapsed until Admin expands it
        composeRule.onNodeWithTag("admin_user_directory_list").assertDoesNotExist()
        composeRule.onNodeWithTag("admin_user_directory_toggle")
            .performScrollTo()
            .performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("admin_user_directory_list", useUnmergedTree = true)
            .performScrollTo()
            .assertExists()
        composeRule.onNodeWithText("Rafiqul Islam", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText("rafiq.nu@gmail.com", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText("Nusrat Jahan", useUnmergedTree = true).assertExists()

        // Filter directory by email
        composeRule.onNodeWithTag("admin_user_directory_search")
            .performScrollTo()
            .performTextInput("nusrat")
        composeRule.waitForIdle()
        composeRule.onNodeWithText("nusrat.honours@gmail.com", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText("rafiq.nu@gmail.com").assertDoesNotExist()

        // Scroll to Email OTP policy switch and verify callback
        composeRule.onNodeWithTag("admin_dashboard_list")
            .performScrollToNode(hasTestTag("admin_email_otp_policy_switch"))
        composeRule.onNodeWithTag("admin_email_otp_policy_switch")
            .performScrollTo()
            .performClick()
        composeRule.waitForIdle()
        assertEquals(false, savedPolicy?.requireEmailOtp)
    }
}
