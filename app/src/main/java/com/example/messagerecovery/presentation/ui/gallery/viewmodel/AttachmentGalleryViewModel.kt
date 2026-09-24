package com.example.messagerecovery.presentation.ui.gallery.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messagerecovery.domain.repository.MediaVaultRepository
import com.example.messagerecovery.presentation.ui.gallery.AttachmentGalleryUiState
import com.example.messagerecovery.presentation.ui.gallery.AttachmentUiModel
import com.example.messagerecovery.utils.MediaExport
import com.example.messagerecovery.utils.AudioPlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AttachmentGalleryViewModel @Inject constructor(
    private val mediaVaultRepository: MediaVaultRepository,
    private val audioPlayerManager: AudioPlayerManager
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("ALL")
    private val _selectedAppFilter = MutableStateFlow("ALL")
    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _previewAttachment = MutableStateFlow<AttachmentUiModel?>(null)
    private val _userMessage = MutableStateFlow<String?>(null)

    private data class GalleryFilterState(
        val category: String,
        val appFilter: String,
        val selectedIds: Set<Long>,
        val previewAttachment: AttachmentUiModel?
    )

    private val filterState = combine(
        _selectedCategory,
        _selectedAppFilter,
        _selectedIds,
        _previewAttachment
    ) { category, appFilter, selectedIds, preview ->
        GalleryFilterState(category, appFilter, selectedIds, preview)
    }

    val uiState: StateFlow<AttachmentGalleryUiState> = combine(
        mediaVaultRepository.getAllAttachmentsFlow(),
        filterState,
        _userMessage,
        audioPlayerManager.playbackState
    ) { attachments, filter, message, playback ->
        val filtered = attachments.filter { item ->
            val matchesCategory = (filter.category == "ALL" || item.mimeType.startsWith(filter.category, ignoreCase = true))
            val matchesApp = (filter.appFilter == "ALL" || item.packageName == filter.appFilter)
            matchesCategory && matchesApp
        }.map { item ->
            AttachmentUiModel(
                id = item.id,
                messageId = item.messageId,
                threadId = item.threadId,
                packageName = item.packageName,
                mimeType = item.mimeType,
                vaultPath = item.vaultPath,
                fileSize = item.fileSize,
                capturedTimestamp = item.capturedTimestamp,
                isSelected = filter.selectedIds.contains(item.id)
            )
        }

        AttachmentGalleryUiState(
            selectedCategory = filter.category,
            selectedAppFilter = filter.appFilter,
            attachments = filtered,
            selectedIds = filter.selectedIds,
            isSelectionMode = filter.selectedIds.isNotEmpty(),
            playbackState = playback,
            previewAttachment = filter.previewAttachment,
            isLoading = false,
            userMessage = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AttachmentGalleryUiState(isLoading = true)
    )

    fun onCategorySelect(category: String) {
        _selectedCategory.value = category
    }

    fun onAppFilterSelect(pkg: String) {
        _selectedAppFilter.value = pkg
    }

    fun onToggleSelection(id: Long) {
        val current = _selectedIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _selectedIds.value = current
    }

    fun onSelectAll() {
        val currentAttachments = uiState.value.attachments
        _selectedIds.value = currentAttachments.map { it.id }.toSet()
    }

    fun onClearSelection() {
        _selectedIds.value = emptySet()
    }

    fun deleteSelected() {
        viewModelScope.launch {
            val toDelete = _selectedIds.value
            val currentItems = uiState.value.attachments.associateBy { it.id }

            toDelete.forEach { id ->
                currentItems[id]?.let { item ->
                    try {
                        File(item.vaultPath).delete()
                    } catch (e: Exception) {
                        Log.e("AttachmentGalleryVM", "Failed to delete physical file: ${item.vaultPath}", e)
                    }
                    mediaVaultRepository.deleteAttachment(id)
                }
            }
            _selectedIds.value = emptySet()
            _userMessage.value = "Deleted ${toDelete.size} items"
        }
    }

    fun exportSelected(context: Context) {
        viewModelScope.launch {
            val toExport = _selectedIds.value
            val currentItems = uiState.value.attachments.associateBy { it.id }
            var successCount = 0

            toExport.forEach { id ->
                currentItems[id]?.let { item ->
                    val result = MediaExport.exportToPublicStorage(
                        context = context,
                        sourceFilePath = item.vaultPath,
                        mimeType = item.mimeType
                    )
                    if (result.isSuccess) successCount++
                }
            }

            _selectedIds.value = emptySet()
            _userMessage.value = "Saved $successCount items to Public Storage"
        }
    }

    fun exportSingle(context: Context, attachment: AttachmentUiModel) {
        viewModelScope.launch {
            val result = MediaExport.exportToPublicStorage(
                context = context,
                sourceFilePath = attachment.vaultPath,
                mimeType = attachment.mimeType
            )
            if (result.isSuccess) {
                _userMessage.value = "Saved to Public Storage"
            } else {
                _userMessage.value = "Failed to save file: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun openPreview(attachment: AttachmentUiModel) {
        _previewAttachment.value = attachment
    }

    fun closePreview() {
        _previewAttachment.value = null
    }

    fun dismissUserMessage() {
        _userMessage.value = null
    }

    fun playAudio(attachmentId: Long, filePath: String) {
        audioPlayerManager.playAudio(attachmentId, filePath)
    }

    fun pauseAudio() {
        audioPlayerManager.pauseAudio()
    }

    fun resumeAudio() {
        audioPlayerManager.resumeAudio()
    }

    fun seekTo(positionMs: Int) {
        audioPlayerManager.seekTo(positionMs)
    }

    fun stopAudio() {
        audioPlayerManager.stopAudio()
    }

    override fun onCleared() {
        audioPlayerManager.stopAudio()
    }
}
