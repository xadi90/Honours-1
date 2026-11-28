package com.example.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.CollegeImage
import com.example.data.model.ChatUserProfile
import com.example.data.model.UserChatThread
import com.example.ui.components.EmptyStateView
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val WhatsAppHeaderTeal = Color(0xFF075E54)
private val WhatsAppAccentGreen = Color(0xFF25D366)
private val WhatsAppDarkHeader = Color(0xFF1F2C34)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserChatListScreen(
    currentUserId: String,
    chats: List<UserChatThread>,
    directoryUsers: List<ChatUserProfile>,
    isOpeningChat: Boolean,
    onBackClick: () -> Unit,
    onOpenThread: (UserChatThread) -> Unit,
    onStartChatWithUser: (ChatUserProfile) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val headerColor = if (isDark) WhatsAppDarkHeader else WhatsAppHeaderTeal

    val onlineMap = remember(directoryUsers) {
        directoryUsers.associateBy { it.userId }
    }
    val onlineCount = remember(directoryUsers) { directoryUsers.count { it.isOnline } }
    val totalUnread = remember(chats, currentUserId) {
        chats.sumOf { it.myUnreadCount(currentUserId).toInt() }
    }

    val cleanQuery = searchQuery.trim().lowercase()
    val filteredChats = remember(chats, cleanQuery, currentUserId) {
        if (cleanQuery.isEmpty()) chats
        else chats.filter { thread ->
            thread.partnerName(currentUserId).lowercase().contains(cleanQuery) ||
                thread.partnerDept(currentUserId).lowercase().contains(cleanQuery) ||
                thread.lastMessageText.lowercase().contains(cleanQuery)
        }
    }
    val filteredUsers = remember(directoryUsers, cleanQuery) {
        if (cleanQuery.isEmpty()) directoryUsers
        else directoryUsers.filter { user ->
            user.displayName.lowercase().contains(cleanQuery) ||
                user.departmentName.lowercase().contains(cleanQuery) ||
                "year ${user.yearNumber}".contains(cleanQuery)
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(headerColor)) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "শিক্ষার্থী চ্যাট",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = if (onlineCount > 0) {
                                    "লাইভ মেসেজিং • $onlineCount জন এখন অনলাইন"
                                } else {
                                    "সহপাঠীদের সাথে সরাসরি চ্যাট"
                                },
                                color = Color.White.copy(alpha = 0.84f),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.testTag("chat_list_back_button")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "ফিরে যান",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = headerColor)
                )

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = headerColor,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = WhatsAppAccentGreen,
                                height = 3.dp
                            )
                        }
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.testTag("chat_tab_chats"),
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("চ্যাট সমূহ", fontWeight = FontWeight.SemiBold)
                                if (totalUnread > 0) {
                                    Badge(
                                        containerColor = WhatsAppAccentGreen,
                                        contentColor = Color(0xFF062E18)
                                    ) {
                                        Text(
                                            text = if (totalUnread > 99) "99+" else "$totalUnread",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.testTag("chat_tab_directory"),
                        text = {
                            Text(
                                text = "সব শিক্ষার্থী (${directoryUsers.size})",
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { selectedTab = 1 },
                    containerColor = WhatsAppAccentGreen,
                    contentColor = Color(0xFF062E18),
                    modifier = Modifier.testTag("chat_new_fab")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "নতুন চ্যাট শুরু করুন")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 760.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("নাম, বিভাগ বা মেসেজ খুঁজুন...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "মুছুন")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chat_search_input")
                )

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = WhatsAppHeaderTeal,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "আপনার ইমেইল ও ব্যক্তিগত তথ্য সম্পূর্ণ গোপন রাখা হয়েছে",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (selectedTab == 0) {
                if (filteredChats.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        EmptyStateView(
                            icon = Icons.AutoMirrored.Filled.Chat,
                            title = "এখনো কোনো কথোপকথন নেই",
                            message = "'সব শিক্ষার্থী' ট্যাব থেকে যেকোনো সহপাঠীকে বেছে নিয়ে সরাসরি হোয়াটসঅ্যাপের মতো চ্যাট শুরু করুন।",
                            actionLabel = "সহপাঠী খুঁজুন",
                            onActionClick = { selectedTab = 1 },
                            modifier = Modifier.widthIn(max = 540.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 760.dp)
                            .align(Alignment.CenterHorizontally)
                            .testTag("chat_threads_list"),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        items(filteredChats, key = { it.id }) { thread ->
                            val partnerProfile = onlineMap[thread.partnerUid(currentUserId)]
                            ChatThreadListItem(
                                currentUserId = currentUserId,
                                thread = thread,
                                isPartnerOnline = partnerProfile?.isOnline == true,
                                onClick = { onOpenThread(thread) }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 78.dp, end = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            } else {
                if (filteredUsers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        EmptyStateView(
                            icon = Icons.Default.PersonSearch,
                            title = "কোনো শিক্ষার্থী পাওয়া যায়নি",
                            message = "অন্য শিক্ষার্থীরা অ্যাপে প্রবেশ করলে এখানে তাঁদের নাম দেখা যাবে।",
                            modifier = Modifier.widthIn(max = 540.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 760.dp)
                            .align(Alignment.CenterHorizontally)
                            .testTag("chat_directory_list"),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(filteredUsers, key = { it.userId }) { user ->
                            ChatDirectoryUserRow(
                                user = user,
                                enabled = !isOpeningChat,
                                onClick = { onStartChatWithUser(user) }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 78.dp, end = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatThreadListItem(
    currentUserId: String,
    thread: UserChatThread,
    isPartnerOnline: Boolean,
    onClick: () -> Unit
) {
    val partnerName = thread.partnerName(currentUserId)
    val partnerPhoto = thread.partnerPhoto(currentUserId)
    val unreadCount = thread.myUnreadCount(currentUserId)
    val isTyping = thread.isPartnerTyping(currentUserId)
    val isMineLast = thread.lastMessageSenderId == currentUserId

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("chat_thread_${thread.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ChatAvatar(
            name = partnerName,
            photoUrl = partnerPhoto,
            isOnline = isPartnerOnline,
            sizeDp = 52
        )

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = partnerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatChatListTime(thread.lastMessageAt ?: thread.updatedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (unreadCount > 0) WhatsAppAccentGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isTyping) {
                        Text(
                            text = "টাইপ করছে...",
                            style = MaterialTheme.typography.bodySmall,
                            color = WhatsAppAccentGreen,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    } else {
                        if (isMineLast && thread.lastMessageText.isNotBlank()) {
                            Icon(
                                imageVector = if ((thread.unreadCounts[thread.partnerUid(currentUserId)] ?: 0L) == 0L) {
                                    Icons.Default.DoneAll
                                } else {
                                    Icons.Default.Done
                                },
                                contentDescription = null,
                                tint = if ((thread.unreadCounts[thread.partnerUid(currentUserId)] ?: 0L) == 0L) {
                                    Color(0xFF34B7F1)
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = thread.lastMessageText.ifBlank { "কথোপকথন শুরু করতে ট্যাপ করুন" },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (unreadCount > 0) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontWeight = if (unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (unreadCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(WhatsAppAccentGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (unreadCount > 99) "99+" else "$unreadCount",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF062E18)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatDirectoryUserRow(
    user: ChatUserProfile,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("chat_user_${user.userId}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ChatAvatar(
            name = user.displayName,
            photoUrl = user.photoUrl,
            isOnline = user.isOnline,
            sizeDp = 50
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.displayName.ifBlank { "NU Student" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = buildString {
                    append(user.departmentName.ifBlank { "অনার্স শিক্ষার্থী" })
                    append(" • Year ${user.yearNumber}")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (user.isOnline) "● অনলাইন" else formatLastSeen(user.lastSeenAt),
                style = MaterialTheme.typography.labelSmall,
                color = if (user.isOnline) WhatsAppAccentGreen else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Button(
            onClick = onClick,
            enabled = enabled,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = WhatsAppHeaderTeal,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text("চ্যাট", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun ChatAvatar(
    name: String,
    photoUrl: String,
    isOnline: Boolean,
    sizeDp: Int = 46
) {
    val initials = remember(name) {
        name.trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercaseChar().toString() }
            .ifBlank { "NU" }
    }
    val palette = remember {
        listOf(
            Color(0xFF0F766E),
            Color(0xFF1D4ED8),
            Color(0xFF7C3AED),
            Color(0xFFB45309),
            Color(0xFF047857)
        )
    }
    val avatarBg = palette[(name.hashCode().let { if (it < 0) -it else it }) % palette.size]

    Box(modifier = Modifier.size(sizeDp.dp)) {
        if (photoUrl.isNotBlank()) {
            CollegeImage(
                imageSource = photoUrl,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(avatarBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
        if (isOnline) {
            Box(
                modifier = Modifier
                    .size((sizeDp * 0.28f).dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(WhatsAppAccentGreen)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }
    }
}

internal fun formatChatListTime(timestamp: Timestamp?): String {
    val date = timestamp?.toDate() ?: return ""
    val msgCal = Calendar.getInstance().apply { time = date }
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    return when {
        msgCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            msgCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) ->
            SimpleDateFormat("h:mm a", Locale.ENGLISH).format(date)
        msgCal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
            msgCal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) ->
            "গতকাল"
        else -> SimpleDateFormat("dd MMM", Locale.ENGLISH).format(date)
    }
}

internal fun formatLastSeen(timestamp: Timestamp?): String {
    val date = timestamp?.toDate() ?: return "অফলাইন"
    return "সর্বশেষ সক্রিয়: ${formatChatListTime(timestamp)}"
}
