package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.core.OperationType
import com.example.core.handleFirestoreError
import com.example.data.catalog.AcademicSeedCatalog
import com.example.data.model.AdminLog
import com.example.data.model.AiConversation
import com.example.data.model.AiMessage
import com.example.data.model.AiRemoteConfig
import com.example.data.model.Announcement
import com.example.data.model.BookmarkItem
import com.example.data.model.Chapter
import com.example.data.model.Department
import com.example.data.model.DeveloperProfileConfig
import com.example.data.model.ModelTest
import com.example.data.model.ModelTestResult
import com.example.data.model.PdfResource
import com.example.data.model.StructuredAiAnswer
import com.example.data.model.StudyQuestion
import com.example.data.model.Subject
import com.example.data.model.UserProfile
import com.example.data.model.UserReport
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
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

class FirestoreRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    companion object {
        private val BOOTSTRAPPED_ADMIN_EMAILS = setOf(
            "xadiahmed7@gmail.com",
            "xadiahmed23@gmail.com"
        )
    }

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Please sign in with Google to access this feature.")
    }

    fun isBootstrappedAdminUser(): Boolean {
        val user = auth.currentUser ?: return false
        val email = user.email?.lowercase().orEmpty()
        return user.isEmailVerified && email in BOOTSTRAPPED_ADMIN_EMAILS
    }

    suspend fun checkIsAdmin(): Boolean {
        if (isBootstrappedAdminUser()) return true
        val uid = auth.currentUser?.uid ?: return false
        return try {
            val snap = db.collection("admins").document(uid).get().await()
            snap.exists()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, "admins/$uid")
            false
        }
    }

    fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    suspend fun hasAdminPinConfigured(): Boolean {
        return try {
            val snap = db.collection("app_settings").document("admin_security").get().await()
            snap.exists() && !snap.getString("pinHash").isNullOrBlank()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, "app_settings/admin_security")
            false
        }
    }

    suspend fun initializeAdminPin(pin: String): Result<Unit> {
        val path = "app_settings/admin_security"
        return try {
            require(pin.length >= 4) { "PIN must be at least 4 characters." }
            val uid = requireUserId()
            db.collection("app_settings").document("admin_security").set(
                mapOf(
                    "pinHash" to sha256(pin),
                    "updatedBy" to uid,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            logAdminAction("ADMIN_PIN_SET", "Initialized backend admin PIN hash")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun verifyAdminPin(enteredPin: String): Result<Boolean> {
        val path = "app_settings/admin_security"
        return try {
            val snap = db.collection("app_settings").document("admin_security").get().await()
            val storedHash = snap.getString("pinHash")
            if (storedHash.isNullOrBlank()) {
                Result.failure(IllegalStateException("Admin PIN not initialized on backend yet."))
            } else {
                Result.success(sha256(enteredPin.trim()) == storedHash)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            Result.failure(e)
        }
    }

    // --- User Profile ---
    suspend fun ensureUserProfileExists(): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(IllegalStateException("Not signed in"))
        val path = "users/${user.uid}"
        return try {
            val docRef = db.collection("users").document(user.uid)
            val existing = docRef.get().await()
            if (!existing.exists()) {
                val payload = mapOf(
                    "userId" to user.uid,
                    "displayName" to (user.displayName?.takeIf { it.isNotBlank() } ?: "NU Student"),
                    "email" to (user.email ?: ""),
                    "photoUrl" to (user.photoUrl?.toString() ?: ""),
                    "selectedDepartmentId" to "political_science",
                    "selectedDepartmentName" to "রাষ্ট্রবিজ্ঞান (Political Science)",
                    "selectedYear" to 1L,
                    "languagePreference" to "bn_en",
                    "savedAnswersCount" to 0L,
                    "bookmarksCount" to 0L,
                    "testsCompletedCount" to 0L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> =
        db.collection("users").document(userId)
            .snapshots()
            .map { snap ->
                snap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }

    suspend fun updateUserPreferences(
        userId: String,
        displayName: String,
        departmentId: String,
        departmentName: String,
        yearNumber: Long,
        languagePreference: String
    ): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "User mismatch" }
            db.collection("users").document(userId).update(
                mapOf(
                    "displayName" to displayName.trim().ifBlank { "NU Student" }.take(100),
                    "selectedDepartmentId" to departmentId,
                    "selectedDepartmentName" to departmentName,
                    "selectedYear" to yearNumber.coerceIn(1L, 4L),
                    "languagePreference" to languagePreference,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- Saved AI Answers ---
    fun observeSavedAiAnswers(userId: String): Flow<List<StructuredAiAnswer>> =
        db.collection("users").document(userId).collection("ai_answers")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { it.toObjects(StructuredAiAnswer::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }

    suspend fun saveAiAnswer(answer: StructuredAiAnswer): Result<String> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Sign in required"))
        val docRef = if (answer.id.isNotBlank()) {
            db.collection("users").document(uid).collection("ai_answers").document(answer.id)
        } else {
            db.collection("users").document(uid).collection("ai_answers").document()
        }
        return try {
            val payload = mapOf(
                "userId" to uid,
                "question" to answer.question.take(2000),
                "answerLength" to answer.answerLength.take(80),
                "yearLabel" to answer.yearLabel.take(80),
                "subjectName" to answer.subjectName.take(150),
                "language" to answer.language.take(40),
                "prashno" to answer.prashno.ifBlank { answer.question }.take(2000),
                "bhumika" to answer.bhumika.ifBlank { "—" }.take(4000),
                "mulAlochona" to answer.mulAlochona.ifBlank { "—" }.take(20000),
                "importantPoints" to answer.importantPoints.take(25),
                "examples" to answer.examples.take(6000),
                "uposhonghar" to answer.uposhonghar.ifBlank { "—" }.take(4000),
                "sourceNote" to answer.sourceNote.take(1000),
                "hasImageInput" to answer.hasImageInput,
                "bookmarked" to answer.bookmarked,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun toggleAnswerBookmark(userId: String, answerId: String, bookmarked: Boolean): Result<Unit> {
        val path = "users/$userId/ai_answers/$answerId"
        return try {
            requireUserId()
            db.document(path).update(
                mapOf(
                    "bookmarked" to bookmarked,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteSavedAiAnswer(userId: String, answerId: String): Result<Unit> {
        val path = "users/$userId/ai_answers/$answerId"
        return try {
            requireUserId()
            db.document(path).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    // --- AI Conversations & Messages ---
    fun observeConversations(userId: String): Flow<List<AiConversation>> =
        db.collection("users").document(userId).collection("ai_conversations")
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { it.toObjects(AiConversation::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }

    fun observeConversationMessages(userId: String, conversationId: String): Flow<List<AiMessage>> =
        db.collection("users").document(userId)
            .collection("ai_conversations").document(conversationId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .snapshots()
            .map { it.toObjects(AiMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }

    suspend fun createConversation(
        title: String,
        mode: String,
        subjectName: String,
        yearNumber: Long
    ): Result<String> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Sign in required"))
        val docRef = db.collection("users").document(uid).collection("ai_conversations").document()
        return try {
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "title" to title.take(200).ifBlank { "AI Study Chat" },
                    "mode" to mode.take(60),
                    "subjectName" to subjectName.take(150),
                    "yearNumber" to yearNumber.coerceIn(0L, 4L),
                    "lastMessage" to "",
                    "messageCount" to 0L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun appendConversationMessage(
        conversationId: String,
        role: String,
        text: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Sign in required"))
        val convRef = db.collection("users").document(uid).collection("ai_conversations").document(conversationId)
        val msgRef = convRef.collection("messages").document()
        return try {
            msgRef.set(
                mapOf(
                    "userId" to uid,
                    "role" to role,
                    "text" to text.take(25000),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            convRef.update(
                mapOf(
                    "lastMessage" to text.take(450),
                    "messageCount" to FieldValue.increment(1),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, msgRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteConversation(userId: String, conversationId: String): Result<Unit> {
        val convRef = db.collection("users").document(userId).collection("ai_conversations").document(conversationId)
        return try {
            requireUserId()
            val msgs = convRef.collection("messages").get().await()
            for (doc in msgs.documents) {
                doc.reference.delete().await()
            }
            convRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, convRef.path)
            Result.failure(e)
        }
    }

    // --- Bookmarks & Model Test Results ---
    fun observeBookmarks(userId: String): Flow<List<BookmarkItem>> =
        db.collection("users").document(userId).collection("bookmarks")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { it.toObjects(BookmarkItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }

    suspend fun addBookmark(item: BookmarkItem): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Sign in required"))
        val safeDocId = "${item.itemType}_${item.targetId}_${item.pageNumber}"
            .replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
            .take(120)
        val docRef = db.collection("users").document(uid).collection("bookmarks").document(safeDocId)
        return try {
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "itemType" to item.itemType,
                    "targetId" to item.targetId.take(150),
                    "title" to item.title.take(250),
                    "subtitle" to item.subtitle.take(300),
                    "pageNumber" to item.pageNumber,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun removeBookmark(userId: String, bookmarkId: String): Result<Unit> {
        val path = "users/$userId/bookmarks/$bookmarkId"
        return try {
            requireUserId()
            db.document(path).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    fun observeModelTestResults(userId: String): Flow<List<ModelTestResult>> =
        db.collection("users").document(userId).collection("model_test_results")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { it.toObjects(ModelTestResult::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }

    suspend fun saveModelTestResult(result: ModelTestResult): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Sign in required"))
        val docRef = db.collection("users").document(uid).collection("model_test_results").document()
        return try {
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "testId" to result.testId.take(128),
                    "testTitle" to result.testTitle.take(200),
                    "yearNumber" to result.yearNumber.coerceIn(1L, 4L),
                    "subjectName" to result.subjectName.take(150),
                    "totalQuestions" to result.totalQuestions,
                    "correctCount" to result.correctCount,
                    "scorePercent" to result.scorePercent.coerceIn(0L, 100L),
                    "durationSeconds" to result.durationSeconds,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Published Shared Academic Streams (Aligned 1:1 with status == "published" rule) ---
    fun observePublishedDepartments(): Flow<List<Department>> =
        db.collection("departments")
            .whereEqualTo("status", "published")
            .snapshots()
            .map { snap ->
                snap.toObjects(Department::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedBy { it.sortOrder }
            }

    fun observePublishedSubjects(): Flow<List<Subject>> =
        db.collection("subjects")
            .whereEqualTo("status", "published")
            .snapshots()
            .map { snap ->
                snap.toObjects(Subject::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedWith(compareBy({ it.yearNumber }, { it.courseCode }))
            }

    fun observePublishedChapters(): Flow<List<Chapter>> =
        db.collection("chapters")
            .whereEqualTo("status", "published")
            .snapshots()
            .map { snap ->
                snap.toObjects(Chapter::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedBy { it.chapterNumber }
            }

    fun observePublishedPdfs(): Flow<List<PdfResource>> =
        db.collection("pdf_library")
            .whereEqualTo("status", "published")
            .snapshots()
            .map { snap ->
                snap.toObjects(PdfResource::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }

    fun observePublishedQuestions(): Flow<List<StudyQuestion>> =
        db.collection("questions")
            .whereEqualTo("status", "published")
            .snapshots()
            .map { snap ->
                snap.toObjects(StudyQuestion::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }

    fun observePublishedModelTests(): Flow<List<ModelTest>> =
        db.collection("model_tests")
            .whereEqualTo("status", "published")
            .snapshots()
            .map { snap ->
                snap.toObjects(ModelTest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }

    fun observePublishedAnnouncements(): Flow<List<Announcement>> =
        db.collection("announcements")
            .whereEqualTo("status", "published")
            .snapshots()
            .map { snap ->
                snap.toObjects(Announcement::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedWith(compareByDescending<Announcement> { it.pinned }.thenByDescending { it.createdAt?.seconds ?: 0L })
            }

    // --- Remote Config / App Settings ---
    fun observeAiRemoteConfig(): Flow<AiRemoteConfig> =
        db.collection("app_settings").document("ai_config")
            .snapshots()
            .map { snap ->
                if (!snap.exists()) AiRemoteConfig()
                else AiRemoteConfig(
                    modelName = snap.getString("modelName")?.takeIf { it.isNotBlank() } ?: "gemini-3.8-flash",
                    systemInstructions = snap.getString("systemInstructions").orEmpty(),
                    maintenanceMode = snap.getBoolean("maintenanceMode") ?: false,
                    maintenanceMessage = snap.getString("maintenanceMessage").orEmpty()
                )
            }

    fun observeDeveloperProfile(): Flow<DeveloperProfileConfig> =
        db.collection("app_settings").document("developer_profile")
            .snapshots()
            .map { snap ->
                val defaults = DeveloperProfileConfig()
                if (!snap.exists()) defaults
                else DeveloperProfileConfig(
                    name = snap.getString("name")?.takeIf { it.isNotBlank() } ?: defaults.name,
                    tagline = snap.getString("tagline")?.takeIf { it.isNotBlank() } ?: defaults.tagline,
                    departmentFocus = snap.getString("departmentFocus")?.takeIf { it.isNotBlank() } ?: defaults.departmentFocus,
                    bio = snap.getString("bio")?.takeIf { it.isNotBlank() } ?: defaults.bio,
                    phone = snap.getString("phone")?.takeIf { it.isNotBlank() } ?: defaults.phone,
                    email = snap.getString("email")?.takeIf { it.isNotBlank() } ?: defaults.email,
                    websiteUrl = snap.getString("websiteUrl")?.takeIf { it.isNotBlank() } ?: defaults.websiteUrl,
                    websiteLabel = snap.getString("websiteLabel")?.takeIf { it.isNotBlank() } ?: defaults.websiteLabel
                )
            }

    suspend fun getVersionInfoMap(): Map<String, Any?> {
        return try {
            val snap = db.collection("app_settings").document("version_info").get().await()
            snap.data ?: emptyMap()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, "app_settings/version_info")
            emptyMap()
        }
    }

    // --- User Reports ---
    suspend fun submitUserReport(
        targetType: String,
        targetId: String,
        reason: String,
        details: String
    ): Result<Unit> {
        val user = auth.currentUser
            ?: return Result.failure(IllegalStateException("Sign in required"))
        val docRef = db.collection("user_reports").document()
        return try {
            docRef.set(
                mapOf(
                    "userId" to user.uid,
                    "reporterName" to (user.displayName ?: "Student").take(120),
                    "targetType" to targetType.take(60),
                    "targetId" to targetId.take(150),
                    "reason" to reason.take(200),
                    "details" to details.take(2000),
                    "status" to "open",
                    "adminNote" to "",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Admin Streams & Mutations ---
    fun observeAllReportsForAdmin(): Flow<List<UserReport>> =
        db.collection("user_reports")
            .snapshots()
            .map { snap ->
                snap.toObjects(UserReport::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }

    fun observeAdminLogs(): Flow<List<AdminLog>> =
        db.collection("admin_logs")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(40)
            .snapshots()
            .map { it.toObjects(AdminLog::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }

    suspend fun logAdminAction(action: String, summary: String) {
        val user = auth.currentUser ?: return
        val docRef = db.collection("admin_logs").document()
        try {
            docRef.set(
                mapOf(
                    "adminUid" to user.uid,
                    "adminEmail" to (user.email ?: ""),
                    "action" to action.take(100),
                    "summary" to summary.take(500),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
        }
    }

    suspend fun upsertDepartment(dept: Department): Result<Unit> {
        val docId = dept.id.ifBlank { db.collection("departments").document().id }
        val docRef = db.collection("departments").document(docId)
        return try {
            requireUserId()
            val existing = docRef.get().await()
            val payload = mapOf(
                "nameBn" to dept.nameBn.take(120),
                "nameEn" to dept.nameEn.take(120),
                "code" to dept.code.take(30),
                "description" to dept.description.take(500),
                "hasStructuredSolution" to dept.hasStructuredSolution,
                "sortOrder" to dept.sortOrder,
                "status" to dept.status,
                "createdAt" to (existing.getTimestamp("createdAt") ?: FieldValue.serverTimestamp()),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            logAdminAction("UPSERT_DEPARTMENT", "${dept.nameBn} (${dept.nameEn})")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteDepartment(departmentId: String): Result<Unit> {
        val path = "departments/$departmentId"
        return try {
            requireUserId()
            db.document(path).delete().await()
            logAdminAction("DELETE_DEPARTMENT", "Deleted department $departmentId")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun upsertSubject(subject: Subject): Result<Unit> {
        val docId = subject.id.ifBlank { db.collection("subjects").document().id }
        val docRef = db.collection("subjects").document(docId)
        return try {
            requireUserId()
            val existing = docRef.get().await()
            docRef.set(
                mapOf(
                    "departmentId" to subject.departmentId.take(128),
                    "yearNumber" to subject.yearNumber.coerceIn(1L, 4L),
                    "courseCode" to subject.courseCode.take(30),
                    "titleBn" to subject.titleBn.take(200),
                    "titleEn" to subject.titleEn.take(200),
                    "credits" to subject.credits.coerceIn(1L, 10L),
                    "syllabusNote" to subject.syllabusNote.take(2000),
                    "status" to subject.status,
                    "createdAt" to (existing.getTimestamp("createdAt") ?: FieldValue.serverTimestamp()),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            logAdminAction("UPSERT_SUBJECT", "${subject.courseCode}: ${subject.titleBn}")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteSubject(subjectId: String): Result<Unit> {
        val path = "subjects/$subjectId"
        return try {
            requireUserId()
            db.document(path).delete().await()
            logAdminAction("DELETE_SUBJECT", "Deleted subject $subjectId")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun upsertChapter(chapter: Chapter): Result<Unit> {
        val docId = chapter.id.ifBlank { db.collection("chapters").document().id }
        val docRef = db.collection("chapters").document(docId)
        return try {
            requireUserId()
            val existing = docRef.get().await()
            docRef.set(
                mapOf(
                    "subjectId" to chapter.subjectId.take(128),
                    "yearNumber" to chapter.yearNumber.coerceIn(1L, 4L),
                    "chapterNumber" to chapter.chapterNumber.coerceIn(1L, 100L),
                    "titleBn" to chapter.titleBn.take(250),
                    "titleEn" to chapter.titleEn.take(250),
                    "topics" to chapter.topics.take(40),
                    "summary" to chapter.summary.take(10000),
                    "definitions" to chapter.definitions.take(10000),
                    "shortQuestions" to chapter.shortQuestions.take(15000),
                    "broadQuestions" to chapter.broadQuestions.take(25000),
                    "creativeQuestions" to chapter.creativeQuestions.take(15000),
                    "status" to chapter.status,
                    "createdAt" to (existing.getTimestamp("createdAt") ?: FieldValue.serverTimestamp()),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            logAdminAction("UPSERT_CHAPTER", "Chapter ${chapter.chapterNumber}: ${chapter.titleBn}")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun upsertPdfResource(pdf: PdfResource): Result<String> {
        val docId = pdf.id.ifBlank { db.collection("pdf_library").document().id }
        val docRef = db.collection("pdf_library").document(docId)
        return try {
            val uid = requireUserId()
            val existing = docRef.get().await()
            docRef.set(
                mapOf(
                    "departmentId" to pdf.departmentId.take(128),
                    "departmentName" to pdf.departmentName.take(150),
                    "yearNumber" to pdf.yearNumber.coerceIn(1L, 4L),
                    "subjectName" to pdf.subjectName.take(200),
                    "courseCode" to pdf.courseCode.take(30),
                    "title" to pdf.title.take(250),
                    "category" to pdf.category.take(80),
                    "description" to pdf.description.take(2000),
                    "pdfUrl" to pdf.pdfUrl.take(1000),
                    "localFileName" to pdf.localFileName.take(200),
                    "coverImageUrl" to pdf.coverImageUrl.take(1000),
                    "pageCount" to pdf.pageCount.coerceAtLeast(0L),
                    "fileSizeBytes" to pdf.fileSizeBytes.coerceAtLeast(0L),
                    "uploadedBy" to uid,
                    "status" to pdf.status,
                    "createdAt" to (existing.getTimestamp("createdAt") ?: FieldValue.serverTimestamp()),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            logAdminAction("UPLOAD_PDF", "${pdf.title} (${pdf.status})")
            Result.success(docId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deletePdfResource(pdfId: String): Result<Unit> {
        val path = "pdf_library/$pdfId"
        return try {
            requireUserId()
            db.document(path).delete().await()
            logAdminAction("DELETE_PDF", "Deleted PDF $pdfId")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun upsertQuestion(question: StudyQuestion): Result<Unit> {
        val docId = question.id.ifBlank { db.collection("questions").document().id }
        val docRef = db.collection("questions").document(docId)
        return try {
            requireUserId()
            val existing = docRef.get().await()
            docRef.set(
                mapOf(
                    "departmentId" to question.departmentId.take(128),
                    "yearNumber" to question.yearNumber.coerceIn(1L, 4L),
                    "subjectId" to question.subjectId.take(128),
                    "subjectName" to question.subjectName.take(200),
                    "courseCode" to question.courseCode.take(30),
                    "questionType" to question.questionType,
                    "examYear" to question.examYear.take(20),
                    "partLabel" to question.partLabel.take(60),
                    "prompt" to question.prompt.take(4000),
                    "answerText" to question.answerText.take(20000),
                    "options" to question.options.take(6),
                    "correctOptionIndex" to question.correctOptionIndex.coerceIn(0L, 5L),
                    "explanation" to question.explanation.take(4000),
                    "priority" to question.priority.take(40),
                    "status" to question.status,
                    "createdAt" to (existing.getTimestamp("createdAt") ?: FieldValue.serverTimestamp()),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            logAdminAction("UPSERT_QUESTION", "${question.questionType}: ${question.prompt.take(60)}")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun upsertAnnouncement(announcement: Announcement): Result<Unit> {
        val docId = announcement.id.ifBlank { db.collection("announcements").document().id }
        val docRef = db.collection("announcements").document(docId)
        return try {
            requireUserId()
            val existing = docRef.get().await()
            docRef.set(
                mapOf(
                    "title" to announcement.title.take(200),
                    "body" to announcement.body.take(4000),
                    "tag" to announcement.tag.take(50),
                    "pinned" to announcement.pinned,
                    "status" to announcement.status,
                    "createdAt" to (existing.getTimestamp("createdAt") ?: FieldValue.serverTimestamp()),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            logAdminAction("PUBLISH_ANNOUNCEMENT", announcement.title)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteAnnouncement(id: String): Result<Unit> {
        val path = "announcements/$id"
        return try {
            requireUserId()
            db.document(path).delete().await()
            logAdminAction("DELETE_ANNOUNCEMENT", "Deleted announcement $id")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun updateReportStatus(reportId: String, status: String, adminNote: String): Result<Unit> {
        val path = "user_reports/$reportId"
        return try {
            requireUserId()
            db.document(path).update(
                mapOf(
                    "status" to status,
                    "adminNote" to adminNote.take(1000),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            logAdminAction("REVIEW_REPORT", "Report $reportId -> $status")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun saveAiRemoteConfig(config: AiRemoteConfig): Result<Unit> {
        val path = "app_settings/ai_config"
        return try {
            requireUserId()
            db.document(path).set(
                mapOf(
                    "modelName" to config.modelName.ifBlank { "gemini-3.8-flash" },
                    "systemInstructions" to config.systemInstructions.take(4000),
                    "maintenanceMode" to config.maintenanceMode,
                    "maintenanceMessage" to config.maintenanceMessage.take(500),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            ).await()
            logAdminAction("UPDATE_AI_CONFIG", "Model=${config.modelName}, maintenance=${config.maintenanceMode}")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun saveDeveloperProfile(config: DeveloperProfileConfig): Result<Unit> {
        val path = "app_settings/developer_profile"
        val defaults = DeveloperProfileConfig()
        return try {
            requireUserId()
            val cleanName = config.name.trim().ifBlank { defaults.name }.take(120)
            db.document(path).set(
                mapOf(
                    "name" to cleanName,
                    "tagline" to config.tagline.trim().ifBlank { defaults.tagline }.take(160),
                    "departmentFocus" to config.departmentFocus.trim().ifBlank { defaults.departmentFocus }.take(200),
                    "bio" to config.bio.trim().ifBlank { defaults.bio }.take(1000),
                    "phone" to config.phone.trim().ifBlank { defaults.phone }.take(40),
                    "email" to config.email.trim().ifBlank { defaults.email }.take(160),
                    "websiteUrl" to config.websiteUrl.trim().ifBlank { defaults.websiteUrl }.take(300),
                    "websiteLabel" to config.websiteLabel.trim().ifBlank { defaults.websiteLabel }.take(120),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            ).await()
            logAdminAction("UPDATE_DEVELOPER_PROFILE", "Updated developer info ($cleanName)")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun seedDefaultCatalogToFirestore(): Result<Int> {
        return try {
            requireUserId()
            var count = 0
            for (dept in AcademicSeedCatalog.defaultDepartments) {
                upsertDepartment(dept).getOrThrow()
                count++
            }
            for (subj in AcademicSeedCatalog.starterSubjects) {
                upsertSubject(subj).getOrThrow()
                count++
            }
            for (chap in AcademicSeedCatalog.starterChapters) {
                upsertChapter(chap).getOrThrow()
                count++
            }
            for (q in AcademicSeedCatalog.starterQuestions) {
                upsertQuestion(q).getOrThrow()
                count++
            }
            for (ann in AcademicSeedCatalog.starterAnnouncements) {
                upsertAnnouncement(ann).getOrThrow()
                count++
            }
            logAdminAction("SEED_CATALOG", "Published $count starter academic items to Firestore")
            Result.success(count)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "seed_catalog")
            Result.failure(e)
        }
    }
}
