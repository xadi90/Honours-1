package com.example.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.AppUpdateInfo
import com.example.core.NetworkMonitor
import com.example.core.PreferencesManager
import com.example.core.UpdateChecker
import com.example.core.toUserFriendlyFirestoreMessage
import com.example.data.catalog.AcademicSeedCatalog
import com.example.data.catalog.CollegeSeedCatalog
import com.example.data.model.AiLimitConfig
import com.example.data.model.AiLimitPurchaseRequest
import com.example.data.model.AiRemoteConfig
import com.example.data.model.Announcement
import com.example.data.model.AuthPolicyConfig
import com.example.data.model.BookmarkItem
import com.example.data.model.CatalogMetaConfig
import com.example.data.model.CollegeProfileConfig
import com.example.data.model.CollegeSectionBlock
import com.example.data.model.DeveloperProfileConfig
import com.example.data.model.ModelTestResult
import com.example.data.model.UiState
import com.example.data.model.UserProfile
import com.example.data.repository.FirestoreRepository
import com.example.data.repository.mergeCatalogItems
import com.example.data.repository.observeAuthPolicy
import com.example.data.repository.observeCatalogMeta
import com.example.data.repository.observeIsCollegeModerator
import com.example.ui.theme.AppThemeMode
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val firestoreRepo: FirestoreRepository,
    private val prefs: PreferencesManager,
    private val networkMonitor: NetworkMonitor,
    private val currentUserId: String
) : ViewModel() {

    companion object {
        private const val TAG = "MainViewModel"
        private const val STOP_TIMEOUT_MS = 5000L
    }

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

    val themeMode: StateFlow<AppThemeMode> = prefs.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AppThemeMode.SYSTEM)

    val languagePref: StateFlow<String> = prefs.languagePref
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), "bn_en")

    val onboardingCompleted: StateFlow<Boolean> = prefs.onboardingCompleted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), true)

    val searchHistory: StateFlow<List<String>> = prefs.searchHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val recentPdfIds: StateFlow<List<String>> = prefs.recentPdfIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val recentSubjectIds: StateFlow<List<String>> = prefs.recentSubjectIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val userProfile: StateFlow<UiState<UserProfile?>> = firestoreRepo.observeUserProfile(currentUserId)
        .asUiState("Failed to load profile")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    val bookmarks: StateFlow<UiState<List<BookmarkItem>>> = firestoreRepo.observeBookmarks(currentUserId)
        .asUiState("Failed to load bookmarks")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    val testResults: StateFlow<UiState<List<ModelTestResult>>> = firestoreRepo.observeModelTestResults(currentUserId)
        .asUiState("Failed to load test history")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    val announcements: StateFlow<List<Announcement>> = combine(
        firestoreRepo.observePublishedAnnouncements(),
        firestoreRepo.observeCatalogMeta().catch { error ->
            unwrapFirestoreErrorOrThrow(error, "catalog_meta")
            emit(CatalogMetaConfig())
        }
    ) { live, meta ->
        mergeCatalogItems(live, AcademicSeedCatalog.starterAnnouncements, meta, includeDrafts = false) { it.id }
            .sortedWith(compareByDescending<Announcement> { it.pinned }.thenByDescending { it.createdAt?.seconds ?: 0L })
    }
        .catch { error ->
            unwrapFirestoreErrorOrThrow(error, "announcements")
            emit(AcademicSeedCatalog.starterAnnouncements)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AcademicSeedCatalog.starterAnnouncements)

    val aiRemoteConfig: StateFlow<AiRemoteConfig> = firestoreRepo.observeAiRemoteConfig()
        .catch { error ->
            unwrapFirestoreErrorOrThrow(error, "ai_config")
            emit(AiRemoteConfig())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AiRemoteConfig())

    val developerProfile: StateFlow<DeveloperProfileConfig> = firestoreRepo.observeDeveloperProfile()
        .catch { error ->
            unwrapFirestoreErrorOrThrow(error, "developer_profile")
            emit(DeveloperProfileConfig())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), DeveloperProfileConfig())

    val aiLimitConfig: StateFlow<AiLimitConfig> = firestoreRepo.observeAiLimitConfig()
        .catch { error ->
            unwrapFirestoreErrorOrThrow(error, "ai_limit_config")
            emit(AiLimitConfig())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AiLimitConfig())

    val myPurchaseRequests: StateFlow<List<AiLimitPurchaseRequest>> =
        firestoreRepo.observeUserPurchaseRequests(currentUserId)
            .catch { error ->
                unwrapFirestoreErrorOrThrow(error, "ai_purchase_requests")
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val collegeProfile: StateFlow<CollegeProfileConfig> = firestoreRepo.observeCollegeProfile()
        .catch { error ->
            unwrapFirestoreErrorOrThrow(error, "sujat_ali_college")
            emit(CollegeProfileConfig())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), CollegeProfileConfig())

    val collegeSections: StateFlow<List<CollegeSectionBlock>> = firestoreRepo.observePublishedCollegeSections()
        .catch { error ->
            unwrapFirestoreErrorOrThrow(error, "college_sections")
            emit(CollegeSeedCatalog.starterSections)
        }
        .map { list -> list.ifEmpty { CollegeSeedCatalog.starterSections } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), CollegeSeedCatalog.starterSections)

    private val _isAdmin = MutableStateFlow(firestoreRepo.isBootstrappedAdminUser())
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    val isCollegeModerator: StateFlow<Boolean> = firestoreRepo.observeIsCollegeModerator()
        .catch { error ->
            unwrapFirestoreErrorOrThrow(error, "college_moderators")
            emit(false)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), false)

    val authPolicy: StateFlow<AuthPolicyConfig> = firestoreRepo.observeAuthPolicy()
        .catch { error ->
            unwrapFirestoreErrorOrThrow(error, "auth_policy")
            emit(AuthPolicyConfig(requireEmailOtp = true))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AuthPolicyConfig(requireEmailOtp = true))

    private val _updateState = MutableStateFlow<UiState<AppUpdateInfo>>(UiState.Idle)
    val updateState: StateFlow<UiState<AppUpdateInfo>> = _updateState.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    init {
        viewModelScope.launch {
            firestoreRepo.ensureUserProfileExists()
            _isAdmin.value = firestoreRepo.checkIsAdmin()
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch { prefs.setThemeMode(mode) }
    }

    fun setLanguagePref(lang: String) {
        viewModelScope.launch { prefs.setLanguagePref(lang) }
    }

    fun addSearchQuery(query: String) {
        viewModelScope.launch { prefs.addSearchHistory(query) }
    }

    fun clearSearchHistory() {
        viewModelScope.launch { prefs.clearSearchHistory() }
    }

    fun recordRecentPdf(pdfId: String) {
        viewModelScope.launch { prefs.recordRecentPdf(pdfId) }
    }

    fun recordRecentSubject(subjectId: String) {
        viewModelScope.launch { prefs.recordRecentSubject(subjectId) }
    }

    fun saveProfilePreferences(
        displayName: String,
        departmentId: String,
        departmentName: String,
        yearNumber: Long,
        languagePreference: String
    ) {
        viewModelScope.launch {
            firestoreRepo.updateUserPreferences(
                userId = currentUserId,
                displayName = displayName,
                departmentId = departmentId,
                departmentName = departmentName,
                yearNumber = yearNumber,
                languagePreference = languagePreference
            ).onSuccess {
                prefs.setLanguagePref(languagePreference)
                _actionMessage.value = "Profile preferences updated."
            }.onFailure { err ->
                _actionMessage.value = err.toUserFriendlyFirestoreMessage("Unable to update profile.")
            }
        }
    }

    fun toggleBookmark(
        itemType: String,
        targetId: String,
        title: String,
        subtitle: String,
        pageNumber: Long = 0L
    ) {
        val currentList = (bookmarks.value as? UiState.Success)?.data ?: emptyList()
        val existing = currentList.firstOrNull {
            it.itemType == itemType && it.targetId == targetId && it.pageNumber == pageNumber
        }
        viewModelScope.launch {
            if (existing != null) {
                firestoreRepo.removeBookmark(currentUserId, existing.id)
                    .onSuccess { _actionMessage.value = "Removed from bookmarks." }
                    .onFailure { _actionMessage.value = it.toUserFriendlyFirestoreMessage("Failed to remove bookmark.") }
            } else {
                firestoreRepo.addBookmark(
                    BookmarkItem(
                        userId = currentUserId,
                        itemType = itemType,
                        targetId = targetId,
                        title = title,
                        subtitle = subtitle,
                        pageNumber = pageNumber
                    )
                ).onSuccess {
                    _actionMessage.value = "Saved to bookmarks."
                }.onFailure {
                    _actionMessage.value = it.toUserFriendlyFirestoreMessage("Failed to save bookmark.")
                }
            }
        }
    }

    fun removeBookmarkById(bookmarkId: String) {
        viewModelScope.launch {
            firestoreRepo.removeBookmark(currentUserId, bookmarkId)
                .onSuccess { _actionMessage.value = "Bookmark removed." }
                .onFailure { _actionMessage.value = it.toUserFriendlyFirestoreMessage("Could not remove bookmark.") }
        }
    }

    fun submitReport(targetType: String, targetId: String, reason: String, details: String) {
        viewModelScope.launch {
            firestoreRepo.submitUserReport(targetType, targetId, reason, details)
                .onSuccess { _actionMessage.value = "Report submitted. Thank you for helping verify accuracy!" }
                .onFailure { _actionMessage.value = it.toUserFriendlyFirestoreMessage("Could not submit report.") }
        }
    }

    fun submitAiLimitPurchaseRequest(
        packageTitle: String,
        creditsAmount: Long,
        priceBdt: Long,
        paymentMethod: String,
        senderNumber: String,
        transactionId: String,
        onSuccess: () -> Unit = {}
    ) {
        val profile = (userProfile.value as? UiState.Success)?.data
        viewModelScope.launch {
            firestoreRepo.submitAiLimitPurchaseRequest(
                AiLimitPurchaseRequest(
                    userId = currentUserId,
                    userName = profile?.displayName.orEmpty(),
                    userEmail = profile?.email.orEmpty(),
                    packageTitle = packageTitle,
                    creditsAmount = creditsAmount,
                    priceBdt = priceBdt,
                    paymentMethod = paymentMethod,
                    senderNumber = senderNumber,
                    transactionId = transactionId
                )
            ).onSuccess {
                _actionMessage.value = "লিমিট কেনার রিকোয়েস্ট জমা হয়েছে! অ্যাডমিন যাচাই করে শীঘ্রই ক্রেডিট যোগ করে দেবেন।"
                onSuccess()
            }.onFailure { err ->
                _actionMessage.value = err.toUserFriendlyFirestoreMessage("লিমিট কেনার রিকোয়েস্ট পাঠানো যায়নি।")
            }
        }
    }

    fun checkForAppUpdates() {
        viewModelScope.launch {
            _updateState.value = UiState.Loading
            try {
                val versionMap = firestoreRepo.getVersionInfoMap()
                val remoteCode = (versionMap["versionCode"] as? Number)?.toInt()
                val remoteName = versionMap["versionName"] as? String
                val remoteNotes = versionMap["releaseNotes"] as? String
                val remoteUrl = versionMap["downloadUrl"] as? String
                val info = UpdateChecker.checkForUpdates(remoteCode, remoteName, remoteNotes, remoteUrl)
                _updateState.value = UiState.Success(info)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _updateState.value = UiState.Error(e.localizedMessage ?: "Failed to check for updates.")
            }
        }
    }

    private fun <T> Flow<T>.asUiState(fallbackError: String): Flow<UiState<T>> =
        this.map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                val root = unwrapFirestoreErrorOrThrow(error, fallbackError)
                emit(UiState.Error(root.toUserFriendlyFirestoreMessage(fallbackError)))
            }

    private fun unwrapFirestoreErrorOrThrow(error: Throwable, contextLabel: String): Throwable {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val root = firestoreError ?: error
        Log.w(TAG, "Stream error ($contextLabel)", root)
        return root
    }

    override fun onCleared() {
        super.onCleared()
        networkMonitor.unregister()
    }
}
