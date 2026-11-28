package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
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

        composeRule.onNodeWithText("মডারেটর").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("college_moderator_email_input").assertExists()
        composeRule.onNodeWithTag("college_moderator_add_button").assertExists()
    }

    @Test
    fun `college moderator mode hides moderator management tab and shows scoped subtitle`() {
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
                    onSeedStarterTemplate = {},
                    isMainAdmin = false
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Sujat Ali Honours College • কলেজ মডারেটর মোড", useUnmergedTree = true)
            .assertExists()
        composeRule.onNodeWithText("মডারেটর").assertDoesNotExist()
    }

    @Test
    fun `college screen renders hero and section images when configured`() {
        val sampleDataUri = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
        val sectionsWithImage = listOf(
            CollegeSeedCatalog.starterSections.first().copy(
                id = "notice_with_photo",
                imageUrl = sampleDataUri
            )
        )
        composeRule.setContent {
            HonoursSolutionTheme {
                SujatAliCollegeScreen(
                    config = CollegeProfileConfig(heroImageUrl = sampleDataUri),
                    sections = sectionsWithImage,
                    isBackendAdmin = false,
                    onBackClick = {},
                    onCustomizeClick = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("college_hero_image", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag("college_content_list")
            .performScrollToNode(hasTestTag("college_section_card_notice_with_photo"))
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("college_section_image_notice_with_photo", useUnmergedTree = true)
            .assertExists()
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class EmailOtpVerificationRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `email masking and image url validation work safely`() {
        assertEquals("xa***@gmail.com", com.example.core.EmailOtpService.maskEmail("xadiahmed7@gmail.com"))
        assertTrue(com.example.core.isValidCollegeImageInput("https://example.com/photo.jpg"))
        assertTrue(com.example.core.isValidCollegeImageInput("data:image/jpeg;base64,/9j/4AAQ"))
        assertFalse(com.example.core.isValidCollegeImageInput("http://insecure.example.com/photo.jpg"))
    }

    @Test
    fun `5-digit otp verification succeeds and admin bypass is hidden for regular students`() {
        var verifiedCalled = false
        composeRule.setContent {
            HonoursSolutionTheme {
                com.example.ui.screens.auth.EmailOtpVerificationScreen(
                    email = "student@gmail.com",
                    isBootstrappedAdmin = false,
                    onVerified = { verifiedCalled = true },
                    onSignOut = {},
                    autoSendOnLaunch = true,
                    sendOtp = {
                        com.example.core.OtpSendResult.ServiceUnavailable(
                            message = "Not deployed",
                            isFunctionNotDeployed = true
                        )
                    },
                    verifyOtp = { code ->
                        if (code == "54321") {
                            com.example.core.OtpVerifyResult.Verified
                        } else {
                            com.example.core.OtpVerifyResult.InvalidCode("ভুল কোড!")
                        }
                    }
                )
            }
        }
        composeRule.waitForIdle()
        // Student never sees admin bypass button even on HTTP 404, and gets non-blocking continue option
        composeRule.onNodeWithTag("email_otp_admin_bypass_button").assertDoesNotExist()
        composeRule.onNodeWithTag("email_otp_unconfigured_continue_button", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText("st***@gmail.com", substring = true, useUnmergedTree = true).assertExists()
        assertFalse(com.example.data.model.AuthPolicyConfig().requireEmailOtp)
        assertTrue(
            com.example.data.model.AuthPolicyConfig(
                requireEmailOtp = true,
                emailJsServiceId = "svc_1",
                emailJsTemplateId = "tpl_1",
                emailJsPublicKey = "pub_1"
            ).hasEmailJsConfigured
        )

        // Enter wrong code first -> verify error banner appears
        composeRule.onNodeWithTag("email_otp_input").performTextInput("11111")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("email_otp_verify_button").performClick()
        composeRule.waitForIdle()
        assertFalse(verifiedCalled)
        composeRule.onNodeWithTag("email_otp_error_banner", useUnmergedTree = true).assertExists()
    }

    @Test
    fun `5-digit otp verification triggers onVerified and admin sees bypass on 404`() {
        var verifiedCalled = false
        composeRule.setContent {
            HonoursSolutionTheme {
                com.example.ui.screens.auth.EmailOtpVerificationScreen(
                    email = "xadiahmed7@gmail.com",
                    isBootstrappedAdmin = true,
                    onVerified = { verifiedCalled = true },
                    onSignOut = {},
                    autoSendOnLaunch = true,
                    sendOtp = {
                        com.example.core.OtpSendResult.ServiceUnavailable(
                            message = "Not deployed",
                            isFunctionNotDeployed = true
                        )
                    },
                    verifyOtp = { code ->
                        if (code == "54321") {
                            com.example.core.OtpVerifyResult.Verified
                        } else {
                            com.example.core.OtpVerifyResult.InvalidCode("ভুল কোড!")
                        }
                    }
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("email_otp_admin_bypass_button", useUnmergedTree = true).assertExists()

        composeRule.onNodeWithTag("email_otp_input").performTextInput("54321")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("email_otp_verify_button").performClick()
        composeRule.waitForIdle()
        assertTrue(verifiedCalled)
    }
}

