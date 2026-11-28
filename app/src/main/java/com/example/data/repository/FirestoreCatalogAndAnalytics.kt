package com.example.data.repository

import com.example.core.OperationType
import com.example.core.handleFirestoreError
import com.example.data.catalog.AcademicSeedCatalog
import com.example.data.model.AdminUserDirectoryEntry
import com.example.data.model.AdminUserUsageSummary
import com.example.data.model.Announcement
import com.example.data.model.AuthPolicyConfig
import com.example.data.model.CatalogMetaConfig
import com.example.data.model.Chapter
import com.example.data.model.CollegeModerator
import com.example.data.model.Department
import com.example.data.model.PdfResource
import com.example.data.model.StudyQuestion
import com.example.data.model.Subject
import com.example.data.model.UserProfile
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

fun FirestoreRepository.observeCatalogMeta(): Flow<CatalogMetaConfig> =
    db.collection("app_settings").document("catalog_meta")
        .snapshots()
        .map { snap ->
            if (!snap.exists()) CatalogMetaConfig()
            else snap.toObject(CatalogMetaConfig::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                ?: CatalogMetaConfig()
        }

internal fun <T> mergeCatalogItems(
    live: List<T>,
    starter: List<T>,
    meta: CatalogMetaConfig,
    includeDrafts: Boolean,
    idSelector: (T) -> String
): List<T> {
    val hiddenIds = buildSet {
        addAll(meta.deletedIds)
        addAll(meta.suppressedIds)
        if (!includeDrafts) {
            addAll(meta.draftIds)
        }
    }
    val liveById = live.associateBy(idSelector)
    return buildList {
        addAll(live)
        for (item in starter) {
            val id = idSelector(item)
            if (id !in liveById && id !in hiddenIds) {
                add(item)
            }
        }
    }
}

internal suspend fun FirestoreRepository.syncCatalogMetaEntry(
    itemId: String,
    isDraft: Boolean = false,
    isDeleted: Boolean = false
) {
    if (itemId.isBlank()) return
    val ref = db.collection("app_settings").document("catalog_meta")
    try {
        val updates = mutableMapOf<String, Any>(
            "updatedAt" to FieldValue.serverTimestamp()
        )
        when {
            isDeleted -> {
                updates["deletedIds"] = FieldValue.arrayUnion(itemId)
                updates["draftIds"] = FieldValue.arrayRemove(itemId)
            }
            isDraft -> {
                updates["draftIds"] = FieldValue.arrayUnion(itemId)
                updates["deletedIds"] = FieldValue.arrayRemove(itemId)
            }
            else -> {
                updates["deletedIds"] = FieldValue.arrayRemove(itemId)
                updates["draftIds"] = FieldValue.arrayRemove(itemId)
            }
        }
        ref.set(updates, SetOptions.merge()).await()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.WRITE, ref.path)
    }
}

fun FirestoreRepository.observeAdminUserUsageSummary(): Flow<AdminUserUsageSummary> {
    if (!isBootstrappedAdminUser()) {
        return flowOf(AdminUserUsageSummary())
    }
    return db.collection("users")
        .snapshots()
        .map { snap ->
            val users = snap.toObjects(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            val nowMs = System.currentTimeMillis()
            val dayAgoMs = nowMs - 24L * 60L * 60L * 1000L
            val weekAgoMs = nowMs - 7L * 24L * 60L * 60L * 1000L

            var activeToday = 0
            var active7Days = 0
            var newThisWeek = 0
            var totalAiUsed = 0L
            val deptCountMap = linkedMapOf<String, Int>()
            val yearCountMap = mutableMapOf(1L to 0, 2L to 0, 3L to 0, 4L to 0)

            for (u in users) {
                val activeMs = (u.lastActiveAt ?: u.updatedAt ?: u.createdAt)?.toDate()?.time ?: 0L
                val createdMs = u.createdAt?.toDate()?.time ?: 0L
                if (activeMs >= dayAgoMs) activeToday++
                if (activeMs >= weekAgoMs) active7Days++
                if (createdMs >= weekAgoMs) newThisWeek++
                totalAiUsed += u.aiUsageCount.coerceAtLeast(0L)

                val deptLabel = u.selectedDepartmentName.ifBlank {
                    AcademicSeedCatalog.defaultDepartments.firstOrNull { it.id == u.selectedDepartmentId }?.nameBn
                        ?: u.selectedDepartmentId.ifBlank { "অনির্ধারিত" }
                }
                deptCountMap[deptLabel] = (deptCountMap[deptLabel] ?: 0) + 1

                val yr = u.selectedYear.coerceIn(1L, 4L)
                yearCountMap[yr] = (yearCountMap[yr] ?: 0) + 1
            }

            val directoryEntries = users
                .map { u ->
                    AdminUserDirectoryEntry(
                        userId = u.userId.ifBlank { u.id },
                        displayName = u.displayName.ifBlank { "NU Student" },
                        email = u.email,
                        selectedDepartmentName = u.selectedDepartmentName.ifBlank {
                            AcademicSeedCatalog.defaultDepartments.firstOrNull { it.id == u.selectedDepartmentId }?.nameBn
                                ?: u.selectedDepartmentId.ifBlank { "অনির্ধারিত" }
                        },
                        selectedYear = u.selectedYear.coerceIn(1L, 4L),
                        aiUsageCount = u.aiUsageCount.coerceAtLeast(0L),
                        lastActiveAt = u.lastActiveAt ?: u.updatedAt ?: u.createdAt,
                        createdAt = u.createdAt
                    )
                }
                .sortedByDescending { (it.lastActiveAt ?: it.createdAt)?.seconds ?: 0L }

            AdminUserUsageSummary(
                totalUsers = users.size,
                activeTodayCount = activeToday,
                activeLast7DaysCount = active7Days,
                newUsersLast7DaysCount = newThisWeek,
                totalAiQueriesUsed = totalAiUsed,
                departmentCounts = deptCountMap.entries
                    .sortedByDescending { it.value }
                    .take(6)
                    .map { it.key to it.value },
                yearCounts = listOf(1L, 2L, 3L, 4L).map { it to (yearCountMap[it] ?: 0) },
                users = directoryEntries
            )
        }
}

fun FirestoreRepository.observeAllDepartmentsForAdmin(): Flow<List<Department>> {
    if (!isBootstrappedAdminUser()) return observePublishedDepartments()
    return db.collection("departments")
        .snapshots()
        .map { snap ->
            snap.toObjects(Department::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                .sortedBy { it.sortOrder }
        }
}

fun FirestoreRepository.observeAllSubjectsForAdmin(): Flow<List<Subject>> {
    if (!isBootstrappedAdminUser()) return observePublishedSubjects()
    return db.collection("subjects")
        .snapshots()
        .map { snap ->
            snap.toObjects(Subject::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                .sortedWith(compareBy({ it.departmentId }, { it.yearNumber }, { it.courseCode }))
        }
}

fun FirestoreRepository.observeAllChaptersForAdmin(): Flow<List<Chapter>> {
    if (!isBootstrappedAdminUser()) return observePublishedChapters()
    return db.collection("chapters")
        .snapshots()
        .map { snap ->
            snap.toObjects(Chapter::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                .sortedWith(compareBy({ it.yearNumber }, { it.chapterNumber }))
        }
}

fun FirestoreRepository.observeAllPdfsForAdmin(): Flow<List<PdfResource>> {
    if (!isBootstrappedAdminUser()) return observePublishedPdfs()
    return db.collection("pdf_library")
        .snapshots()
        .map { snap ->
            snap.toObjects(PdfResource::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                .sortedByDescending { it.createdAt?.seconds ?: 0L }
        }
}

fun FirestoreRepository.observeAllQuestionsForAdmin(): Flow<List<StudyQuestion>> {
    if (!isBootstrappedAdminUser()) return observePublishedQuestions()
    return db.collection("questions")
        .snapshots()
        .map { snap ->
            snap.toObjects(StudyQuestion::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        }
}

fun FirestoreRepository.observeAllAnnouncementsForAdmin(): Flow<List<Announcement>> {
    if (!isBootstrappedAdminUser()) return observePublishedAnnouncements()
    return db.collection("announcements")
        .snapshots()
        .map { snap ->
            snap.toObjects(Announcement::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                .sortedWith(compareByDescending<Announcement> { it.pinned }.thenByDescending { it.createdAt?.seconds ?: 0L })
        }
}

suspend fun FirestoreRepository.deleteChapter(chapterId: String): Result<Unit> {
    val path = "chapters/$chapterId"
    return try {
        requireUserId()
        db.document(path).delete().await()
        syncCatalogMetaEntry(chapterId, isDeleted = true)
        logAdminAction("DELETE_CHAPTER", "Deleted chapter $chapterId")
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.DELETE, path)
        Result.failure(e)
    }
}

suspend fun FirestoreRepository.deleteQuestion(questionId: String): Result<Unit> {
    val path = "questions/$questionId"
    return try {
        requireUserId()
        db.document(path).delete().await()
        syncCatalogMetaEntry(questionId, isDeleted = true)
        logAdminAction("DELETE_QUESTION", "Deleted question $questionId")
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.DELETE, path)
        Result.failure(e)
    }
}

suspend fun FirestoreRepository.seedAllHonoursPdfsToFirestore(): Result<Int> {
    return try {
        val uid = requireUserId()
        val existingDocs = db.collection("pdf_library").get().await()
        val existingCreatedAts = mutableMapOf<String, Timestamp>()
        for (doc in existingDocs.documents) {
            doc.getTimestamp("createdAt")?.let { existingCreatedAts[doc.id] = it }
        }

        val items = AcademicSeedCatalog.starterPdfs
        var written = 0
        for (chunk in items.chunked(350)) {
            db.runBatch { batch ->
                for (pdf in chunk) {
                    val docRef = db.collection("pdf_library").document(pdf.id)
                    val payload = mapOf(
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
                        "createdAt" to (existingCreatedAts[pdf.id] ?: FieldValue.serverTimestamp()),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    batch.set(docRef, payload)
                }
            }.await()
            written += chunk.size
        }

        db.collection("app_settings").document("catalog_meta").set(
            mapOf(
                "seeded" to true,
                "pdfSeedVersion" to AcademicSeedCatalog.PDF_SEED_VERSION,
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
        logAdminAction("SEED_ALL_HONOURS_PDFS", "Published $written Honours PDFs across 22 departments")
        Result.success(written)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.WRITE, "pdf_library/seed_all")
        Result.failure(e)
    }
}

private val MODERATOR_EMAIL_REGEX = Regex("^[a-z0-9._%+\\-]+@[a-z0-9.\\-]+\\.[a-z]{2,}$")

fun FirestoreRepository.observeAuthPolicy(): Flow<AuthPolicyConfig> =
    db.collection("app_settings").document("auth_policy")
        .snapshots()
        .map { snap ->
            if (!snap.exists()) AuthPolicyConfig(requireEmailOtp = true)
            else AuthPolicyConfig(
                requireEmailOtp = snap.getBoolean("requireEmailOtp") ?: true
            )
        }

suspend fun FirestoreRepository.saveAuthPolicy(config: AuthPolicyConfig): Result<Unit> {
    val path = "app_settings/auth_policy"
    return try {
        requireUserId()
        db.document(path).set(
            mapOf(
                "requireEmailOtp" to config.requireEmailOtp,
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
        logAdminAction(
            "UPDATE_AUTH_POLICY",
            "Email 5-digit OTP on login=${config.requireEmailOtp}"
        )
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.WRITE, path)
        Result.failure(e)
    }
}

fun FirestoreRepository.observeIsCollegeModerator(): Flow<Boolean> {
    val user = auth.currentUser ?: return flowOf(false)
    val email = user.email?.trim()?.lowercase().orEmpty()
    if (!user.isEmailVerified || email.isBlank() || !MODERATOR_EMAIL_REGEX.matches(email)) {
        return flowOf(false)
    }
    return db.collection("college_moderators").document(email)
        .snapshots()
        .map { snap ->
            snap.exists() && (snap.getBoolean("active") == true)
        }
}

fun FirestoreRepository.observeCollegeModerators(): Flow<List<CollegeModerator>> {
    if (!isBootstrappedAdminUser()) {
        return flowOf(emptyList())
    }
    return db.collection("college_moderators")
        .snapshots()
        .map { snap ->
            snap.toObjects(CollegeModerator::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                .sortedBy { it.email }
        }
}

suspend fun FirestoreRepository.upsertCollegeModerator(
    email: String,
    displayName: String,
    note: String,
    active: Boolean
): Result<Unit> {
    val normalizedEmail = email.trim().lowercase()
    return try {
        requireUserId()
        require(normalizedEmail.length in 5..160 && MODERATOR_EMAIL_REGEX.matches(normalizedEmail)) {
            "সঠিক Gmail / ইমেইল ঠিকানা লিখুন।"
        }
        val docRef = db.collection("college_moderators").document(normalizedEmail)
        val existing = docRef.get().await()
        val addedBy = auth.currentUser?.email.orEmpty().take(160)
        docRef.set(
            mapOf(
                "email" to normalizedEmail,
                "displayName" to displayName.trim().take(120),
                "note" to note.trim().take(300),
                "active" to active,
                "addedByEmail" to (existing.getString("addedByEmail")?.takeIf { it.isNotBlank() } ?: addedBy),
                "createdAt" to (existing.getTimestamp("createdAt") ?: FieldValue.serverTimestamp()),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
        logAdminAction(
            "UPSERT_COLLEGE_MODERATOR",
            "Sujat Ali College moderator $normalizedEmail (active=$active)"
        )
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.WRITE, "college_moderators/$normalizedEmail")
        Result.failure(e)
    }
}

suspend fun FirestoreRepository.deleteCollegeModerator(moderatorEmail: String): Result<Unit> {
    val normalizedEmail = moderatorEmail.trim().lowercase()
    val path = "college_moderators/$normalizedEmail"
    return try {
        requireUserId()
        require(MODERATOR_EMAIL_REGEX.matches(normalizedEmail)) { "অবৈধ ইমেইল।" }
        db.document(path).delete().await()
        logAdminAction("DELETE_COLLEGE_MODERATOR", "Removed Sujat Ali College moderator $normalizedEmail")
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, OperationType.DELETE, path)
        Result.failure(e)
    }
}
