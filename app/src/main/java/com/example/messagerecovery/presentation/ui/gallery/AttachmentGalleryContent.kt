package com.example.messagerecovery.presentation.ui.gallery

import android.content.Intent
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.messagerecovery.presentation.theme.AlertRed
import com.example.messagerecovery.presentation.theme.CyberObsidian
import com.example.messagerecovery.presentation.theme.CyberSurfaceDark
import com.example.messagerecovery.presentation.theme.CyberSurfaceElevated
import com.example.messagerecovery.presentation.theme.ElectricCyan
import com.example.messagerecovery.presentation.theme.MessageRecoveryTheme
import com.example.messagerecovery.presentation.theme.TextPrimary
import com.example.messagerecovery.presentation.theme.TextSecondary
import com.example.messagerecovery.presentation.ui.chatlist.getAppColor
import com.example.messagerecovery.presentation.ui.conversation.AudioMiniPlayerBar
import com.example.messagerecovery.presentation.ui.conversation.formatFileSize
import com.example.messagerecovery.utils.AudioPlaybackState
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AttachmentGalleryContent(
    state: AttachmentGalleryUiState,
    onCategorySelect: (String) -> Unit,
    onAppFilterSelect: (String) -> Unit,
    onToggleSelection: (Long) -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onDeleteSelected: () -> Unit,
    onExportSelected: () -> Unit,
    onExportSingle: (AttachmentUiModel) -> Unit,
    onOpenPreview: (AttachmentUiModel) -> Unit,
    onClosePreview: () -> Unit,
    onPlayAudio: (Long, String) -> Unit,
    onPauseAudio: () -> Unit,
    onResumeAudio: () -> Unit,
    onSeekAudio: (Int) -> Unit,
    onStopAudio: () -> Unit,
    onDismissMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            onDismissMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CyberObsidian,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Selection Action Bar (when in selection mode)
            if (state.isSelectionMode) {
                Card(
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onClearSelection) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${state.selectedIds.size} Selected",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = ElectricCyan
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onSelectAll) {
                                Icon(Icons.Default.SelectAll, contentDescription = "Select All", tint = TextPrimary)
                            }
                            IconButton(onClick = onExportSelected) {
                                Icon(Icons.Default.Download, contentDescription = "Save to Public Storage", tint = ElectricCyan)
                            }
                            IconButton(onClick = onDeleteSelected) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AlertRed)
                            }
                        }
                    }
                }
            }

            // Category Filter Chips
            val categories = listOf(
                "ALL" to "All",
                "IMAGE" to "Photos",
                "VIDEO" to "Videos",
                "AUDIO" to "Voice/Audio",
                "DOCUMENT" to "Documents"
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { (cat, label) ->
                    val isSelected = state.selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelect(cat) },
                        label = { Text(text = label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = CyberSurfaceElevated,
                            labelColor = TextSecondary,
                            selectedContainerColor = ElectricCyan.copy(alpha = 0.2f),
                            selectedLabelColor = ElectricCyan
                        )
                    )
                }
            }

            // App Source Filter Chips
            val appFilters = listOf(
                "ALL" to "All Apps",
                "com.whatsapp" to "WhatsApp",
                "com.whatsapp.w4b" to "WA Business",
                "com.facebook.orca" to "Messenger",
                "com.instagram.android" to "Instagram"
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(appFilters) { (pkg, label) ->
                    val isSelected = state.selectedAppFilter == pkg
                    FilterChip(
                        selected = isSelected,
                        onClick = { onAppFilterSelect(pkg) },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (pkg != "ALL") {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(getAppColor(pkg))
                                    )
                                }
                                Text(text = label, fontSize = 12.sp)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = CyberSurfaceElevated,
                            labelColor = TextSecondary,
                            selectedContainerColor = ElectricCyan.copy(alpha = 0.2f),
                            selectedLabelColor = ElectricCyan
                        )
                    )
                }
            }

            // Media Grid / Empty State
            if (state.attachments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(CyberSurfaceDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PermMedia,
                                contentDescription = null,
                                tint = ElectricCyan.copy(alpha = 0.6f),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No captured media yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Photos, voice notes, and videos received from WhatsApp, WA Business, Messenger, and Instagram will automatically be cloned into this secure vault.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(
                        items = state.attachments,
                        key = { it.id }
                    ) { item ->
                        AttachmentGridTile(
                            item = item,
                            isSelectionMode = state.isSelectionMode,
                            playbackState = state.playbackState,
                            onToggleSelection = { onToggleSelection(item.id) },
                            onOpenPreview = { onOpenPreview(item) },
                            onPlayAudio = { onPlayAudio(item.id, item.vaultPath) },
                            onPauseAudio = onPauseAudio
                        )
                    }
                }
            }
        }
    }

    val context = LocalContext.current

    // Full-screen Media Preview Dialog
    state.previewAttachment?.let { preview ->
        val isVideo = preview.mimeType.startsWith("VIDEO")
        val file = File(preview.vaultPath)

        Dialog(
            onDismissRequest = onClosePreview,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CyberObsidian)
            ) {
                if (isVideo) {
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                val controller = MediaController(ctx)
                                controller.setAnchorView(this)
                                setMediaController(controller)
                                setVideoPath(preview.vaultPath)
                                setOnPreparedListener { mp ->
                                    mp.isLooping = true
                                    start()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                    )
                } else {
                    AsyncImage(
                        model = file,
                        contentDescription = "Fullscreen Media",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                // Top Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onClosePreview,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(CyberSurfaceDark.copy(alpha = 0.8f))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = {
                                try {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, if (isVideo) "video/*" else "image/*")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Open with"))
                                } catch (e: Exception) {
                                    android.util.Log.e("AttachmentGallery", "Failed to launch external player", e)
                                }
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(CyberSurfaceDark.copy(alpha = 0.8f))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Open in External App", tint = ElectricCyan)
                        }

                        IconButton(
                            onClick = { onExportSingle(preview) },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(CyberSurfaceDark.copy(alpha = 0.8f))
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Save to Device", tint = ElectricCyan)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AttachmentGridTile(
    item: AttachmentUiModel,
    isSelectionMode: Boolean,
    playbackState: AudioPlaybackState,
    onToggleSelection: () -> Unit,
    onOpenPreview: () -> Unit,
    onPlayAudio: () -> Unit,
    onPauseAudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val file = File(item.vaultPath)
    val mime = item.mimeType.uppercase()
    val isPlayingAudio = playbackState.currentAttachmentId == item.id && playbackState.isPlaying

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(CyberSurfaceDark)
            .border(
                width = if (item.isSelected) 2.dp else 1.dp,
                color = if (item.isSelected) ElectricCyan else CyberSurfaceElevated,
                shape = RoundedCornerShape(10.dp)
            )
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        onToggleSelection()
                    } else if (mime.startsWith("IMAGE") || mime.startsWith("VIDEO")) {
                        onOpenPreview()
                    } else if (mime.startsWith("AUDIO")) {
                        if (isPlayingAudio) onPauseAudio() else onPlayAudio()
                    }
                },
                onLongClick = onToggleSelection
            )
    ) {
        when {
            mime.startsWith("IMAGE") -> {
                AsyncImage(
                    model = file,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            mime.startsWith("VIDEO") -> {
                AsyncImage(
                    model = file,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(CyberObsidian.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircleOutline,
                        contentDescription = "Play Video",
                        tint = ElectricCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            mime.startsWith("AUDIO") -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlayingAudio) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlayingAudio) "Pause" else "Play",
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Voice Note",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = formatFileSize(item.fileSize),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FileCopy,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = file.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Selection Checkmark Overlay
        if (isSelectionMode) {
            Box(
                modifier = Modifier
                    .padding(6.dp)
                    .align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = if (item.isSelected) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = null,
                    tint = if (item.isSelected) ElectricCyan else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // App Origin Tag
        Box(
            modifier = Modifier
                .padding(6.dp)
                .size(8.dp)
                .align(Alignment.BottomStart)
                .clip(CircleShape)
                .background(getAppColor(item.packageName))
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun AttachmentGalleryContentPreview() {
    MessageRecoveryTheme(darkTheme = true) {
        AttachmentGalleryContent(
            state = AttachmentGalleryUiState(
                attachments = listOf(
                    AttachmentUiModel(
                        id = 1,
                        messageId = 1,
                        threadId = "wa_alice",
                        packageName = "com.whatsapp",
                        mimeType = "IMAGE",
                        vaultPath = "/vault/sample1.jpg",
                        fileSize = 1048576,
                        capturedTimestamp = System.currentTimeMillis()
                    ),
                    AttachmentUiModel(
                        id = 2,
                        messageId = 2,
                        threadId = "fb_bob",
                        packageName = "com.facebook.orca",
                        mimeType = "AUDIO",
                        vaultPath = "/vault/sample2.opus",
                        fileSize = 51200,
                        capturedTimestamp = System.currentTimeMillis()
                    ),
                    AttachmentUiModel(
                        id = 3,
                        messageId = 3,
                        threadId = "ig_charlie",
                        packageName = "com.instagram.android",
                        mimeType = "VIDEO",
                        vaultPath = "/vault/sample3.mp4",
                        fileSize = 5242880,
                        capturedTimestamp = System.currentTimeMillis()
                    )
                )
            ),
            onCategorySelect = {},
            onAppFilterSelect = {},
            onToggleSelection = {},
            onSelectAll = {},
            onClearSelection = {},
            onDeleteSelected = {},
            onExportSelected = {},
            onExportSingle = {},
            onOpenPreview = {},
            onClosePreview = {},
            onPlayAudio = { _, _ -> },
            onPauseAudio = {},
            onResumeAudio = {},
            onSeekAudio = {},
            onStopAudio = {},
            onDismissMessage = {}
        )
    }
}
