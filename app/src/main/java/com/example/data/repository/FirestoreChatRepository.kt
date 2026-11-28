package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.core.isValidCollegeImageInput
import com.example.data.model.ChatUserProfile
import com.example.data.model.UserChatMessage
import com.example.data.model.UserChatThread
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest

class FirestoreChatRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    companion object {
        fun buildDirectChatId(uidA: String, uidB: String): String {
            val cleanA = uidA.trim()
            val cleanB = uidB.trim()
            val sorted = listOf(cleanA, cleanB).sorted()
            val raw = "dm_${sorted[0]}_${sorted[1]}"
            val safePattern = Regex("^[a-zA-Z0-9_\\-]+$")
            if (raw.length <= 120 && safePattern.matches(raw)) {
                return raw
            }
            val digest = MessageDigest.getInstance("SHA-256")
                .digest("${sorted[0]}:${sorted[1]}".toByteArray(Charsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
            return "dm_$digest"
        }
    }

    fun currentUid(): String? = auth.currentUser?.uid

    private fun requireUid(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("চ্যাট করতে অনুগ্রহ করে লগইন করুন।")

    private fun safeDisplayName(profile: UserProfile?): String {
        val fromProfile = profile?.displayName?.trim().orEmpty()
        if (fromProfile.isNotEmpty()) return fromProfile.take(90)
        val fromAuth = auth.currentUser?.displayName?.trim().orEmpty()
        if (fromAuth.isNotEmpty()) return fromAuth.take(90)
        return "NU Student"
    }

    private fun safePhotoUrl(profile: UserProfile?): String {
        val candidate = profile?.photoUrl?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: auth.currentUser?.photoUrl?.toString()?.trim().orEmpty()
        return if (isValidCollegeImageInput(candidate)) candidate else ""
    }

    suspend fun syncMyChatProfile(profile: UserProfile?): Result<Unit> = runCatching {
        val uid = requireUid()
        val payload = hashMapOf<String, Any>(
            "userId" to uid,
            "displayName" to safeDisplayName(profile),
            "departmentName" to (profile?.selectedDepartmentName?.ifBlank { "অনার্স শিক্ষার্থী" } ?: "অনার্স শিক্ষার্থী").take(110),
            "yearNumber" to (profile?.selectedYear?.coerceIn(1L, 4L) ?: 1L),
            "photoUrl" to safePhotoUrl(profile),
            "isOnline" to true,
            "lastSeenAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        db.collection("chat_profiles").document(uid)
            .set(payload, SetOptions.merge())
            .await()
        Unit
    }.onFailure { if (it is CancellationException) throw it }

    suspend fun setOnlineStatus(isOnline: Boolean) {
        val uid = currentUid() ?: return
        try {
            db.collection("chat_profiles").document(uid)
                .update(
                    mapOf(
                        "isOnline" to isOnline,
                        "lastSeenAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Best-effort presence update; ignored if profile doc isn't created yet
        }
    }

    fun observeDirectoryUsers(): Flow<List<ChatUserProfile>> {
        val myUid = currentUid().orEmpty()
        return db.collection("chat_profiles")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    runCatching { doc.toObject(ChatUserProfile::class.java) }.getOrNull()
                }
                    .filter { it.userId.isNotBlank() && it.userId != myUid }
                    .sortedWith(
                        compareByDescending<ChatUserProfile> { it.isOnline }
                            .thenByDescending { it.lastSeenAt?.seconds ?: 0L }
                            .thenBy { it.displayName.lowercase() }
                    )
            }
    }

    fun observeMyChats(): Flow<List<UserChatThread>> {
        val myUid = currentUid().orEmpty()
        return db.collection("chats")
            .whereArrayContains("participantIds", myUid)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    runCatching { doc.toObject(UserChatThread::class.java) }.getOrNull()
                }.sortedByDescending { thread ->
                    thread.lastMessageAt?.seconds ?: thread.updatedAt?.seconds ?: thread.createdAt?.seconds ?: 0L
                }
            }
    }

    fun observeChatThread(chatId: String): Flow<UserChatThread?> {
        return db.collection("chats").document(chatId)
            .snapshots()
            .map { doc ->
                if (!doc.exists()) null
                else runCatching { doc.toObject(UserChatThread::class.java) }.getOrNull()
            }
    }

    fun observeMessages(chatId: String): Flow<List<UserChatMessage>> {
        return db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .limitToLast(200)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    runCatching { doc.toObject(UserChatMessage::class.java) }.getOrNull()
                }
            }
    }

    suspend fun openOrCreateDirectChat(
        partner: ChatUserProfile,
        myProfile: UserProfile?
    ): Result<String> = runCatching {
        val myUid = requireUid()
        require(partner.userId.isNotBlank() && partner.userId != myUid) {
            "সঠিক ব্যবহারকারী নির্বাচন করুন।"
        }
        val chatId = buildDirectChatId(myUid, partner.userId)
        val chatRef = db.collection("chats").document(chatId)
        val myName = safeDisplayName(myProfile)
        val myDept = (myProfile?.selectedDepartmentName?.ifBlank { "অনার্স শিক্ষার্থী" } ?: "অনার্স শিক্ষার্থী").take(110)
        val myPhoto = safePhotoUrl(myProfile)

        val existingSnap = chatRef.get().await()
        if (!existingSnap.exists()) {
            val participants = listOf(myUid, partner.userId).sorted()
            val threadData = hashMapOf<String, Any>(
                "participantIds" to participants,
                "participantNames" to mapOf(
                    myUid to myName,
                    partner.userId to partner.displayName.ifBlank { "NU Student" }.take(90)
                ),
                "participantDepts" to mapOf(
                    myUid to myDept,
                    partner.userId to partner.departmentName.take(110)
                ),
                "participantPhotos" to mapOf(
                    myUid to myPhoto,
                    partner.userId to partner.photoUrl.takeIf { isValidCollegeImageInput(it) }.orEmpty()
                ),
                "lastMessageText" to "",
                "lastMessageSenderId" to "",
                "unreadCounts" to mapOf(myUid to 0L, partner.userId to 0L),
                "typingUsers" to mapOf(myUid to 0L, partner.userId to 0L),
                "blockedBy" to emptyList<String>(),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            chatRef.set(threadData).await()
        } else {
            chatRef.update(
                mapOf(
                    "participantNames.$myUid" to myName,
                    "participantDepts.$myUid" to myDept,
                    "participantPhotos.$myUid" to myPhoto,
                    "participantNames.${partner.userId}" to partner.displayName.ifBlank { "NU Student" }.take(90),
                    "participantDepts.${partner.userId}" to partner.departmentName.take(110),
                    "participantPhotos.${partner.userId}" to partner.photoUrl.takeIf { isValidCollegeImageInput(it) }.orEmpty(),
                    "unreadCounts.$myUid" to 0L,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
        chatId
    }.onFailure { if (it is CancellationException) throw it }

    suspend fun sendMessage(
        chatId: String,
        partnerUid: String,
        senderName: String,
        text: String,
        imageUrl: String = "",
        replyTo: UserChatMessage? = null
    ): Result<Unit> = runCatching {
        val myUid = requireUid()
        val cleanText = text.trim().take(3900)
        val cleanImage = imageUrl.trim()
        require(cleanText.isNotEmpty() || cleanImage.isNotEmpty()) {
            "বার্তা অথবা ছবি যোগ করুন।"
        }
        require(isValidCollegeImageInput(cleanImage)) {
            "ছবির ফরম্যাট সঠিক নয়।"
        }

        val chatRef = db.collection("chats").document(chatId)
        val msgRef = chatRef.collection("messages").document()
        val preview = when {
            cleanText.isNotEmpty() -> cleanText.take(140)
            else -> "📷 ছবি"
        }

        val replyPreview = replyTo?.let { target ->
            when {
                target.deletedForAll -> "মুছে ফেলা বার্তা"
                target.text.isNotBlank() -> target.text.take(140)
                target.imageUrl.isNotBlank() -> "📷 ছবি"
                else -> ""
            }
        }.orEmpty()

        val messagePayload = hashMapOf<String, Any>(
            "senderId" to myUid,
            "senderName" to senderName.ifBlank { "NU Student" }.take(90),
            "text" to cleanText,
            "imageUrl" to cleanImage,
            "replyToMessageId" to (replyTo?.id.orEmpty().take(120)),
            "replyToSenderName" to (replyTo?.senderName.orEmpty().take(90)),
            "replyToPreview" to replyPreview,
            "readBy" to listOf(myUid),
            "deletedForAll" to false,
            "createdAt" to FieldValue.serverTimestamp()
        )

        val batch = db.batch()
        batch.set(msgRef, messagePayload)
        batch.update(
            chatRef,
            mapOf(
                "lastMessageText" to preview,
                "lastMessageSenderId" to myUid,
                "lastMessageAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
                "unreadCounts.$myUid" to 0L,
                "unreadCounts.$partnerUid" to FieldValue.increment(1L),
                "typingUsers.$myUid" to 0L
            )
        )
        batch.commit().await()
        Unit
    }.onFailure { if (it is CancellationException) throw it }

    suspend fun markMessagesAsRead(
        chatId: String,
        messages: List<UserChatMessage>,
        currentThreadUnread: Long
    ) {
        val myUid = currentUid() ?: return
        val unreadPartnerMsgs = messages.filter { msg ->
            msg.id.isNotBlank() &&
                msg.senderId != myUid &&
                myUid !in msg.readBy
        }
        if (unreadPartnerMsgs.isEmpty() && currentThreadUnread <= 0L) return

        try {
            val chatRef = db.collection("chats").document(chatId)
            val batch = db.batch()
            if (currentThreadUnread > 0L) {
                batch.update(
                    chatRef,
                    mapOf(
                        "unreadCounts.$myUid" to 0L,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            unreadPartnerMsgs.takeLast(50).forEach { msg ->
                val updatedReadBy = (msg.readBy + myUid).distinct().take(2)
                batch.update(chatRef.collection("messages").document(msg.id), "readBy", updatedReadBy)
            }
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Non-critical read-receipt update
        }
    }

    suspend fun setTypingState(chatId: String, isTyping: Boolean) {
        val myUid = currentUid() ?: return
        try {
            val ts = if (isTyping) System.currentTimeMillis() else 0L
            db.collection("chats").document(chatId)
                .update(
                    mapOf(
                        "typingUsers.$myUid" to ts,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Best-effort typing indicator
        }
    }

    suspend fun deleteMessageForEveryone(chatId: String, messageId: String): Result<Unit> = runCatching {
        requireUid()
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .document(messageId)
            .update(
                mapOf(
                    "deletedForAll" to true,
                    "text" to "",
                    "imageUrl" to ""
                )
            )
            .await()
        Unit
    }.onFailure { if (it is CancellationException) throw it }

    suspend fun toggleBlockUser(chatId: String, currentlyBlockedByMe: Boolean): Result<Unit> = runCatching {
        val myUid = requireUid()
        val updateOp = if (currentlyBlockedByMe) {
            FieldValue.arrayRemove(myUid)
        } else {
            FieldValue.arrayUnion(myUid)
        }
        db.collection("chats").document(chatId)
            .update(
                mapOf(
                    "blockedBy" to updateOp,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            .await()
        Unit
    }.onFailure { if (it is CancellationException) throw it }

    suspend fun reportChatUser(
        partnerUid: String,
        partnerName: String,
        reason: String,
        details: String
    ): Result<Unit> = runCatching {
        val myUid = requireUid()
        db.collection("user_reports").document().set(
            mapOf(
                "userId" to myUid,
                "reporterName" to (auth.currentUser?.displayName ?: "Student"),
                "targetType" to "chat_user",
                "targetId" to partnerUid.take(128),
                "reason" to "$reason ($partnerName)".take(180),
                "details" to details.trim().take(1800),
                "status" to "open",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
        Unit
    }.onFailure { if (it is CancellationException) throw it }
}
