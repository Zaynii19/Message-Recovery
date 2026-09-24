package com.example.messagerecovery.presentation.ui.gallery

import com.example.messagerecovery.utils.AudioPlaybackState

data class AttachmentUiModel(
    val id: Long,
    val messageId: Long?,
    val threadId: String?,
    val packageName: String,
    val mimeType: String,
    val vaultPath: String,
    val fileSize: Long,
    val capturedTimestamp: Long,
    val isSelected: Boolean = false
)

data class AttachmentGalleryUiState(
    val selectedCategory: String = "ALL", // "ALL", "IMAGE", "VIDEO", "AUDIO", "DOCUMENT"
    val selectedAppFilter: String = "ALL",
    val attachments: List<AttachmentUiModel> = emptyList(),
    val selectedIds: Set<Long> = emptySet(),
    val isSelectionMode: Boolean = false,
    val playbackState: AudioPlaybackState = AudioPlaybackState(),
    val previewAttachment: AttachmentUiModel? = null,
    val isLoading: Boolean = false,
    val userMessage: String? = null
)
