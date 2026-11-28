package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.core.isValidCollegeImageInput
import com.example.data.model.ChatUserProfile
import com.example.data.model.UserChatMessage
import com.example.data.model.UserChatThread
import com.example.data.repository.FirestoreChatRepository
import com.example.ui.screens.chat.UserChatListScreen
import com.example.ui.screens.chat.UserChatRoomScreen
import com.example.ui.theme.HonoursSolutionTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

class ChatRepositoryUnitTest {

    @Test
    fun `buildDirectChatId is deterministic regardless of participant order`() {
        val id1 = FirestoreChatRepository.buildDirectChatId("uid_alpha", "uid_beta")
        val id2 = FirestoreChatRepository.buildDirectChatId("uid_beta", "uid_alpha")
        assertEquals(id1, id2)
        assertEquals("dm_uid_alpha_uid_beta", id1)
    }

    @Test
    fun `UserChatThread partner helpers resolve partner metadata, unread count, and typing window`() {
        val now = System.currentTimeMillis()
        val thread = UserChatThread(
            id = "dm_u1_u2",
            participantIds = listOf("u1", "u2"),
            participantNames = mapOf("u1" to "Me", "u2" to "Nusrat"),
            participantDepts = mapOf("u1" to "History", "u2" to "Political Science"),
            unreadCounts = mapOf("u1" to 4L, "u2" to 0L),
            typingUsers = mapOf("u2" to (now - 1500L))
        )
        assertEquals("u2", thread.partnerUid("u1"))
        assertEquals("Nusrat", thread.partnerName("u1"))
        assertEquals("Political Science", thread.partnerDept("u1"))
        assertEquals(4L, thread.myUnreadCount("u1"))
        assertTrue(thread.isPartnerTyping("u1", now))
        assertFalse(thread.isPartnerTyping("u1", now + 20_000L))
    }

    @Test
    fun `isValidCollegeImageInput accepts https and base64 data URIs and rejects http`() {
        assertTrue(isValidCollegeImageInput(""))
        assertTrue(isValidCollegeImageInput("https://example.com/campus.jpg"))
        assertTrue(isValidCollegeImageInput("data:image/jpeg;base64,/9j/4AAQSkZJRg=="))
        assertTrue(isValidCollegeImageInput("data:image/png;base64,iVBORw0KGgo="))
        assertFalse(isValidCollegeImageInput("http://insecure.example.com/photo.jpg"))
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class UserChatUiRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `chat list renders threads with unread count and opens student directory on tab click`() {
        var openedPartnerId = ""
        val threads = listOf(
            UserChatThread(
                id = "dm_u1_u2",
                participantIds = listOf("u1", "u2"),
                participantNames = mapOf("u1" to "Me", "u2" to "Karim Uddin"),
                participantDepts = mapOf("u2" to "রাষ্ট্রবিজ্ঞান"),
                lastMessageText = "কাল ক্লাস হবে?",
                lastMessageSenderId = "u2",
                unreadCounts = mapOf("u1" to 2L)
            )
        )
        val users = listOf(
            ChatUserProfile(
                userId = "u2",
                displayName = "Karim Uddin",
                departmentName = "রাষ্ট্রবিজ্ঞান",
                yearNumber = 2L,
                isOnline = true
            ),
            ChatUserProfile(
                userId = "u3",
                displayName = "Farhana Akter",
                departmentName = "সমাজবিজ্ঞান",
                yearNumber = 3L,
                isOnline = false
            )
        )

        composeRule.setContent {
            HonoursSolutionTheme {
                UserChatListScreen(
                    currentUserId = "u1",
                    chats = threads,
                    directoryUsers = users,
                    isOpeningChat = false,
                    onBackClick = {},
                    onOpenThread = {},
                    onStartChatWithUser = { openedPartnerId = it.userId }
                )
            }
        }

        composeRule.onNodeWithText("Karim Uddin").assertIsDisplayed()
        composeRule.onNodeWithText("কাল ক্লাস হবে?").assertIsDisplayed()

        composeRule.onNodeWithTag("chat_tab_directory").performClick()
        composeRule.onNodeWithTag("chat_user_u3").assertIsDisplayed()
        composeRule.onNodeWithTag("chat_user_u3").performClick()

        assertEquals("u3", openedPartnerId)
    }

    @Test
    fun `chat room displays live typing status, reply quote, bubbles, and sends text`() {
        var sentMessage = ""
        val now = System.currentTimeMillis()
        val thread = UserChatThread(
            id = "dm_u1_u2",
            participantIds = listOf("u1", "u2"),
            participantNames = mapOf("u1" to "Ayesha", "u2" to "Tanvir Hasan"),
            typingUsers = mapOf("u2" to now)
        )
        val partner = ChatUserProfile(
            userId = "u2",
            displayName = "Tanvir Hasan",
            departmentName = "রাষ্ট্রবিজ্ঞান",
            yearNumber = 2L,
            isOnline = true
        )
        val messages = listOf(
            UserChatMessage(
                id = "m1",
                senderId = "u2",
                senderName = "Tanvir Hasan",
                text = "নোটসটা শেয়ার করবা?"
            ),
            UserChatMessage(
                id = "m2",
                senderId = "u1",
                senderName = "Ayesha",
                text = "হ্যাঁ, এখনই পাঠাচ্ছি!",
                replyToMessageId = "m1",
                replyToSenderName = "Tanvir Hasan",
                replyToPreview = "নোটসটা শেয়ার করবা?",
                readBy = listOf("u1", "u2")
            )
        )

        composeRule.setContent {
            HonoursSolutionTheme {
                UserChatRoomScreen(
                    currentUserId = "u1",
                    thread = thread,
                    partnerProfile = partner,
                    messages = messages,
                    replyingTo = messages.first(),
                    pendingImageDataUri = null,
                    isProcessingImage = false,
                    isSending = false,
                    onBackClick = {},
                    onComposerTextChanged = {},
                    onAttachPhotoUri = {},
                    onClearPendingPhoto = {},
                    onSelectReply = {},
                    onSendMessage = { sentMessage = it },
                    onMarkRead = {},
                    onDeleteMessageForAll = {},
                    onToggleBlock = {},
                    onReportUser = { _, _ -> }
                )
            }
        }

        composeRule.onNodeWithTag("chat_room_partner_name").assertIsDisplayed()
        composeRule.onNodeWithText("টাইপ করছে...").assertIsDisplayed()
        composeRule.onNodeWithTag("chat_reply_preview").assertIsDisplayed()
        composeRule.onNodeWithText("হ্যাঁ, এখনই পাঠাচ্ছি!").assertIsDisplayed()

        composeRule.onNodeWithTag("chat_message_input").performTextInput("নোটস আপলোড করেছি")
        composeRule.onNodeWithTag("chat_send_button").performClick()
        assertEquals("নোটস আপলোড করেছি", sentMessage)
    }

    @Test
    fun `chat room disables sending when user is blocked`() {
        val blockedThread = UserChatThread(
            id = "dm_u1_u2",
            participantIds = listOf("u1", "u2"),
            participantNames = mapOf("u1" to "Ayesha", "u2" to "Tanvir Hasan"),
            blockedBy = listOf("u1")
        )

        composeRule.setContent {
            HonoursSolutionTheme {
                UserChatRoomScreen(
                    currentUserId = "u1",
                    thread = blockedThread,
                    partnerProfile = null,
                    messages = emptyList(),
                    replyingTo = null,
                    pendingImageDataUri = null,
                    isProcessingImage = false,
                    isSending = false,
                    onBackClick = {},
                    onComposerTextChanged = {},
                    onAttachPhotoUri = {},
                    onClearPendingPhoto = {},
                    onSelectReply = {},
                    onSendMessage = {},
                    onMarkRead = {},
                    onDeleteMessageForAll = {},
                    onToggleBlock = {},
                    onReportUser = { _, _ -> }
                )
            }
        }

        composeRule.onNodeWithTag("chat_blocked_banner").assertIsDisplayed()
        composeRule.onNodeWithTag("chat_send_button").assertIsNotEnabled()
    }
}
