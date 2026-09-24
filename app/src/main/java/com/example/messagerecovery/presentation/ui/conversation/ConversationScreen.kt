package com.example.messagerecovery.presentation.ui.conversation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.messagerecovery.presentation.ui.conversation.viewmodel.ConversationViewModel

@Composable
fun ConversationScreen(
    threadId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConversationViewModel = hiltViewModel()
) {
    BackHandler {
        onNavigateBack()
    }

    LaunchedEffect(threadId) {
        viewModel.setThreadId(threadId)
    }

    val state by viewModel.uiState.collectAsState()

    ConversationContent(
        state = state,
        onBackClick = onNavigateBack,
        onDeleteThreadClick = {
            viewModel.deleteThread(onComplete = onNavigateBack)
        },
        onPlayAudio = viewModel::playAudio,
        onPauseAudio = viewModel::pauseAudio,
        onResumeAudio = viewModel::resumeAudio,
        onSeekAudio = viewModel::seekTo,
        onStopAudio = viewModel::stopAudio,
        modifier = modifier
    )
}
