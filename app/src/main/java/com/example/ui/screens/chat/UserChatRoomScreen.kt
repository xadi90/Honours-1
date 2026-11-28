package com.example.ui.screens.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.CollegeImage
import com.example.data.model.ChatUserProfile
import com.example.data.model.UserChatMessage
import com.example.data.model.UserChatThread
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val RoomHeaderLight = Color(0xFF075E54)
private val RoomHeaderDark = Color(0xFF1F2C34)
private val RoomBgLight = Color(0xFFEFEAE2)
private val RoomBgDark = Color(0xFF0B141A)
private val BubbleOutLight = Color(0xFFD9FDD3)
private val BubbleOutDark = Color(0xFF005C4B)
private val BubbleInLight = Color(0xFFFFFFFF)
private val BubbleInDark = Color(0xFF1F2C34)
private val TickBlue = Color(0xFF34B7F1)
private val SendGreen = Color(0xFF00A884)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserChatRoomScreen(
    currentUserId: String,
    thread: UserChatThread?,
    partnerProfile: ChatUserProfile?,
    messages: List<UserChatMessage>,
    replyingTo: UserChatMessage?,
    pendingImageDataUri: String?,
    isProcessingImage: Boolean,
    isSending: Boolean,
    onBackClick: () -> Unit,
    onComposerTextChanged: (String) -> Unit,
    onAttachPhotoUri: (Uri) -> Unit,
    onClearPendingPhoto: () -> Unit,
    onSelectReply: (UserChatMessage?) -> Unit,
    onSendMessage: (String) -> Unit,
    onMarkRead: () -> Unit,
    onDeleteMessageForAll: (String) -> Unit,
    onToggleBlock: () -> Unit,
    onReportUser: (reason: String, details: String) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val headerColor = if (isDark) RoomHeaderDark else RoomHeaderLight
    val chatBgColor = if (isDark) RoomBgDark else RoomBgLight

    val partnerUid = thread?.partnerUid(currentUserId).orEmpty()
    val partnerName = partnerProfile?.displayName?.takeIf { it.isNotBlank() }
        ?: thread?.partnerName(currentUserId).orEmpty().ifBlank { "NU Student" }
    val partnerPhoto = partnerProfile?.photoUrl?.takeIf { it.isNotBlank() }
        ?: thread?.partnerPhoto(currentUserId).orEmpty()
    val isPartnerOnline = partnerProfile?.isOnline == true
    val isPartnerTyping = thread?.isPartnerTyping(currentUserId) == true

    val blockedBy = thread?.blockedBy.orEmpty()
    val isBlockedByMe = currentUserId in blockedBy
    val isAnyBlocked = blockedBy.isNotEmpty()

    var messageText by remember { mutableStateOf("") }
    var menuExpanded by remember { mutableStateOf(false) }
    var selectedMessageForMenu by remember { mutableStateOf<UserChatMessage?>(null) }
    var zoomedImageSource by remember { mutableStateOf<String?>(null) }
    var showReportDialog by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    val listState = rememberLazyListState()

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            onAttachPhotoUri(uri)
        }
    }

    LaunchedEffect(messages.size, thread?.id) {
        onMarkRead()
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ChatAvatar(
                            name = partnerName,
                            photoUrl = partnerPhoto,
                            isOnline = isPartnerOnline,
                            sizeDp = 38
                        )
                        Column {
                            Text(
                                text = partnerName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.testTag("chat_room_partner_name")
                            )
                            Text(
                                text = when {
                                    isPartnerTyping -> "টাইপ করছে..."
                                    isPartnerOnline -> "অনলাইন"
                                    else -> formatLastSeen(partnerProfile?.lastSeenAt)
                                },
                                color = if (isPartnerTyping) Color(0xFF69F0AE) else Color.White.copy(alpha = 0.82f),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isPartnerTyping) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.testTag("chat_room_status_text")
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("chat_room_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ফিরে যান",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.testTag("chat_room_menu_button")
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "চ্যাট মেনু",
                                tint = Color.White
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (isBlockedByMe) "আনব্লক করুন" else "ব্লক করুন") },
                                leadingIcon = { Icon(Icons.Default.Block, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onToggleBlock()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("রিপোর্ট করুন") },
                                leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    showReportDialog = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = headerColor)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(chatBgColor)
                .imePadding()
        ) {
            if (isAnyBlocked) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chat_blocked_banner")
                ) {
                    Text(
                        text = if (isBlockedByMe) {
                            "আপনি এই ব্যবহারকারীকে ব্লক করেছেন। বার্তা পাঠাতে আনব্লক করুন।"
                        } else {
                            "এই কথোপকথনে বার্তা পাঠানো আপাতত বন্ধ রয়েছে।"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (messages.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                            shape = RoundedCornerShape(14.dp),
                            shadowElevation = 1.dp
                        ) {
                            Text(
                                text = "👋 $partnerName-কে হাই বলুন এবং পড়াশোনা বা নোটস নিয়ে আলোচনা শুরু করুন!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 820.dp)
                            .align(Alignment.Center)
                            .testTag("chat_messages_list"),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemsIndexed(messages, key = { idx, msg -> msg.id.ifBlank { "msg_$idx" } }) { index, msg ->
                            val currentDay = dayKey(msg.createdAt)
                            val prevDay = if (index > 0) dayKey(messages[index - 1].createdAt) else ""
                            if (currentDay.isNotEmpty() && currentDay != prevDay) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        color = if (isDark) Color(0xFF182229) else Color(0xFFE1F3FB),
                                        shape = RoundedCornerShape(10.dp),
                                        shadowElevation = 0.5.dp
                                    ) {
                                        Text(
                                            text = currentDay,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            val isMine = msg.senderId == currentUserId
                            val isReadByPartner = partnerUid.isNotBlank() && partnerUid in msg.readBy
                            ChatBubbleRow(
                                message = msg,
                                isMine = isMine,
                                isReadByPartner = isReadByPartner,
                                isDark = isDark,
                                onBubbleLongOrTap = {
                                    if (!msg.deletedForAll) selectedMessageForMenu = msg
                                },
                                onImageClick = { src -> zoomedImageSource = src }
                            )
                        }
                    }
                }
            }

            // Reply Quote Preview
            if (replyingTo != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chat_reply_preview")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(36.dp)
                                .background(SendGreen, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = replyingTo.senderName.ifBlank { "উত্তর" },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = SendGreen
                            )
                            Text(
                                text = replyingTo.text.ifBlank { "📷 ছবি" },
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onSelectReply(null) }) {
                            Icon(Icons.Default.Close, contentDescription = "বাতিল করুন")
                        }
                    }
                }
            }

            // Pending Photo Preview
            if (!pendingImageDataUri.isNullOrBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chat_pending_image_preview")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CollegeImage(
                            imageSource = pendingImageDataUri,
                            contentDescription = "পাঠানোর জন্য নির্বাচিত ছবি",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Text(
                            text = "ছবি পাঠানো হবে (সাথে বার্তা লিখতে পারেন)",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onClearPendingPhoto) {
                            Icon(Icons.Default.Close, contentDescription = "ছবি সরান")
                        }
                    }
                }
            }

            // WhatsApp-style Bottom Composer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 820.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = if (isDark) Color(0xFF1F2C34) else Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                photoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            enabled = !isProcessingImage && !isAnyBlocked,
                            modifier = Modifier.testTag("chat_attach_image_button")
                        ) {
                            if (isProcessingImage) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    Icons.Default.AddPhotoAlternate,
                                    contentDescription = "ছবি যোগ করুন",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            if (messageText.isEmpty()) {
                                Text(
                                    text = if (isAnyBlocked) "বার্তা পাঠানো বন্ধ" else "বার্তা লিখুন...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            BasicTextField(
                                value = messageText,
                                onValueChange = { updated ->
                                    messageText = updated
                                    onComposerTextChanged(updated)
                                },
                                enabled = !isAnyBlocked,
                                maxLines = 5,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (isDark) Color.White else Color(0xFF111B21)
                                ),
                                cursorBrush = SolidColor(SendGreen),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("chat_message_input")
                            )
                        }
                    }
                }

                val canSend = !isAnyBlocked && !isSending && !isProcessingImage &&
                    (messageText.isNotBlank() || !pendingImageDataUri.isNullOrBlank())

                Surface(
                    onClick = {
                        if (canSend) {
                            val toSend = messageText
                            messageText = ""
                            onSendMessage(toSend)
                        }
                    },
                    enabled = canSend,
                    shape = CircleShape,
                    color = if (canSend) SendGreen else SendGreen.copy(alpha = 0.45f),
                    contentColor = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("chat_send_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "পাঠান",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    selectedMessageForMenu?.let { target ->
        val isMine = target.senderId == currentUserId
        AlertDialog(
            onDismissRequest = { selectedMessageForMenu = null },
            title = { Text("বার্তা অপশন", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = {
                            selectedMessageForMenu = null
                            onSelectReply(target)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = null)
                            Text("উত্তর দিন (Reply)")
                        }
                    }
                    if (target.text.isNotBlank()) {
                        TextButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(target.text))
                                selectedMessageForMenu = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                                Text("কপি করুন (Copy)")
                            }
                        }
                    }
                    if (isMine && target.id.isNotBlank()) {
                        TextButton(
                            onClick = {
                                selectedMessageForMenu = null
                                onDeleteMessageForAll(target.id)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    "সবার জন্য মুছে ফেলুন",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedMessageForMenu = null }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }

    zoomedImageSource?.let { imgSrc ->
        Dialog(
            onDismissRequest = { zoomedImageSource = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.94f))
                    .clickable { zoomedImageSource = null },
                contentAlignment = Alignment.Center
            ) {
                CollegeImage(
                    imageSource = imgSrc,
                    contentDescription = "পূর্ণ ছবি",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
                IconButton(
                    onClick = { zoomedImageSource = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন", tint = Color.White)
                }
            }
        }
    }

    if (showReportDialog) {
        var reportReason by remember { mutableStateOf("অশোভন বা অপ্রাসঙ্গিক আচরণ") }
        var reportDetails by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("ব্যবহারকারী রিপোর্ট করুন", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        label = { Text("কারণ") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reportDetails,
                        onValueChange = { reportDetails = it },
                        label = { Text("বিস্তারিত বিবরণ (ঐচ্ছিক)") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReportUser(reportReason, reportDetails)
                        showReportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("রিপোর্ট জমা দিন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatBubbleRow(
    message: UserChatMessage,
    isMine: Boolean,
    isReadByPartner: Boolean,
    isDark: Boolean,
    onBubbleLongOrTap: () -> Unit,
    onImageClick: (String) -> Unit
) {
    val bubbleColor = when {
        isMine && isDark -> BubbleOutDark
        isMine -> BubbleOutLight
        isDark -> BubbleInDark
        else -> BubbleInLight
    }
    val textColor = if (isDark) Color(0xFFE9EDEF) else Color(0xFF111B21)
    val metaColor = if (isDark) Color(0xFF9FB0B9) else Color(0xFF667781)
    val bubbleShape = if (isMine) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = bubbleColor,
            shape = bubbleShape,
            shadowElevation = 1.dp,
            modifier = Modifier
                .widthIn(min = 90.dp, max = 300.dp)
                .clip(bubbleShape)
                .combinedClickable(
                    onClick = onBubbleLongOrTap,
                    onLongClick = onBubbleLongOrTap
                )
        ) {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                if (message.replyToPreview.isNotBlank() && !message.deletedForAll) {
                    Surface(
                        color = Color.Black.copy(alpha = if (isDark) 0.25f else 0.06f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 5.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(28.dp)
                                    .background(SendGreen, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                if (message.replyToSenderName.isNotBlank()) {
                                    Text(
                                        text = message.replyToSenderName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SendGreen,
                                        maxLines = 1
                                    )
                                }
                                Text(
                                    text = message.replyToPreview,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = metaColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                if (message.deletedForAll) {
                    Text(
                        text = "🚫 এই বার্তাটি মুছে ফেলা হয়েছে",
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                        color = metaColor
                    )
                } else {
                    if (message.imageUrl.isNotBlank()) {
                        CollegeImage(
                            imageSource = message.imageUrl,
                            contentDescription = "চ্যাট ছবি",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onImageClick(message.imageUrl) }
                        )
                        if (message.text.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    if (message.text.isNotBlank()) {
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = formatBubbleTime(message.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = metaColor
                    )
                    if (isMine && !message.deletedForAll) {
                        Icon(
                            imageVector = if (isReadByPartner) Icons.Default.DoneAll else Icons.Default.Done,
                            contentDescription = if (isReadByPartner) "পড়া হয়েছে" else "পাঠানো হয়েছে",
                            tint = if (isReadByPartner) TickBlue else metaColor,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatBubbleTime(timestamp: Timestamp?): String {
    val date = timestamp?.toDate() ?: return "এখনই"
    return SimpleDateFormat("h:mm a", Locale.ENGLISH).format(date)
}

private fun dayKey(timestamp: Timestamp?): String {
    val date = timestamp?.toDate() ?: return ""
    val msgCal = Calendar.getInstance().apply { time = date }
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    return when {
        msgCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            msgCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "আজ"
        msgCal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
            msgCal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> "গতকাল"
        else -> SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(date)
    }
}
