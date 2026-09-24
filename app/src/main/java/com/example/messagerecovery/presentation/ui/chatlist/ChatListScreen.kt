package com.example.messagerecovery.presentation.ui.chatlist

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.messagerecovery.presentation.ui.chatlist.viewmodel.ChatListViewModel

@Composable
fun ChatListScreen(
    onNavigateToConversation: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val hasActiveFilter = state.searchQuery.isNotEmpty() || state.onlyDeletedFilter || state.selectedAppFilter != "ALL"
    BackHandler(enabled = hasActiveFilter) {
        when {
            state.searchQuery.isNotEmpty() -> viewModel.onSearchQueryChange("")
            state.onlyDeletedFilter -> viewModel.onToggleOnlyDeleted()
            state.selectedAppFilter != "ALL" -> viewModel.onAppFilterSelect("ALL")
        }
    }

    ChatListContent(
        state = state,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onAppFilterSelect = viewModel::onAppFilterSelect,
        onToggleOnlyDeleted = viewModel::onToggleOnlyDeleted,
        onThreadClick = onNavigateToConversation,
        onDeleteThread = viewModel::deleteThread,
        modifier = modifier
    )
}
