package com.example.messagerecovery.presentation.ui.chatlist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messagerecovery.domain.repository.MessageRepository
import com.example.messagerecovery.presentation.ui.chatlist.ChatListUiState
import com.example.messagerecovery.presentation.ui.chatlist.ChatThreadUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedAppFilter = MutableStateFlow("ALL")
    private val _onlyDeletedFilter = MutableStateFlow(false)

    val uiState: StateFlow<ChatListUiState> = combine(
        messageRepository.getThreadsFlow(),
        _searchQuery,
        _selectedAppFilter,
        _onlyDeletedFilter
    ) { threads, query, appFilter, onlyDeleted ->
        val filtered = threads.filter { thread ->
            val matchesApp = (appFilter == "ALL" || thread.packageName == appFilter)
            val matchesDeleted = (!onlyDeleted || thread.hasDeletedMessages)
            val matchesSearch = if (query.isBlank()) {
                true
            } else {
                thread.displayName.contains(query, ignoreCase = true) ||
                        (thread.lastSnippet ?: "").contains(query, ignoreCase = true)
            }
            matchesApp && matchesDeleted && matchesSearch
        }.map { thread ->
            ChatThreadUiModel(
                id = thread.id,
                packageName = thread.packageName,
                title = thread.displayName,
                lastSnippet = thread.lastSnippet ?: "",
                lastTimestamp = thread.lastTimestamp,
                unreadCount = 0,
                hasDeletedMessages = thread.hasDeletedMessages
            )
        }

        ChatListUiState(
            searchQuery = query,
            selectedAppFilter = appFilter,
            onlyDeletedFilter = onlyDeleted,
            threads = filtered,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChatListUiState(isLoading = true)
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onAppFilterSelect(pkg: String) {
        _selectedAppFilter.value = pkg
        // Reset deleted-only filter when switching app tabs so other tabs do not appear empty
        _onlyDeletedFilter.value = false
    }

    fun onToggleOnlyDeleted() {
        _onlyDeletedFilter.value = !_onlyDeletedFilter.value
    }

    fun resetFilters() {
        _searchQuery.value = ""
        _selectedAppFilter.value = "ALL"
        _onlyDeletedFilter.value = false
    }

    fun deleteThread(threadId: String) {
        viewModelScope.launch {
            messageRepository.deleteThread(threadId)
        }
    }
}
