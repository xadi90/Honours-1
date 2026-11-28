package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class ChatUserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val departmentName: String = "",
    val yearNumber: Long = 1L,
    val photoUrl: String = "",
    val isOnline: Boolean = false,
    val lastSeenAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class UserChatThread(
    @DocumentId val id: String = "",
    val participantIds: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val participantDepts: Map<String, String> = emptyMap(),
    val participantPhotos: Map<String, String> = emptyMap(),
    val lastMessageText: String = "",
    val lastMessageSenderId: String = "",
    val lastMessageAt: Timestamp? = null,
    val unreadCounts: Map<String, Long> = emptyMap(),
    val typingUsers: Map<String, Long> = emptyMap(),
    val blockedBy: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun partnerUid(myUid: String): String =
        participantIds.firstOrNull { it != myUid } ?: myUid

    fun partnerName(myUid: String): String {
        val partner = partnerUid(myUid)
        return participantNames[partner]?.takeIf { it.isNotBlank() } ?: "NU Student"
    }

    fun partnerDept(myUid: String): String {
        val partner = partnerUid(myUid)
        return participantDepts[partner].orEmpty()
    }

    fun partnerPhoto(myUid: String): String {
        val partner = partnerUid(myUid)
        return participantPhotos[partner].orEmpty()
    }

    fun myUnreadCount(myUid: String): Long =
        (unreadCounts[myUid] ?: 0L).coerceAtLeast(0L)

    fun isPartnerTyping(myUid: String, nowMs: Long = System.currentTimeMillis()): Boolean {
        val partner = partnerUid(myUid)
        val ts = typingUsers[partner] ?: return false
        return (nowMs - ts) in 0L..7000L
    }
}

@Keep
@IgnoreExtraProperties
data class UserChatMessage(
    @DocumentId val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val imageUrl: String = "",
    val replyToMessageId: String = "",
    val replyToSenderName: String = "",
    val replyToPreview: String = "",
    val readBy: List<String> = emptyList(),
    val deletedForAll: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)
