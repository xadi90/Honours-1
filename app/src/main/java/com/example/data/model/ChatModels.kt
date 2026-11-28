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
    val isGroup: Boolean = false,
    val groupName: String = "",
    val groupPhoto: String = "",
    val createdBy: String = "",
    val lastMessageText: String = "",
    val lastMessageSenderId: String = "",
    val lastMessageSenderName: String = "",
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
        if (isGroup) return groupName.ifBlank { "শিক্ষার্থী গ্রুপ" }
        val partner = partnerUid(myUid)
        return participantNames[partner]?.takeIf { it.isNotBlank() } ?: "NU Student"
    }

    fun partnerDept(myUid: String): String {
        if (isGroup) return "${participantIds.size} জন সদস্য"
        val partner = partnerUid(myUid)
        return participantDepts[partner].orEmpty()
    }

    fun partnerPhoto(myUid: String): String {
        if (isGroup) return groupPhoto
        val partner = partnerUid(myUid)
        return participantPhotos[partner].orEmpty()
    }

    fun displayTitle(myUid: String): String =
        if (isGroup) groupName.ifBlank { "শিক্ষার্থী গ্রুপ" } else partnerName(myUid)

    fun myUnreadCount(myUid: String): Long =
        (unreadCounts[myUid] ?: 0L).coerceAtLeast(0L)

    fun isPartnerTyping(myUid: String, nowMs: Long = System.currentTimeMillis()): Boolean {
        return typingDisplayText(myUid, nowMs) != null
    }

    fun typingDisplayText(myUid: String, nowMs: Long = System.currentTimeMillis()): String? {
        val activeTypers = typingUsers.entries
            .filter { (uid, ts) -> uid != myUid && uid in participantIds && (nowMs - ts) in 0L..7000L }
            .map { it.key }
        if (activeTypers.isEmpty()) return null
        return if (!isGroup) {
            "টাইপ করছে..."
        } else if (activeTypers.size == 1) {
            val name = participantNames[activeTypers.first()]?.substringBefore(" ")?.takeIf { it.isNotBlank() } ?: "কেউ"
            "$name টাইপ করছে..."
        } else {
            "${activeTypers.size} জন টাইপ করছে..."
        }
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
    val audioUrl: String = "",
    val audioDurationSec: Long = 0L,
    val replyToMessageId: String = "",
    val replyToSenderName: String = "",
    val replyToPreview: String = "",
    val readBy: List<String> = emptyList(),
    val deletedForAll: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ChatBlockStatus(
    @DocumentId val id: String = "",
    val userId: String = "",
    val blockedUntil: Timestamp? = null,
    val durationMinutes: Long = 0L,
    val reason: String = "",
    val blockedByUid: String = "",
    val reportId: String = "",
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun isCurrentlyBlocked(nowMs: Long = System.currentTimeMillis()): Boolean {
        val untilMs = blockedUntil?.toDate()?.time ?: return false
        return untilMs > nowMs
    }

    fun remainingMillis(nowMs: Long = System.currentTimeMillis()): Long {
        val untilMs = blockedUntil?.toDate()?.time ?: return 0L
        return (untilMs - nowMs).coerceAtLeast(0L)
    }
}
