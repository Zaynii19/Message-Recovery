package com.example.messagerecovery.presentation.ui.conversation

import com.example.messagerecovery.utils.AudioPlaybackState

data class MediaAttachmentUiModel(
    val id: Long,
    val mimeType: String,
    val vaultPath: String,
    val fileSize: Long
)

data class MessageUiItem(
    val id: Long,
    val senderName: String,
    val text: String,
    val timestamp: Long,
    val isDeleted: Boolean,
    val deletionTimestamp: Long? = null,
    val attachment: MediaAttachmentUiModel? = null
)

data class ConversationUiState(
    val threadId: String = "",
    val contactTitle: String = "",
    val packageName: String = "",
    val messages: List<MessageUiItem> = emptyList(),
    val playbackState: AudioPlaybackState = AudioPlaybackState(),
    val isLoading: Boolean = false
)
