package com.example.messagerecovery.presentation.ui.conversation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messagerecovery.domain.repository.MediaVaultRepository
import com.example.messagerecovery.domain.repository.MessageRepository
import com.example.messagerecovery.presentation.ui.conversation.ConversationUiState
import com.example.messagerecovery.presentation.ui.conversation.MediaAttachmentUiModel
import com.example.messagerecovery.presentation.ui.conversation.MessageUiItem
import com.example.messagerecovery.utils.AudioPlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val mediaVaultRepository: MediaVaultRepository,
    private val audioPlayerManager: AudioPlayerManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialThreadId: String = savedStateHandle.get<String>("threadId") ?: ""
    private val _threadId = MutableStateFlow(initialThreadId)

    val uiState: StateFlow<ConversationUiState> = _threadId.flatMapLatest { threadId ->
        if (threadId.isBlank()) {
            flowOf(ConversationUiState(isLoading = false))
        } else {
            combine(
                messageRepository.getThreadsFlow(),
                messageRepository.getMessagesForThreadFlow(threadId),
                mediaVaultRepository.getAttachmentsForThreadFlow(threadId),
                audioPlayerManager.playbackState
            ) { threads, messages, attachments, playback ->
                val currentThread = threads.find { it.id == threadId }
                val attachmentMap = attachments.associateBy { it.messageId }

                val messageUiItems = messages.map { msg ->
                    val attachedMedia = (attachmentMap[msg.id] ?: attachments.find { it.messageId == null && abs(it.capturedTimestamp - msg.timestamp) < 5000L })?.let {
                        MediaAttachmentUiModel(
                            id = it.id,
                            mimeType = it.mimeType,
                            vaultPath = it.vaultPath,
                            fileSize = it.fileSize
                        )
                    }

                    MessageUiItem(
                        id = msg.id,
                        senderName = msg.senderName,
                        text = msg.rawText,
                        timestamp = msg.timestamp,
                        isDeleted = msg.isDeleted,
                        deletionTimestamp = msg.deletionTimestamp,
                        attachment = attachedMedia
                    )
                }

                ConversationUiState(
                    threadId = threadId,
                    contactTitle = currentThread?.displayName ?: "Unknown",
                    packageName = currentThread?.packageName ?: "",
                    messages = messageUiItems,
                    playbackState = playback,
                    isLoading = false
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ConversationUiState(isLoading = true)
    )

    fun setThreadId(threadId: String) {
        if (_threadId.value != threadId) {
            _threadId.value = threadId
        }
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

    fun deleteThread(onComplete: () -> Unit) {
        viewModelScope.launch {
            val id = _threadId.value
            if (id.isNotBlank()) {
                messageRepository.deleteThread(id)
                onComplete()
            }
        }
    }

    override fun onCleared() {
        audioPlayerManager.stopAudio()
    }
}
