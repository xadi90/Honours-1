package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.encodePickedImageToDataUri
import com.example.core.toUserFriendlyFirestoreMessage
import com.example.data.model.ChatUserProfile
import com.example.data.model.UserChatMessage
import com.example.data.model.UserChatThread
import com.example.data.model.UserProfile
import com.example.data.repository.FirestoreChatRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel(
    private val chatRepo: FirestoreChatRepository,
    val currentUserId: String
) : ViewModel() {

    companion object {
        private const val TAG = "ChatViewModel"
        private const val STOP_TIMEOUT_MS = 5000L
        private const val TYPING_IDLE_TIMEOUT_MS = 3500L
    }

    val directoryUsers: StateFlow<List<ChatUserProfile>> = chatRepo.observeDirectoryUsers()
        .catch { err ->
            if (err is CancellationException) throw err
            Log.w(TAG, "observeDirectoryUsers error", err)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val myChats: StateFlow<List<UserChatThread>> = chatRepo.observeMyChats()
        .catch { err ->
            if (err is CancellationException) throw err
            Log.w(TAG, "observeMyChats error", err)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val totalUnreadCount: StateFlow<Int> = myChats
        .map { list -> list.sumOf { it.myUnreadCount(currentUserId).toInt() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), 0)

    private val _activeChatId = MutableStateFlow<String?>(null)
    val activeChatId: StateFlow<String?> = _activeChatId.asStateFlow()

    val activeThread: StateFlow<UserChatThread?> = _activeChatId
        .flatMapLatest { chatId ->
            if (chatId.isNullOrBlank()) flowOf(null)
            else chatRepo.observeChatThread(chatId)
                .catch { err ->
                    if (err is CancellationException) throw err
                    Log.w(TAG, "observeChatThread error", err)
                    emit(null)
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    val activeMessages: StateFlow<List<UserChatMessage>> = _activeChatId
        .flatMapLatest { chatId ->
            if (chatId.isNullOrBlank()) flowOf(emptyList())
            else chatRepo.observeMessages(chatId)
                .catch { err ->
                    if (err is CancellationException) throw err
                    Log.w(TAG, "observeMessages error", err)
                    emit(emptyList())
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    private val _replyingTo = MutableStateFlow<UserChatMessage?>(null)
    val replyingTo: StateFlow<UserChatMessage?> = _replyingTo.asStateFlow()

    private val _pendingImageDataUri = MutableStateFlow<String?>(null)
    val pendingImageDataUri: StateFlow<String?> = _pendingImageDataUri.asStateFlow()

    private val _isProcessingImage = MutableStateFlow(false)
    val isProcessingImage: StateFlow<Boolean> = _isProcessingImage.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _isOpeningChat = MutableStateFlow(false)
    val isOpeningChat: StateFlow<Boolean> = _isOpeningChat.asStateFlow()

    private val _chatToast = MutableStateFlow<String?>(null)
    val chatToast: StateFlow<String?> = _chatToast.asStateFlow()

    private var typingDebounceJob: Job? = null
    private var lastTypingSentAtMs: Long = 0L

    fun clearChatToast() {
        _chatToast.value = null
    }

    fun syncMyProfile(profile: UserProfile?) {
        viewModelScope.launch {
            chatRepo.syncMyChatProfile(profile)
        }
    }

    fun setOnlineStatus(isOnline: Boolean) {
        viewModelScope.launch {
            chatRepo.setOnlineStatus(isOnline)
        }
    }

    fun openChatById(chatId: String) {
        _activeChatId.value = chatId
        _replyingTo.value = null
        _pendingImageDataUri.value = null
    }

    fun closeActiveChat() {
        val currentId = _activeChatId.value
        typingDebounceJob?.cancel()
        if (!currentId.isNullOrBlank()) {
            viewModelScope.launch { chatRepo.setTypingState(currentId, false) }
        }
        _replyingTo.value = null
        _pendingImageDataUri.value = null
        _activeChatId.value = null
    }

    fun openChatWithUser(
        partner: ChatUserProfile,
        myProfile: UserProfile?,
        onOpened: (String) -> Unit
    ) {
        if (_isOpeningChat.value) return
        viewModelScope.launch {
            _isOpeningChat.value = true
            chatRepo.openOrCreateDirectChat(partner, myProfile)
                .onSuccess { chatId ->
                    _activeChatId.value = chatId
                    _replyingTo.value = null
                    _pendingImageDataUri.value = null
                    onOpened(chatId)
                }
                .onFailure { err ->
                    _chatToast.value = err.toUserFriendlyFirestoreMessage("চ্যাট খোলা যায়নি।")
                }
            _isOpeningChat.value = false
        }
    }

    fun setReplyingTo(message: UserChatMessage?) {
        _replyingTo.value = message
    }

    fun clearPendingImage() {
        _pendingImageDataUri.value = null
    }

    fun attachPhotoUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            _isProcessingImage.value = true
            encodePickedImageToDataUri(context, uri)
                .onSuccess { encoded ->
                    _pendingImageDataUri.value = encoded
                }
                .onFailure { err ->
                    _chatToast.value = err.localizedMessage ?: "ছবিটি পড়া যায়নি।"
                }
            _isProcessingImage.value = false
        }
    }

    fun onComposerTextChanged(text: String) {
        val chatId = _activeChatId.value ?: return
        val thread = activeThread.value
        if (thread != null && thread.blockedBy.isNotEmpty()) return

        typingDebounceJob?.cancel()
        if (text.isBlank()) {
            lastTypingSentAtMs = 0L
            viewModelScope.launch { chatRepo.setTypingState(chatId, false) }
            return
        }

        val now = System.currentTimeMillis()
        if (now - lastTypingSentAtMs >= 2500L) {
            lastTypingSentAtMs = now
            viewModelScope.launch { chatRepo.setTypingState(chatId, true) }
        }
        typingDebounceJob = viewModelScope.launch {
            delay(TYPING_IDLE_TIMEOUT_MS)
            lastTypingSentAtMs = 0L
            chatRepo.setTypingState(chatId, false)
        }
    }

    fun sendMessage(
        senderName: String,
        text: String,
        onSentSuccess: () -> Unit = {}
    ) {
        val chatId = _activeChatId.value ?: return
        val thread = activeThread.value ?: return
        if (thread.blockedBy.isNotEmpty()) {
            _chatToast.value = "এই চ্যাটে ব্লক থাকায় বার্তা পাঠানো যাবে না।"
            return
        }
        val image = _pendingImageDataUri.value.orEmpty()
        val cleanText = text.trim()
        if (cleanText.isEmpty() && image.isEmpty()) return
        if (_isSending.value) return

        val partnerUid = thread.partnerUid(currentUserId)
        val replyTarget = _replyingTo.value

        viewModelScope.launch {
            _isSending.value = true
            typingDebounceJob?.cancel()
            lastTypingSentAtMs = 0L
            chatRepo.sendMessage(
                chatId = chatId,
                partnerUid = partnerUid,
                senderName = senderName,
                text = cleanText,
                imageUrl = image,
                replyTo = replyTarget
            ).onSuccess {
                _replyingTo.value = null
                _pendingImageDataUri.value = null
                onSentSuccess()
            }.onFailure { err ->
                _chatToast.value = err.toUserFriendlyFirestoreMessage("বার্তা পাঠানো যায়নি।")
            }
            _isSending.value = false
        }
    }

    fun markActiveChatRead() {
        val chatId = _activeChatId.value ?: return
        val thread = activeThread.value ?: return
        val messages = activeMessages.value
        viewModelScope.launch {
            chatRepo.markMessagesAsRead(
                chatId = chatId,
                messages = messages,
                currentThreadUnread = thread.myUnreadCount(currentUserId)
            )
        }
    }

    fun deleteMessageForEveryone(messageId: String) {
        val chatId = _activeChatId.value ?: return
        viewModelScope.launch {
            chatRepo.deleteMessageForEveryone(chatId, messageId)
                .onSuccess { _chatToast.value = "বার্তা সবার জন্য মুছে ফেলা হয়েছে।" }
                .onFailure { err -> _chatToast.value = err.toUserFriendlyFirestoreMessage("বার্তা মোছা যায়নি।") }
        }
    }

    fun toggleBlockPartner() {
        val chatId = _activeChatId.value ?: return
        val thread = activeThread.value ?: return
        val blockedByMe = currentUserId in thread.blockedBy
        viewModelScope.launch {
            chatRepo.toggleBlockUser(chatId, blockedByMe)
                .onSuccess {
                    _chatToast.value = if (blockedByMe) {
                        "আনব্লক করা হয়েছে।"
                    } else {
                        "ব্যবহারকারীকে ব্লক করা হয়েছে।"
                    }
                }
                .onFailure { err ->
                    _chatToast.value = err.toUserFriendlyFirestoreMessage("আপডেট করা যায়নি।")
                }
        }
    }

    fun reportPartner(reason: String, details: String) {
        val thread = activeThread.value ?: return
        val partnerUid = thread.partnerUid(currentUserId)
        val partnerName = thread.partnerName(currentUserId)
        viewModelScope.launch {
            chatRepo.reportChatUser(partnerUid, partnerName, reason, details)
                .onSuccess {
                    _chatToast.value = "রিপোর্ট অ্যাডমিনের কাছে পাঠানো হয়েছে।"
                }
                .onFailure { err ->
                    _chatToast.value = err.toUserFriendlyFirestoreMessage("রিপোর্ট পাঠানো যায়নি।")
                }
        }
    }
}
