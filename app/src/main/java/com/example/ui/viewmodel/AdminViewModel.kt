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
import com.example.data.model.AdminLog
import com.example.data.model.AiRemoteConfig
import com.example.data.model.Announcement
import com.example.data.model.Chapter
import com.example.data.model.Department
import com.example.data.model.DeveloperProfileConfig
import com.example.data.model.PdfResource
import com.example.data.model.StudyQuestion
import com.example.data.model.Subject
import com.example.data.model.UiState
import com.example.data.model.UserReport
import com.example.data.repository.FirestoreRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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
                .onSuccess { count -> _adminToast.value = "Published $count starter items to Firestore." }
                .onFailure { err -> _adminToast.value = err.toUserFriendlyFirestoreMessage("Failed to seed catalog.") }
        }
    }

    fun saveDepartment(dept: Department) {
        viewModelScope.launch {
            firestoreRepo.upsertDepartment(dept)
                .onSuccess { _adminToast.value = "Department saved (${dept.nameBn})." }
                .onFailure { _adminToast.value = it.toUserFriendlyFirestoreMessage("Failed to save department.") }
        }
    }

    fun deleteDepartment(id: String) {
        viewModelScope.launch {
            firestoreRepo.deleteDepartment(id)
                .onSuccess { _adminToast.value = "Department deleted." }
                .onFailure { _adminToast.value = it.toUserFriendlyFirestoreMessage("Failed to delete department.") }
        }
    }

    fun saveSubject(subject: Subject) {
        viewModelScope.launch {
            firestoreRepo.upsertSubject(subject)
                .onSuccess { _adminToast.value = "Subject saved (${subject.courseCode})." }
                .onFailure { _adminToast.value = it.toUserFriendlyFirestoreMessage("Failed to save subject.") }
        }
    }

    fun deleteSubject(id: String) {
        viewModelScope.launch {
            firestoreRepo.deleteSubject(id)
                .onSuccess { _adminToast.value = "Subject deleted." }
                .onFailure { _adminToast.value = it.toUserFriendlyFirestoreMessage("Failed to delete subject.") }
        }
    }

    fun saveChapter(chapter: Chapter) {
        viewModelScope.launch {
            firestoreRepo.upsertChapter(chapter)
                .onSuccess { _adminToast.value = "Chapter ${chapter.chapterNumber} saved." }
                .onFailure { _adminToast.value = it.toUserFriendlyFirestoreMessage("Failed to save chapter.") }
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
                .onSuccess { _adminToast.value = "Question saved." }
                .onFailure { _adminToast.value = it.toUserFriendlyFirestoreMessage("Failed to save question.") }
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
}
