package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.data.catalog.CollegeSeedCatalog
import com.example.data.model.CollegeProfileConfig
import com.example.data.repository.AiRepository
import com.example.ui.screens.admin.AdminCollegeEditorScreen
import com.example.ui.screens.college.SujatAliCollegeScreen
import com.example.ui.theme.HonoursSolutionTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

class AiFallbackUnitTest {

    @Test
    fun `buildCandidateModels includes flash-lite fallback cleanly without duplicates`() {
        assertEquals(
            listOf("gemini-3.8-flash", "gemini-3.5-flash-lite"),
            AiRepository.buildCandidateModels("gemini-3.8-flash")
        )
        assertEquals(
            listOf("gemini-3.5-flash-lite", "gemini-3.8-flash"),
            AiRepository.buildCandidateModels("gemini-3.5-flash-lite")
        )
        assertEquals(
            listOf("gemini-3.8-flash", "gemini-3.5-flash-lite"),
            AiRepository.buildCandidateModels("")
        )
    }

    @Test
    fun `isTransientCapacityError detects high demand errors and excludes App Check`() {
        assertTrue(
            AiRepository.isTransientCapacityError(
                RuntimeException(
                    "This model is currently experiencing high demand. Spikes in demand are usually temporary. Please try again later."
                )
            )
        )
        assertTrue(AiRepository.isTransientCapacityError(RuntimeException("503 Service Unavailable")))
        assertTrue(AiRepository.isTransientCapacityError(RuntimeException("429 RESOURCE_EXHAUSTED")))
        assertFalse(
            AiRepository.isTransientCapacityError(
                RuntimeException("403 App Check token is invalid")
            )
        )
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SujatAliCollegeRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `student view renders college title and hides admin edit button for students`() {
        composeRule.setContent {
            HonoursSolutionTheme {
                SujatAliCollegeScreen(
                    config = CollegeProfileConfig(),
                    sections = CollegeSeedCatalog.starterSections,
                    isBackendAdmin = false,
                    onBackClick = {},
                    onCustomizeClick = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("সুজাত আলী অনার্স কলেজ, পিংনা", useUnmergedTree = true)
            .assertExists()
        composeRule.onNodeWithTag("college_admin_edit_button").assertDoesNotExist()
    }

    @Test
    fun `admin view shows customize shortcut button on college screen`() {
        composeRule.setContent {
            HonoursSolutionTheme {
                SujatAliCollegeScreen(
                    config = CollegeProfileConfig(),
                    sections = CollegeSeedCatalog.starterSections,
                    isBackendAdmin = true,
                    onBackClick = {},
                    onCustomizeClick = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("college_admin_edit_button").assertIsDisplayed()
    }

    @Test
    fun `admin college editor renders builder controls and switches tabs`() {
        composeRule.setContent {
            HonoursSolutionTheme {
                AdminCollegeEditorScreen(
                    config = CollegeProfileConfig(),
                    sections = CollegeSeedCatalog.starterSections,
                    onBack = {},
                    onPreviewCollege = {},
                    onSaveProfile = {},
                    onSaveSection = {},
                    onDeleteSection = {},
                    onMoveSection = { _, _ -> },
                    onToggleSectionStatus = {},
                    onSeedStarterTemplate = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("admin_college_add_block_button").assertExists()
        composeRule.onNodeWithTag("admin_college_seed_button").assertExists()

        composeRule.onNodeWithText("হেডার ও থিম").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("admin_college_save_theme_button").assertExists()

        composeRule.onNodeWithText("তথ্য ও যোগাযোগ").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("admin_college_save_contact_button").assertExists()
    }
}
