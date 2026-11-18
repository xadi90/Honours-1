package com.example.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.PdfUtils
import com.example.core.toUserFriendlyFirestoreMessage
import com.example.data.model.AiConversation
import com.example.data.model.AiMessage
import com.example.data.model.AiRemoteConfig
import com.example.data.model.AiTutorMode
import com.example.data.model.AnswerLength
import com.example.data.model.GeneratedMcqItem
import com.example.data.model.StructuredAiAnswer
import com.example.data.model.UiState
import com.example.data.repository.AiRepository
import com.example.data.repository.FirestoreRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class AiViewModel(
    private val aiRepo: AiRepository,
    private val firestoreRepo: FirestoreRepository,
    private val currentUserId: String
) : ViewModel() {

    companion object {
        private const val TAG = "AiViewModel"
        private const val STOP_TIMEOUT_MS = 5000L
    }

    val savedAnswers: StateFlow<UiState<List<StructuredAiAnswer>>> =
        firestoreRepo.observeSavedAiAnswers(currentUserId)
            .map<List<StructuredAiAnswer>, UiState<List<StructuredAiAnswer>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                emit(UiState.Error((firestoreError ?: error).toUserFriendlyFirestoreMessage("Failed to load saved AI answers")))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    val conversations: StateFlow<List<AiConversation>> =
        firestoreRepo.observeConversations(currentUserId)
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                Log.w(TAG, "Error loading conversations", firestoreError ?: error)
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeMessages: StateFlow<List<AiMessage>> = _activeConversationId
        .flatMapLatest { convId ->
            if (convId.isNullOrBlank()) flowOf(emptyList())
            else firestoreRepo.observeConversationMessages(currentUserId, convId)
        }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    private val _solveState = MutableStateFlow<UiState<StructuredAiAnswer>>(UiState.Idle)
    val solveState: StateFlow<UiState<StructuredAiAnswer>> = _solveState.asStateFlow()

    private val _selectedAnswer = MutableStateFlow<StructuredAiAnswer?>(null)
    val selectedAnswer: StateFlow<StructuredAiAnswer?> = _selectedAnswer.asStateFlow()

    private val _attachedImages = MutableStateFlow<List<Bitmap>>(emptyList())
    val attachedImages: StateFlow<List<Bitmap>> = _attachedImages.asStateFlow()

    private val _streamingChatText = MutableStateFlow("")
    val streamingChatText: StateFlow<String> = _streamingChatText.asStateFlow()

    private val _isChatStreaming = MutableStateFlow(false)
    val isChatStreaming: StateFlow<Boolean> = _isChatStreaming.asStateFlow()

    private val _chatError = MutableStateFlow<String?>(null)
    val chatError: StateFlow<String?> = _chatError.asStateFlow()

    private val _generatedMcqs = MutableStateFlow<UiState<List<GeneratedMcqItem>>>(UiState.Idle)
    val generatedMcqs: StateFlow<UiState<List<GeneratedMcqItem>>> = _generatedMcqs.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    fun clearStatusBanner() {
        _statusBanner.value = null
    }

    fun attachBitmap(bitmap: Bitmap) {
        _attachedImages.value = (_attachedImages.value + bitmap).take(3)
    }

    fun setAttachedBitmaps(bitmaps: List<Bitmap>) {
        _attachedImages.value = bitmaps.take(3)
    }

    fun clearAttachedImages() {
        _attachedImages.value = emptyList()
    }

    fun selectAnswer(answer: StructuredAiAnswer) {
        _selectedAnswer.value = answer
    }

    fun solveQuestion(
        questionText: String,
        yearLabel: String,
        subjectName: String,
        answerLength: AnswerLength,
        language: String,
        verifiedContext: String,
        remoteConfig: AiRemoteConfig,
        onSolved: (StructuredAiAnswer) -> Unit = {}
    ) {
        if (remoteConfig.maintenanceMode) {
            _solveState.value = UiState.Error(
                remoteConfig.maintenanceMessage.ifBlank { "AI service is currently under scheduled maintenance." }
            )
            return
        }
        val bitmaps = _attachedImages.value
        if (questionText.isBlank() && bitmaps.isEmpty()) {
            _solveState.value = UiState.Error("Please type a question or attach an image/PDF.")
            return
        }

        viewModelScope.launch {
            _solveState.value = UiState.Loading
            val result = aiRepo.solveQuestionStructured(
                questionText = questionText,
                bitmaps = bitmaps,
                yearLabel = yearLabel,
                subjectName = subjectName,
                answerLength = answerLength,
                language = language,
                verifiedContext = verifiedContext,
                remoteConfig = remoteConfig
            )
            result.onSuccess { generated ->
                val saveRes = firestoreRepo.saveAiAnswer(generated)
                val persisted = saveRes.getOrNull()?.let { docId -> generated.copy(id = docId) } ?: generated
                _selectedAnswer.value = persisted
                _solveState.value = UiState.Success(persisted)
                onSolved(persisted)
            }.onFailure { err ->
                _solveState.value = UiState.Error(err.localizedMessage ?: "Failed to generate answer.")
            }
        }
    }

    fun toggleBookmarkOnSelectedAnswer() {
        val current = _selectedAnswer.value ?: return
        val nextVal = !current.bookmarked
        viewModelScope.launch {
            if (current.id.isNotBlank()) {
                firestoreRepo.toggleAnswerBookmark(currentUserId, current.id, nextVal)
                    .onSuccess {
                        _selectedAnswer.value = current.copy(bookmarked = nextVal)
                        _statusBanner.value = if (nextVal) "Answer bookmarked." else "Bookmark removed."
                    }
                    .onFailure {
                        _statusBanner.value = it.toUserFriendlyFirestoreMessage("Unable to update bookmark.")
                    }
            } else {
                firestoreRepo.saveAiAnswer(current.copy(bookmarked = nextVal))
                    .onSuccess { id ->
                        _selectedAnswer.value = current.copy(id = id, bookmarked = nextVal)
                        _statusBanner.value = "Answer saved & bookmarked."
                    }
            }
        }
    }

    fun deleteSavedAnswer(answerId: String) {
        viewModelScope.launch {
            firestoreRepo.deleteSavedAiAnswer(currentUserId, answerId)
                .onSuccess { _statusBanner.value = "Saved answer deleted." }
                .onFailure { _statusBanner.value = it.toUserFriendlyFirestoreMessage("Unable to delete answer.") }
        }
    }

    suspend fun exportSelectedAnswerPdf(context: Context): Result<File> {
        val answer = _selectedAnswer.value
            ?: return Result.failure(IllegalStateException("No answer selected"))
        return try {
            val file = PdfUtils.exportAnswerToPdf(context, answer)
            Result.success(file)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- AI Tutor Multi-Turn Streaming Chat ---
    fun selectConversation(conversationId: String?) {
        _activeConversationId.value = conversationId
        _chatError.value = null
    }

    fun sendTutorMessage(
        promptText: String,
        mode: AiTutorMode,
        yearLabel: String,
        yearNumber: Long,
        subjectName: String,
        remoteConfig: AiRemoteConfig
    ) {
        val cleanPrompt = promptText.trim()
        if (cleanPrompt.isBlank() || _isChatStreaming.value) return
        if (remoteConfig.maintenanceMode) {
            _chatError.value = remoteConfig.maintenanceMessage.ifBlank { "AI Tutor is under maintenance." }
            return
        }

        viewModelScope.launch {
            _chatError.value = null
            _isChatStreaming.value = true
            _streamingChatText.value = ""

            try {
                val convId = _activeConversationId.value ?: run {
                    val created = firestoreRepo.createConversation(
                        title = cleanPrompt.take(55),
                        mode = mode.name,
                        subjectName = subjectName,
                        yearNumber = yearNumber
                    ).getOrThrow()
                    _activeConversationId.value = created
                    created
                }

                val priorHistory = activeMessages.value
                firestoreRepo.appendConversationMessage(convId, "user", cleanPrompt)

                var finalReply = ""
                aiRepo.streamTutorResponse(
                    userPrompt = cleanPrompt,
                    history = priorHistory,
                    mode = mode,
                    yearLabel = yearLabel,
                    subjectName = subjectName,
                    remoteConfig = remoteConfig
                ).collect { accumulated ->
                    finalReply = accumulated
                    _streamingChatText.value = accumulated
                }

                if (finalReply.isNotBlank()) {
                    firestoreRepo.appendConversationMessage(convId, "model", finalReply)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Tutor chat failed", e)
                _chatError.value = e.localizedMessage ?: "AI response failed. Check internet connection."
            } finally {
                _streamingChatText.value = ""
                _isChatStreaming.value = false
            }
        }
    }

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            if (_activeConversationId.value == conversationId) {
                _activeConversationId.value = null
            }
            firestoreRepo.deleteConversation(currentUserId, conversationId)
                .onSuccess { _statusBanner.value = "Conversation deleted." }
                .onFailure { _statusBanner.value = it.toUserFriendlyFirestoreMessage("Failed to delete conversation.") }
        }
    }

    fun generateMcqs(
        topicOrSubject: String,
        yearLabel: String,
        count: Int = 5,
        language: String = "bn",
        remoteConfig: AiRemoteConfig
    ) {
        viewModelScope.launch {
            _generatedMcqs.value = UiState.Loading
            val res = aiRepo.generateMcqsStructured(topicOrSubject, yearLabel, count, language, remoteConfig)
            res.onSuccess { items ->
                _generatedMcqs.value = UiState.Success(items)
            }.onFailure { err ->
                _generatedMcqs.value = UiState.Error(err.localizedMessage ?: "Failed to generate MCQs.")
            }
        }
    }
}
