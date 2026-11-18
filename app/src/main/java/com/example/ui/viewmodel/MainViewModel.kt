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
import com.example.data.model.AiRemoteConfig
import com.example.data.model.Announcement
import com.example.data.model.BookmarkItem
import com.example.data.model.DeveloperProfileConfig
import com.example.data.model.ModelTestResult
import com.example.data.model.UiState
import com.example.data.model.UserProfile
import com.example.data.repository.FirestoreRepository
import com.example.ui.theme.AppThemeMode
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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

    val announcements: StateFlow<List<Announcement>> = firestoreRepo.observePublishedAnnouncements()
        .catch { error ->
            unwrapFirestoreErrorOrThrow(error, "announcements")
            emit(AcademicSeedCatalog.starterAnnouncements)
        }
        .map { list -> list.ifEmpty { AcademicSeedCatalog.starterAnnouncements } }
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

    private val _isAdmin = MutableStateFlow(firestoreRepo.isBootstrappedAdminUser())
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

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
