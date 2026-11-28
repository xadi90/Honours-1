package com.example.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.PdfDownloadState
import com.example.core.PdfUtils
import com.example.core.PreferencesManager
import com.example.core.toUserFriendlyFirestoreMessage
import com.example.data.catalog.AcademicSeedCatalog
import com.example.data.model.CatalogMetaConfig
import com.example.data.model.Chapter
import com.example.data.model.Department
import com.example.data.model.ModelTest
import com.example.data.model.ModelTestResult
import com.example.data.model.PdfResource
import com.example.data.model.StudyQuestion
import com.example.data.model.Subject
import com.example.data.model.UiState
import com.example.data.repository.FirestoreRepository
import com.example.data.repository.mergeCatalogItems
import com.example.data.repository.observeCatalogMeta
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class AcademicViewModel(
    private val firestoreRepo: FirestoreRepository,
    private val prefs: PreferencesManager,
    private val appContext: Context
) : ViewModel() {

    companion object {
        private const val TAG = "AcademicViewModel"
        private const val STOP_TIMEOUT_MS = 5000L
    }

    private val catalogMetaFlow: Flow<CatalogMetaConfig> = firestoreRepo.observeCatalogMeta()
        .catch { err ->
            val firestoreErr = generateSequence(err) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreErr == null && err is CancellationException) throw err
            emit(CatalogMetaConfig())
        }

    val departments: StateFlow<UiState<List<Department>>> = combine(
        firestoreRepo.observePublishedDepartments(),
        catalogMetaFlow
    ) { live, meta ->
        mergeCatalogItems(live, AcademicSeedCatalog.defaultDepartments, meta, includeDrafts = false) { it.id }
            .sortedBy { it.sortOrder }
    }
        .asUiState("Failed to load departments")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    val subjects: StateFlow<UiState<List<Subject>>> = combine(
        firestoreRepo.observePublishedSubjects(),
        catalogMetaFlow
    ) { live, meta ->
        mergeCatalogItems(live, AcademicSeedCatalog.starterSubjects, meta, includeDrafts = false) { it.id }
            .sortedWith(compareBy({ it.yearNumber }, { it.courseCode }))
    }
        .asUiState("Failed to load subjects")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    val chapters: StateFlow<UiState<List<Chapter>>> = combine(
        firestoreRepo.observePublishedChapters(),
        catalogMetaFlow
    ) { live, meta ->
        mergeCatalogItems(live, AcademicSeedCatalog.starterChapters, meta, includeDrafts = false) { it.id }
            .sortedBy { it.chapterNumber }
    }
        .asUiState("Failed to load chapters")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    val pdfs: StateFlow<UiState<List<PdfResource>>> = combine(
        firestoreRepo.observePublishedPdfs(),
        catalogMetaFlow
    ) { live, meta ->
        mergeCatalogItems(live, AcademicSeedCatalog.starterPdfs, meta, includeDrafts = false) { it.id }
    }
        .asUiState("Failed to load PDF library")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    val questions: StateFlow<UiState<List<StudyQuestion>>> = combine(
        firestoreRepo.observePublishedQuestions(),
        catalogMetaFlow
    ) { live, meta ->
        mergeCatalogItems(live, AcademicSeedCatalog.starterQuestions, meta, includeDrafts = false) { it.id }
    }
        .asUiState("Failed to load questions")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    val modelTests: StateFlow<UiState<List<ModelTest>>> = firestoreRepo.observePublishedModelTests()
        .map { list -> list.ifEmpty { AcademicSeedCatalog.starterModelTests } }
        .asUiState("Failed to load model tests")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    private val _downloadStates = MutableStateFlow<Map<String, PdfDownloadState>>(emptyMap())
    val downloadStates: StateFlow<Map<String, PdfDownloadState>> = _downloadStates.asStateFlow()

    private val downloadJobs = mutableMapOf<String, Job>()

    private val _offlineRefreshTick = MutableStateFlow(0)
    val offlineRefreshTick: StateFlow<Int> = _offlineRefreshTick.asStateFlow()

    fun isPdfDownloaded(pdf: PdfResource): Boolean {
        val file = PdfUtils.getOfflinePdfFile(appContext, pdf.id, pdf.localFileName)
        return file.exists() && file.length() > 0L
    }

    suspend fun ensureLocalPdfReady(pdf: PdfResource): Result<File> {
        val destFile = PdfUtils.getOfflinePdfFile(appContext, pdf.id, pdf.localFileName)
        if (destFile.exists() && destFile.length() > 0L) {
            return Result.success(destFile)
        }
        // If remote URL exists, download it
        if (pdf.pdfUrl.startsWith("https://")) {
            var finalResult: Result<File> = Result.failure(IllegalStateException("Download did not complete"))
            PdfUtils.downloadPdfWithProgress(pdf.pdfUrl, destFile).collect { state ->
                _downloadStates.update { it + (pdf.id to state) }
                if (state is PdfDownloadState.Completed) {
                    _offlineRefreshTick.update { it + 1 }
                    finalResult = Result.success(state.file)
                } else if (state is PdfDownloadState.Failed) {
                    finalResult = Result.failure(IllegalStateException(state.message))
                }
            }
            return finalResult
        }
        // Starter or metadata-only PDF -> materialize clean offline study guide PDF so PdfRenderer always opens cleanly
        val body = buildString {
            appendLine("National University (NU) Honours Academic Resource")
            appendLine("Department: ${pdf.departmentName.ifBlank { pdf.departmentId }}")
            appendLine("Honours Year: Year ${pdf.yearNumber}")
            appendLine("Subject / Paper: ${pdf.subjectName} (${pdf.courseCode.ifBlank { "NU Core" }})")
            appendLine("Category: ${pdf.category}")
            appendLine()
            appendLine("1. Course & Resource Overview")
            appendLine(pdf.description.ifBlank { "National University Honours study guide managed via Honours Solution." })
            appendLine()
            appendLine("2. NU Honours Exam Structure & Preparation Strategy")
            appendLine("- Part A (Brief Questions - 10 marks): Memorize definitions, key scholars, dates, and constitutional/core concepts.")
            appendLine("- Part B (Short Questions - 20 marks): Write structured 1.5-2 page answers with clear headings and examples.")
            appendLine("- Part C (Broad Essay Questions - 50 marks): Include Bhumika, Theorists/Definitions, Core Analysis, Critical Evaluation, and Uposhonghar.")
            appendLine()
            appendLine("3. Smart Study Workflow in Honours Solution")
            appendLine("- Open 'AI প্রশ্ন সমাধান' and select Year ${pdf.yearNumber} (${pdf.subjectName}) to generate exam-ready Bangla or English answers.")
            appendLine("- Practice timed MCQ sets & review previous-year NU board questions before exams.")
            appendLine("- Bookmark key pages here for instant offline revision.")
            appendLine()
            appendLine("Live Sync Notice: Whenever Admin uploads or updates the full PDF in Content Management, your app updates automatically.")
        }
        val generated = PdfUtils.createStudyGuidePdf(
            context = appContext,
            fileName = destFile.name,
            title = pdf.title,
            subtitle = "${pdf.departmentName} • Year ${pdf.yearNumber} • ${pdf.category}",
            body = body
        )
        _offlineRefreshTick.update { it + 1 }
        return Result.success(generated)
    }

    fun startPdfDownload(pdf: PdfResource) {
        if (downloadJobs[pdf.id]?.isActive == true) return
        val job = viewModelScope.launch {
            _downloadStates.update { it + (pdf.id to PdfDownloadState.Downloading(0, 0L, pdf.fileSizeBytes)) }
            val result = ensureLocalPdfReady(pdf)
            result.onSuccess { file ->
                _downloadStates.update { it + (pdf.id to PdfDownloadState.Completed(file)) }
            }.onFailure { err ->
                _downloadStates.update { it + (pdf.id to PdfDownloadState.Failed(err.localizedMessage ?: "Download failed")) }
            }
        }
        downloadJobs[pdf.id] = job
    }

    fun cancelPdfDownload(pdfId: String) {
        downloadJobs[pdfId]?.cancel()
        downloadJobs.remove(pdfId)
        _downloadStates.update { it - pdfId }
    }

    fun deleteOfflinePdf(pdf: PdfResource) {
        val file = PdfUtils.getOfflinePdfFile(appContext, pdf.id, pdf.localFileName)
        if (file.exists()) file.delete()
        _downloadStates.update { it - pdf.id }
        _offlineRefreshTick.update { it + 1 }
    }

    suspend fun getSavedPageProgress(pdfId: String): Int {
        return prefs.getPdfReadingProgress(pdfId).first()
    }

    fun savePageProgress(pdfId: String, pageIndex: Int) {
        viewModelScope.launch {
            prefs.savePdfReadingProgress(pdfId, pageIndex)
        }
    }

    fun recordModelTestResult(result: ModelTestResult) {
        viewModelScope.launch {
            firestoreRepo.saveModelTestResult(result)
        }
    }

    private fun <T> Flow<T>.asUiState(fallbackError: String): Flow<UiState<T>> =
        this.map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val root = firestoreError ?: error
                Log.w(TAG, "Stream error ($fallbackError)", root)
                emit(UiState.Error(root.toUserFriendlyFirestoreMessage(fallbackError)))
            }
}
