package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.AdminLockoutState
import com.example.core.PdfUtils
import com.example.core.PreferencesManager
import com.example.core.toUserFriendlyFirestoreMessage
import com.example.data.catalog.AcademicSeedCatalog
import com.example.data.model.AdminLog
import com.example.data.model.AdminUserUsageSummary
import com.example.data.model.AiLimitConfig
import com.example.data.model.AiLimitPurchaseRequest
import com.example.data.model.AiRemoteConfig
import com.example.data.model.Announcement
import com.example.data.model.AuthPolicyConfig
import com.example.data.model.CatalogMetaConfig
import com.example.data.model.Chapter
import com.example.data.model.CollegeModerator
import com.example.data.model.CollegeProfileConfig
import com.example.data.model.CollegeSectionBlock
import com.example.data.model.Department
import com.example.data.model.DeveloperProfileConfig
import com.example.data.model.PdfResource
import com.example.data.model.StudyQuestion
import com.example.data.model.Subject
import com.example.data.model.UiState
import com.example.data.model.UserReport
import com.example.data.repository.FirestoreRepository
import com.example.data.repository.deleteChapter
import com.example.data.repository.deleteCollegeModerator
import com.example.data.repository.deleteQuestion
import com.example.data.repository.mergeCatalogItems
import com.example.data.repository.observeAdminUserUsageSummary
import com.example.data.repository.observeAllAnnouncementsForAdmin
import com.example.data.repository.observeAllChaptersForAdmin
import com.example.data.repository.observeAllDepartmentsForAdmin
import com.example.data.repository.observeAllPdfsForAdmin
import com.example.data.repository.observeAllQuestionsForAdmin
import com.example.data.repository.observeAllSubjectsForAdmin
import com.example.data.repository.observeAuthPolicy
import com.example.data.repository.observeCatalogMeta
import com.example.data.repository.observeCollegeModerators
import com.example.data.repository.observeIsCollegeModerator
import com.example.data.repository.saveAuthPolicy
import com.example.data.repository.seedAllHonoursPdfsToFirestore
import com.example.data.repository.upsertCollegeModerator
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminViewModel(
    private val firestoreRepo: FirestoreRepository,
    private val prefs: PreferencesManager,
    private val appContext: Context
) : ViewModel() {

    companion object {
        private const val TAG = "AdminViewModel"
        private const val STOP_TIMEOUT_MS = 5000L
    }

    val lockoutState: StateFlow<AdminLockoutState> = prefs.adminLockoutState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AdminLockoutState())

    private val _isAdminAuthorized = MutableStateFlow(false)
    val isAdminAuthorized: StateFlow<Boolean> = _isAdminAuthorized.asStateFlow()

    private val _isBackendAdminAccount = MutableStateFlow(firestoreRepo.isBootstrappedAdminUser())
    val isBackendAdminAccount: StateFlow<Boolean> = _isBackendAdminAccount.asStateFlow()

    private val _hasPinConfigured = MutableStateFlow<Boolean?>(null)
    val hasPinConfigured: StateFlow<Boolean?> = _hasPinConfigured.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isVerifying = MutableStateFlow(false)
    val isVerifying: StateFlow<Boolean> = _isVerifying.asStateFlow()

    private val _adminToast = MutableStateFlow<String?>(null)
    val adminToast: StateFlow<String?> = _adminToast.asStateFlow()

    val reports: StateFlow<UiState<List<UserReport>>> = firestoreRepo.observeAllReportsForAdmin()
        .map<List<UserReport>, UiState<List<UserReport>>> { UiState.Success(it) }
        .catch { err ->
            val firestoreErr = generateSequence(err) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreErr == null && err is CancellationException) throw err
            emit(UiState.Error((firestoreErr ?: err).toUserFriendlyFirestoreMessage("Unable to load user reports.")))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    val adminLogs: StateFlow<List<AdminLog>> = firestoreRepo.observeAdminLogs()
        .catch { err ->
            val firestoreErr = generateSequence(err) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreErr == null && err is CancellationException) throw err
            Log.w(TAG, "Unable to load admin logs", firestoreErr ?: err)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val purchaseRequests: StateFlow<UiState<List<AiLimitPurchaseRequest>>> =
        firestoreRepo.observeAllPurchaseRequestsForAdmin()
            .map<List<AiLimitPurchaseRequest>, UiState<List<AiLimitPurchaseRequest>>> { UiState.Success(it) }
            .catch { err ->
                val firestoreErr = generateSequence(err) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreErr == null && err is CancellationException) throw err
                emit(UiState.Error((firestoreErr ?: err).toUserFriendlyFirestoreMessage("পেমেন্ট রিকোয়েস্ট লোড করা যায়নি।")))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    val aiLimitConfig: StateFlow<AiLimitConfig> = firestoreRepo.observeAiLimitConfig()
        .catch { err ->
            val firestoreErr = generateSequence(err) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreErr == null && err is CancellationException) throw err
            emit(AiLimitConfig())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AiLimitConfig())

    val collegeProfile: StateFlow<CollegeProfileConfig> = firestoreRepo.observeCollegeProfile()
        .catch { err ->
            val firestoreErr = generateSequence(err) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreErr == null && err is CancellationException) throw err
            emit(CollegeProfileConfig())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), CollegeProfileConfig())

    val adminCollegeSections: StateFlow<List<CollegeSectionBlock>> = firestoreRepo.observeAllCollegeSectionsForAdmin()
        .catch { err ->
            val firestoreErr = generateSequence(err) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreErr == null && err is CancellationException) throw err
            Log.w(TAG, "Unable to load admin college sections", firestoreErr ?: err)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val catalogMeta: StateFlow<CatalogMetaConfig> = firestoreRepo.observeCatalogMeta()
        .catch { err ->
            val firestoreErr = generateSequence(err) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreErr == null && err is CancellationException) throw err
            emit(CatalogMetaConfig())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), CatalogMetaConfig())

    val adminUsageSummary: StateFlow<AdminUserUsageSummary> = firestoreRepo.observeAdminUserUsageSummary()
        .catch { err ->
            val firestoreErr = generateSequence(err) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreErr == null && err is CancellationException) throw err
            Log.w(TAG, "Unable to load admin user usage summary", firestoreErr ?: err)
            emit(AdminUserUsageSummary())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AdminUserUsageSummary())

    val isCollegeModerator: StateFlow<Boolean> = firestoreRepo.observeIsCollegeModerator()
        .catch { err ->
            val firestoreErr = generateSequence(err) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreErr == null && err is CancellationException) throw err
            emit(false)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), false)

    val authPolicy: StateFlow<AuthPolicyConfig> = firestoreRepo.observeAuthPolicy()
        .catch { err ->
            val firestoreErr = generateSequence(err) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreErr == null && err is CancellationException) throw err
            emit(AuthPolicyConfig(requireEmailOtp = true))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AuthPolicyConfig(requireEmailOtp = true))

    val collegeModerators: StateFlow<List<CollegeModerator>> = firestoreRepo.observeCollegeModerators()
        .catch { err ->
            val firestoreErr = generateSequence(err) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreErr == null && err is CancellationException) throw err
            Log.w(TAG, "Unable to load college moderators", firestoreErr ?: err)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val adminDepartments: StateFlow<List<Department>> = combine(
        firestoreRepo.observeAllDepartmentsForAdmin()
            .catch { err ->
                val firestoreErr = generateSequence(err) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                if (firestoreErr == null && err is CancellationException) throw err
                emit(emptyList())
            },
        catalogMeta
    ) { live, meta ->
        mergeCatalogItems(live, AcademicSeedCatalog.defaultDepartments, meta, includeDrafts = true) { it.id }
            .sortedBy { it.sortOrder }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AcademicSeedCatalog.defaultDepartments)

    val adminSubjects: StateFlow<List<Subject>> = combine(
        firestoreRepo.observeAllSubjectsForAdmin()
            .catch { err ->
                val firestoreErr = generateSequence(err) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                if (firestoreErr == null && err is CancellationException) throw err
                emit(emptyList())
            },
        catalogMeta
    ) { live, meta ->
        mergeCatalogItems(live, AcademicSeedCatalog.starterSubjects, meta, includeDrafts = true) { it.id }
            .sortedWith(compareBy({ it.departmentId }, { it.yearNumber }, { it.courseCode }))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AcademicSeedCatalog.starterSubjects)

    val adminChapters: StateFlow<List<Chapter>> = combine(
        firestoreRepo.observeAllChaptersForAdmin()
            .catch { err ->
                val firestoreErr = generateSequence(err) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                if (firestoreErr == null && err is CancellationException) throw err
                emit(emptyList())
            },
        catalogMeta
    ) { live, meta ->
        mergeCatalogItems(live, AcademicSeedCatalog.starterChapters, meta, includeDrafts = true) { it.id }
            .sortedWith(compareBy({ it.yearNumber }, { it.chapterNumber }))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AcademicSeedCatalog.starterChapters)

    val adminPdfs: StateFlow<List<PdfResource>> = combine(
        firestoreRepo.observeAllPdfsForAdmin()
            .catch { err ->
                val firestoreErr = generateSequence(err) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                if (firestoreErr == null && err is CancellationException) throw err
                emit(emptyList())
            },
        catalogMeta
    ) { live, meta ->
        mergeCatalogItems(live, AcademicSeedCatalog.starterPdfs, meta, includeDrafts = true) { it.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AcademicSeedCatalog.starterPdfs)

    val adminQuestions: StateFlow<List<StudyQuestion>> = combine(
        firestoreRepo.observeAllQuestionsForAdmin()
            .catch { err ->
                val firestoreErr = generateSequence(err) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                if (firestoreErr == null && err is CancellationException) throw err
                emit(emptyList())
            },
        catalogMeta
    ) { live, meta ->
        mergeCatalogItems(live, AcademicSeedCatalog.starterQuestions, meta, includeDrafts = true) { it.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AcademicSeedCatalog.starterQuestions)

    val adminAnnouncements: StateFlow<List<Announcement>> = combine(
        firestoreRepo.observeAllAnnouncementsForAdmin()
            .catch { err ->
                val firestoreErr = generateSequence(err) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                if (firestoreErr == null && err is CancellationException) throw err
                emit(emptyList())
            },
        catalogMeta
    ) { live, meta ->
        mergeCatalogItems(live, AcademicSeedCatalog.starterAnnouncements, meta, includeDrafts = true) { it.id }
            .sortedWith(compareByDescending<Announcement> { it.pinned }.thenByDescending { it.createdAt?.seconds ?: 0L })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AcademicSeedCatalog.starterAnnouncements)

    fun refreshAdminStatus() {
        viewModelScope.launch {
            val isAdmin = firestoreRepo.checkIsAdmin()
            _isBackendAdminAccount.value = isAdmin
            if (isAdmin) {
                _hasPinConfigured.value = firestoreRepo.hasAdminPinConfigured()
            } else {
                _hasPinConfigured.value = null
            }
        }
    }

    fun clearAdminToast() {
        _adminToast.value = null
    }

    fun clearAuthError() {
        _authError.value = null
    }

    fun initializeBackendPin(pin: String) {
        viewModelScope.launch {
            _isVerifying.value = true
            _authError.value = null
            firestoreRepo.initializeAdminPin(pin)
                .onSuccess {
                    _hasPinConfigured.value = true
                    _isAdminAuthorized.value = true
                    prefs.resetAdminFailedAttempts()
                }
                .onFailure { err ->
                    _authError.value = err.toUserFriendlyFirestoreMessage("Failed to initialize admin PIN.")
                }
            _isVerifying.value = false
        }
    }

    fun verifyAndUnlock(enteredPin: String) {
        val currentLockout = lockoutState.value
        val now = System.currentTimeMillis()
        if (currentLockout.lockoutUntilMs > now) {
            val remainingSec = ((currentLockout.lockoutUntilMs - now) / 1000L).coerceAtLeast(1L)
            _authError.value = "Too many failed attempts. Locked for ${remainingSec}s."
            return
        }

        viewModelScope.launch {
            _isVerifying.value = true
            _authError.value = null

            val isAdminAccount = firestoreRepo.checkIsAdmin()
            _isBackendAdminAccount.value = isAdminAccount
            if (!isAdminAccount) {
                prefs.recordAdminFailedAttempt()
                _authError.value = "Unauthorized account. Only designated administrators can access Content Management."
                _isVerifying.value = false
                return@launch
            }

            val result = firestoreRepo.verifyAdminPin(enteredPin)
            result.onSuccess { matched ->
                if (matched) {
                    prefs.resetAdminFailedAttempts()
                    _isAdminAuthorized.value = true
                    firestoreRepo.logAdminAction("ADMIN_UNLOCK", "Unlocked Content Management Panel")
                } else {
                    prefs.recordAdminFailedAttempt()
                    val attempts = lockoutState.value.failedAttempts + 1
                    _authError.value = "Incorrect password. Attempt $attempts of 5."
                }
            }.onFailure { err ->
                _authError.value = err.toUserFriendlyFirestoreMessage("Verification error.")
            }
            _isVerifying.value = false
        }
    }

    fun lockAdminPanel() {
        _isAdminAuthorized.value = false
    }

    fun seedStarterCatalog() {
        viewModelScope.launch {
            firestoreRepo.seedDefaultCatalogToFirestore()
                .onSuccess { count -> _adminToast.value = "$count টি একাডেমিক আইটেম ও সকল বিভাগের PDF সবার কাছে প্রকাশিত হয়েছে।" }
                .onFailure { err -> _adminToast.value = err.toUserFriendlyFirestoreMessage("Failed to seed catalog.") }
        }
    }

    fun seedAllHonoursPdfs() {
        viewModelScope.launch {
            firestoreRepo.seedAllHonoursPdfsToFirestore()
                .onSuccess { count -> _adminToast.value = "সকল ২২টি বিভাগের $count টি অনার্স PDF সবার কাছে অটো আপডেট হয়েছে।" }
                .onFailure { err -> _adminToast.value = err.toUserFriendlyFirestoreMessage("PDF প্রকাশ করা যায়নি।") }
        }
    }

    fun saveDepartment(dept: Department) {
        viewModelScope.launch {
            firestoreRepo.upsertDepartment(dept)
                .onSuccess { _adminToast.value = "${dept.nameBn} সংরক্ষণ হয়েছে ও সবার কাছে অটো সিঙ্ক হয়েছে।" }
                .onFailure { _adminToast.value = it.toUserFriendlyFirestoreMessage("Failed to save department.") }
        }
    }

    fun deleteDepartment(id: String) {
        viewModelScope.launch {
            firestoreRepo.deleteDepartment(id)
                .onSuccess { _adminToast.value = "বিভাগটি মুছে ফেলা হয়েছে ও লাইভ আপডেট হয়েছে।" }
                .onFailure { _adminToast.value = it.toUserFriendlyFirestoreMessage("Failed to delete department.") }
        }
    }

    fun saveSubject(subject: Subject) {
        viewModelScope.launch {
            firestoreRepo.upsertSubject(subject)
                .onSuccess { _adminToast.value = "${subject.courseCode} সংরক্ষণ হয়েছে ও সবার কাছে অটো সিঙ্ক হয়েছে।" }
                .onFailure { _adminToast.value = it.toUserFriendlyFirestoreMessage("Failed to save subject.") }
        }
    }

    fun deleteSubject(id: String) {
        viewModelScope.launch {
            firestoreRepo.deleteSubject(id)
                .onSuccess { _adminToast.value = "বিষয়টি মুছে ফেলা হয়েছে।" }
                .onFailure { _adminToast.value = it.toUserFriendlyFirestoreMessage("Failed to delete subject.") }
        }
    }

    fun saveChapter(chapter: Chapter) {
        viewModelScope.launch {
            firestoreRepo.upsertChapter(chapter)
                .onSuccess { _adminToast.value = "অধ্যায় ${chapter.chapterNumber} সংরক্ষণ ও লাইভ সিঙ্ক হয়েছে।" }
                .onFailure { _adminToast.value = it.toUserFriendlyFirestoreMessage("Failed to save chapter.") }
        }
    }

    fun deleteChapter(id: String) {
        viewModelScope.launch {
            firestoreRepo.deleteChapter(id)
                .onSuccess { _adminToast.value = "অধ্যায়টি মুছে ফেলা হয়েছে।" }
                .onFailure { _adminToast.value = it.toUserFriendlyFirestoreMessage("Failed to delete chapter.") }
        }
    }

    fun uploadPdfResource(pdf: PdfResource, pickedPdfUri: Uri?) {
        viewModelScope.launch {
            try {
                var finalPdf = pdf
                if (pickedPdfUri != null) {
                    val fileName = "admin_${System.currentTimeMillis()}.pdf"
                    val (copiedFile, pageCount) = PdfUtils.copyUriToPdfFile(appContext, pickedPdfUri, fileName)
                    finalPdf = pdf.copy(
                        localFileName = copiedFile.name,
                        pageCount = pageCount.toLong(),
                        fileSizeBytes = copiedFile.length()
                    )
                }
                firestoreRepo.upsertPdfResource(finalPdf)
                    .onSuccess { _adminToast.value = "PDF '${finalPdf.title}' (${finalPdf.status}) saved!" }
                    .onFailure { _adminToast.value = it.toUserFriendlyFirestoreMessage("Failed to save PDF metadata.") }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _adminToast.value = e.localizedMessage ?: "Failed to process selected PDF file."
            }
        }
    }

    fun deletePdf(id: String) {
        viewModelScope.launch {
            firestoreRepo.deletePdfResource(id)
                .onSuccess { _adminToast.value = "PDF deleted from library." }
                .onFailure { _adminToast.value = it.toUserFriendlyFirestoreMessage("Failed to delete PDF.") }
        }
    }

    fun saveQuestion(question: StudyQuestion) {
        viewModelScope.launch {
            firestoreRepo.upsertQuestion(question)
                .onSuccess { _adminToast.value = "প্রশ্ন সংরক্ষণ ও সবার কাছে অটো সিঙ্ক হয়েছে।" }
                .onFailure { _adminToast.value = it.toUserFriendlyFirestoreMessage("Failed to save question.") }
        }
    }

    fun deleteQuestion(id: String) {
        viewModelScope.launch {
            firestoreRepo.deleteQuestion(id)
                .onSuccess { _adminToast.value = "প্রশ্নটি মুছে ফেলা হয়েছে।" }
                .onFailure { _adminToast.value = it.toUserFriendlyFirestoreMessage("Failed to delete question.") }
        }
    }

    fun saveAnnouncement(announcement: Announcement) {
        viewModelScope.launch {
            firestoreRepo.upsertAnnouncement(announcement)
                .onSuccess { _adminToast.value = "Announcement saved." }
                .onFailure { _adminToast.value = it.toUserFriendlyFirestoreMessage("Failed to publish announcement.") }
        }
    }

    fun deleteAnnouncement(id: String) {
        viewModelScope.launch {
            firestoreRepo.deleteAnnouncement(id)
                .onSuccess { _adminToast.value = "Announcement deleted." }
                .onFailure { _adminToast.value = it.toUserFriendlyFirestoreMessage("Failed to delete announcement.") }
        }
    }

    fun updateReportStatus(reportId: String, status: String, adminNote: String) {
        viewModelScope.launch {
            firestoreRepo.updateReportStatus(reportId, status, adminNote)
                .onSuccess { _adminToast.value = "Report updated to $status." }
                .onFailure { _adminToast.value = it.toUserFriendlyFirestoreMessage("Failed to update report.") }
        }
    }

    fun saveAiConfig(config: AiRemoteConfig) {
        viewModelScope.launch {
            firestoreRepo.saveAiRemoteConfig(config)
                .onSuccess { _adminToast.value = "AI instructions & model config updated remotely." }
                .onFailure { _adminToast.value = it.toUserFriendlyFirestoreMessage("Failed to save AI config.") }
        }
    }

    fun saveDeveloperProfile(config: DeveloperProfileConfig) {
        viewModelScope.launch {
            firestoreRepo.saveDeveloperProfile(config)
                .onSuccess { _adminToast.value = "ডেভেলপারের সকল তথ্য সফলভাবে আপডেট করা হয়েছে।" }
                .onFailure { _adminToast.value = it.toUserFriendlyFirestoreMessage("Failed to update developer profile.") }
        }
    }

    fun saveAiLimitConfig(config: AiLimitConfig) {
        viewModelScope.launch {
            firestoreRepo.saveAiLimitConfig(config)
                .onSuccess { _adminToast.value = "AI লিমিট ও পেমেন্ট সেটিংস সংরক্ষণ করা হয়েছে।" }
                .onFailure { _adminToast.value = it.toUserFriendlyFirestoreMessage("Failed to save AI limit config.") }
        }
    }

    fun reviewPurchaseRequest(requestId: String, approve: Boolean, adminNote: String) {
        viewModelScope.launch {
            firestoreRepo.reviewAiLimitPurchaseRequest(requestId, approve, adminNote)
                .onSuccess {
                    _adminToast.value = if (approve) {
                        "পেমেন্ট অনুমোদিত হয়েছে এবং শিক্ষার্থীর অ্যাকাউন্টে লিমিট যোগ করা হয়েছে!"
                    } else {
                        "পেমেন্ট রিকোয়েস্ট বাতিল করা হয়েছে।"
                    }
                }
                .onFailure { err ->
                    _adminToast.value = err.toUserFriendlyFirestoreMessage("রিকোয়েস্ট প্রসেস করা যায়নি।")
                }
        }
    }

    // --- Sujat Ali Honours College, Pingna Admin Actions ---
    fun saveCollegeProfile(config: CollegeProfileConfig) {
        viewModelScope.launch {
            firestoreRepo.saveCollegeProfile(config)
                .onSuccess { _adminToast.value = "সুজাত আলী অনার্স কলেজের থিম ও তথ্য সংরক্ষণ করা হয়েছে।" }
                .onFailure { _adminToast.value = it.toUserFriendlyFirestoreMessage("কলেজের তথ্য সংরক্ষণ করা যায়নি।") }
        }
    }

    fun saveCollegeSection(section: CollegeSectionBlock) {
        viewModelScope.launch {
            firestoreRepo.upsertCollegeSection(section)
                .onSuccess { _adminToast.value = "কলেজ কার্ড '${section.title}' সংরক্ষণ করা হয়েছে।" }
                .onFailure { _adminToast.value = it.toUserFriendlyFirestoreMessage("কলেজ কার্ড সংরক্ষণ করা যায়নি।") }
        }
    }

    fun deleteCollegeSection(sectionId: String) {
        viewModelScope.launch {
            firestoreRepo.deleteCollegeSection(sectionId)
                .onSuccess { _adminToast.value = "কার্ডটি মুছে ফেলা হয়েছে।" }
                .onFailure { _adminToast.value = it.toUserFriendlyFirestoreMessage("কার্ড মুছে ফেলা যায়নি।") }
        }
    }

    fun moveCollegeSection(index: Int, direction: Int) {
        val list = adminCollegeSections.value
        val targetIndex = index + direction
        if (index !in list.indices || targetIndex !in list.indices) return
        val first = list[index]
        val second = list[targetIndex]
        viewModelScope.launch {
            firestoreRepo.swapCollegeSectionOrder(first, second)
                .onFailure { _adminToast.value = it.toUserFriendlyFirestoreMessage("ক্রম পরিবর্তন করা যায়নি।") }
        }
    }

    fun toggleCollegeSectionStatus(section: CollegeSectionBlock) {
        val nextStatus = if (section.status == "published") "draft" else "published"
        saveCollegeSection(section.copy(status = nextStatus))
    }

    fun seedCollegeStarterData() {
        viewModelScope.launch {
            firestoreRepo.seedDefaultCollegeToFirestore()
                .onSuccess { count -> _adminToast.value = "সুজাত আলী কলেজের $count টি প্রারম্ভিক কার্ড Firestore-এ যুক্ত হয়েছে।" }
                .onFailure { _adminToast.value = it.toUserFriendlyFirestoreMessage("টেমপ্লেট লোড করা যায়নি।") }
        }
    }

    fun saveAuthPolicy(config: AuthPolicyConfig) {
        viewModelScope.launch {
            firestoreRepo.saveAuthPolicy(config)
                .onSuccess {
                    _adminToast.value = if (config.requireEmailOtp) {
                        "লগইনে ৫ সংখ্যার ইমেইল ভেরিফিকেশন কোড সক্রিয় করা হয়েছে।"
                    } else {
                        "লগইনে ইমেইল OTP নীতি বন্ধ করা হয়েছে।"
                    }
                }
                .onFailure { _adminToast.value = it.toUserFriendlyFirestoreMessage("অথেনটিকেশন নীতি সংরক্ষণ করা যায়নি।") }
        }
    }

    fun saveCollegeModerator(email: String, displayName: String, note: String, active: Boolean) {
        viewModelScope.launch {
            firestoreRepo.upsertCollegeModerator(email, displayName, note, active)
                .onSuccess {
                    _adminToast.value = "সুজাত আলী কলেজের মডারেটর (${email.trim().lowercase()}) সংরক্ষণ করা হয়েছে।"
                }
                .onFailure { _adminToast.value = it.toUserFriendlyFirestoreMessage("মডারেটর যুক্ত করা যায়নি।") }
        }
    }

    fun deleteCollegeModerator(email: String) {
        viewModelScope.launch {
            firestoreRepo.deleteCollegeModerator(email)
                .onSuccess { _adminToast.value = "কলেজ মডারেটর মুছে ফেলা হয়েছে।" }
                .onFailure { _adminToast.value = it.toUserFriendlyFirestoreMessage("মডারেটর মুছে ফেলা যায়নি।") }
        }
    }
}
