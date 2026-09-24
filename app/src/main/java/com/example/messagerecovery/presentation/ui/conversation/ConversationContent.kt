package com.example.messagerecovery.presentation.ui.conversation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.messagerecovery.presentation.theme.AlertRed
import com.example.messagerecovery.presentation.theme.CyberObsidian
import com.example.messagerecovery.presentation.theme.CyberSurfaceDark
import com.example.messagerecovery.presentation.theme.CyberSurfaceElevated
import com.example.messagerecovery.presentation.theme.ElectricCyan
import com.example.messagerecovery.presentation.theme.MessageRecoveryTheme
import com.example.messagerecovery.presentation.theme.TextPrimary
import com.example.messagerecovery.presentation.theme.TextSecondary
import com.example.messagerecovery.presentation.theme.WarningYellow
import com.example.messagerecovery.presentation.ui.chatlist.getAppColor
import com.example.messagerecovery.utils.AudioPlaybackState
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationContent(
    state: ConversationUiState,
    onBackClick: () -> Unit,
    onDeleteThreadClick: () -> Unit,
    onPlayAudio: (Long, String) -> Unit,
    onPauseAudio: () -> Unit,
    onResumeAudio: () -> Unit,
    onSeekAudio: (Int) -> Unit,
    onStopAudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = remember(state.threadId) { LazyListState() }
    var hasScrolledToInitialBottom by remember(state.threadId) { mutableStateOf(false) }

    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) true
            else {
                val lastVisibleItem = visibleItems.last()
                lastVisibleItem.index >= layoutInfo.totalItemsCount - 2
            }
        }
    }

    LaunchedEffect(state.threadId, state.messages.size) {
        if (state.messages.isNotEmpty()) {
            if (!hasScrolledToInitialBottom) {
                listState.scrollToItem(state.messages.size - 1)
                hasScrolledToInitialBottom = true
            } else if (isAtBottom) {
                listState.animateScrollToItem(state.messages.size - 1)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CyberObsidian,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(getAppColor(state.packageName).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = state.contactTitle.take(1).uppercase(),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = getAppColor(state.packageName)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = state.contactTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = resolveAppDisplayName(state.packageName),
                                style = MaterialTheme.typography.labelSmall,
                                color = getAppColor(state.packageName)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onDeleteThreadClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Conversation",
                            tint = AlertRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CyberSurfaceDark)
            )
        },
        bottomBar = {
            // Floating bottom mini-player for audio
            if (state.playbackState.filePath != null) {
                AudioMiniPlayerBar(
                    playbackState = state.playbackState,
                    onPauseAudio = onPauseAudio,
                    onResumeAudio = onResumeAudio,
                    onSeekAudio = onSeekAudio,
                    onStopAudio = onStopAudio
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            state = listState,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(
                items = state.messages,
                key = { _, msg -> msg.id }
            ) { index, msg ->
                val prevMsg = if (index > 0) state.messages[index - 1] else null
                val showDate = prevMsg == null || !isSameDay(prevMsg.timestamp, msg.timestamp)

                if (showDate) {
                    DateSeparatorChip(timestamp = msg.timestamp)
                }

                MessageBubbleItem(
                    message = msg,
                    contactTitle = state.contactTitle,
                    playbackState = state.playbackState,
                    onPlayAudio = onPlayAudio,
                    onPauseAudio = onPauseAudio,
                    onResumeAudio = onResumeAudio
                )
            }
        }
    }
}

@Composable
fun DateSeparatorChip(
    timestamp: Long,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF182229),
            shadowElevation = 1.dp
        ) {
            Text(
                text = formatDateHeader(timestamp),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp
                ),
                color = Color(0xFF8696A0),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun MessageBubbleItem(
    message: MessageUiItem,
    contactTitle: String,
    playbackState: AudioPlaybackState,
    onPlayAudio: (Long, String) -> Unit,
    onPauseAudio: () -> Unit,
    onResumeAudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDeleted = message.isDeleted
    val showSender = message.senderName.isNotBlank() &&
            !message.senderName.equals(contactTitle, ignoreCase = true)

    val isSingleLineShortText = !isDeleted &&
            !showSender &&
            message.attachment == null &&
            message.text.isNotBlank() &&
            message.text.length <= 26 &&
            !message.text.contains("\n")

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 4.dp,
                topEnd = 14.dp,
                bottomEnd = 14.dp,
                bottomStart = 14.dp
            ),
            color = if (isDeleted) Color(0xFF28171F) else Color(0xFF202C33),
            border = if (isDeleted) BorderStroke(1.dp, AlertRed.copy(alpha = 0.55f)) else null,
            shadowElevation = 1.dp,
            modifier = Modifier
                .widthIn(min = 52.dp, max = 320.dp)
                .fillMaxWidth(0.85f)
                .wrapContentWidth(Alignment.Start)
        ) {
            if (isSingleLineShortText) {
                // Compact inline row for short single-line messages (e.g. "Hn 08:19 PM", "Ok 08:13 AM")
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.padding(start = 10.dp, end = 8.dp, top = 6.dp, bottom = 6.dp)
                ) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.5.sp,
                            lineHeight = 19.sp
                        ),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatClockTime(message.timestamp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = Color(0xFF8696A0),
                        modifier = Modifier.padding(bottom = 1.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 6.dp, bottom = 5.dp)
                ) {
                    // Header for Deleted Messages
                    if (isDeleted) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(AlertRed.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = AlertRed,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "DELETED MESSAGE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.5.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = AlertRed
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Sender Name (Only in group chats when sender != contact title)
                    if (showSender) {
                        Text(
                            text = message.senderName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            ),
                            color = ElectricCyan,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    // Attached Media Preview
                    message.attachment?.let { attachment ->
                        MediaAttachmentBubble(
                            attachment = attachment,
                            playbackState = playbackState,
                            onPlayAudio = onPlayAudio,
                            onPauseAudio = onPauseAudio,
                            onResumeAudio = onResumeAudio
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Message Body Text
                    if (message.text.isNotBlank()) {
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.5.sp,
                                lineHeight = 19.sp
                            ),
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Timestamps: Captured vs Deleted
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isDeleted && message.deletionTimestamp != null) {
                            Text(
                                text = "Del: ${formatClockTime(message.deletionTimestamp)} • ",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = WarningYellow.copy(alpha = 0.9f)
                            )
                        }
                        Text(
                            text = formatClockTime(message.timestamp),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color(0xFF8696A0)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MediaAttachmentBubble(
    attachment: MediaAttachmentUiModel,
    playbackState: AudioPlaybackState,
    onPlayAudio: (Long, String) -> Unit,
    onPauseAudio: () -> Unit,
    onResumeAudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val file = File(attachment.vaultPath)
    val mime = attachment.mimeType.uppercase()

    when {
        mime.startsWith("IMAGE") -> {
            AsyncImage(
                model = file,
                contentDescription = "Recovered Image",
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 220.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }
        mime.startsWith("VIDEO") -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 220.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = file,
                    contentDescription = "Recovered Video Thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CyberObsidian.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircleOutline,
                        contentDescription = "Play Video",
                        tint = ElectricCyan,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
        mime.startsWith("AUDIO") -> {
            val isCurrentAudio = playbackState.currentAttachmentId == attachment.id
            val isPlaying = isCurrentAudio && playbackState.isPlaying

            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceElevated.copy(alpha = 0.6f)),
                modifier = modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (isPlaying) {
                                onPauseAudio()
                            } else if (isCurrentAudio) {
                                onResumeAudio()
                            } else {
                                onPlayAudio(attachment.id, attachment.vaultPath)
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = ElectricCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Voice Note",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )
                        }
                        if (isCurrentAudio && playbackState.durationMs > 0) {
                            Spacer(modifier = Modifier.height(3.dp))
                            LinearProgressIndicator(
                                progress = { (playbackState.currentPositionMs.toFloat() / playbackState.durationMs.toFloat()).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = ElectricCyan,
                                trackColor = CyberObsidian
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formatFileSize(attachment.fileSize),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Color(0xFF8696A0)
                    )
                }
            }
        }
        else -> {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceElevated.copy(alpha = 0.6f)),
                modifier = modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Document: ${file.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun AudioMiniPlayerBar(
    playbackState: AudioPlaybackState,
    onPauseAudio: () -> Unit,
    onResumeAudio: () -> Unit,
    onSeekAudio: (Int) -> Unit,
    onStopAudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, ElectricCyan.copy(alpha = 0.3f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = {
                        if (playbackState.isPlaying) onPauseAudio() else onResumeAudio()
                    }
                ) {
                    Icon(
                        imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                        tint = ElectricCyan
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = File(playbackState.filePath ?: "").name,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary,
                        maxLines = 1
                    )
                    Slider(
                        value = playbackState.currentPositionMs.toFloat(),
                        onValueChange = { onSeekAudio(it.toInt()) },
                        valueRange = 0f..playbackState.durationMs.toFloat().coerceAtLeast(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricCyan,
                            activeTrackColor = ElectricCyan,
                            inactiveTrackColor = CyberSurfaceElevated
                        ),
                        modifier = Modifier.height(20.dp)
                    )
                }

                Text(
                    text = "${formatAudioTime(playbackState.currentPositionMs)} / ${formatAudioTime(playbackState.durationMs)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )

                IconButton(onClick = onStopAudio) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Stop",
                        tint = TextSecondary
                    )
                }
            }
        }
    }
}

fun resolveAppDisplayName(packageName: String): String {
    return when (packageName) {
        "com.whatsapp" -> "WhatsApp"
        "com.whatsapp.w4b" -> "WhatsApp Business"
        "com.facebook.orca" -> "Messenger"
        "com.instagram.android" -> "Instagram Direct"
        else -> "Message Recovery"
    }
}

fun formatClockTime(timestamp: Long): String {
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
}

fun isSameDay(t1: Long, t2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = t1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = t2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun formatDateHeader(timestamp: Long): String {
    val calMsg = Calendar.getInstance().apply { timeInMillis = timestamp }
    val calToday = Calendar.getInstance()

    if (calMsg.get(Calendar.YEAR) == calToday.get(Calendar.YEAR) &&
        calMsg.get(Calendar.DAY_OF_YEAR) == calToday.get(Calendar.DAY_OF_YEAR)
    ) {
        return "Today"
    }

    val calYesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    if (calMsg.get(Calendar.YEAR) == calYesterday.get(Calendar.YEAR) &&
        calMsg.get(Calendar.DAY_OF_YEAR) == calYesterday.get(Calendar.DAY_OF_YEAR)
    ) {
        return "Yesterday"
    }

    val pattern = if (calMsg.get(Calendar.YEAR) == calToday.get(Calendar.YEAR)) "d MMMM" else "d MMMM yyyy"
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))
}

fun formatAudioTime(ms: Int): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}

fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0))
    }
}

@Preview(showSystemUi = true)
@Composable
fun ConversationContentPreview() {
    MessageRecoveryTheme(darkTheme = true) {
        ConversationContent(
            state = ConversationUiState(
                contactTitle = "Alice Smith",
                packageName = "com.whatsapp",
                messages = listOf(
                    MessageUiItem(
                        id = 1,
                        senderName = "Alice",
                        text = "Hey! What time are we meeting today?",
                        timestamp = System.currentTimeMillis() - 600_000L,
                        isDeleted = false
                    ),
                    MessageUiItem(
                        id = 2,
                        senderName = "Alice",
                        text = "Actually, let me send you the secret passcode: 9942",
                        timestamp = System.currentTimeMillis() - 300_000L,
                        isDeleted = true,
                        deletionTimestamp = System.currentTimeMillis() - 120_000L
                    ),
                    MessageUiItem(
                        id = 3,
                        senderName = "Alice",
                        text = "Did you get my voice note?",
                        timestamp = System.currentTimeMillis() - 60_000L,
                        isDeleted = false,
                        attachment = MediaAttachmentUiModel(
                            id = 10,
                            mimeType = "AUDIO",
                            vaultPath = "/vault/sample.opus",
                            fileSize = 48500
                        )
                    )
                )
            ),
            onBackClick = {},
            onDeleteThreadClick = {},
            onPlayAudio = { _, _ -> },
            onPauseAudio = {},
            onResumeAudio = {},
            onSeekAudio = {},
            onStopAudio = {}
        )
    }
}
