package com.example.messagerecovery.presentation.ui.gallery

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.messagerecovery.presentation.ui.gallery.viewmodel.AttachmentGalleryViewModel

@Composable
fun AttachmentGalleryScreen(
    modifier: Modifier = Modifier,
    viewModel: AttachmentGalleryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val hasActiveState = state.previewAttachment != null ||
            state.selectedIds.isNotEmpty() ||
            state.selectedCategory != "ALL" ||
            state.selectedAppFilter != "ALL"

    BackHandler(enabled = hasActiveState) {
        when {
            state.previewAttachment != null -> viewModel.closePreview()
            state.selectedIds.isNotEmpty() -> viewModel.onClearSelection()
            state.selectedCategory != "ALL" -> viewModel.onCategorySelect("ALL")
            state.selectedAppFilter != "ALL" -> viewModel.onAppFilterSelect("ALL")
        }
    }

    AttachmentGalleryContent(
        state = state,
        onCategorySelect = viewModel::onCategorySelect,
        onAppFilterSelect = viewModel::onAppFilterSelect,
        onToggleSelection = viewModel::onToggleSelection,
        onSelectAll = viewModel::onSelectAll,
        onClearSelection = viewModel::onClearSelection,
        onDeleteSelected = viewModel::deleteSelected,
        onExportSelected = { viewModel.exportSelected(context) },
        onExportSingle = { viewModel.exportSingle(context, it) },
        onOpenPreview = viewModel::openPreview,
        onClosePreview = viewModel::closePreview,
        onPlayAudio = viewModel::playAudio,
        onPauseAudio = viewModel::pauseAudio,
        onResumeAudio = viewModel::resumeAudio,
        onSeekAudio = viewModel::seekTo,
        onStopAudio = viewModel::stopAudio,
        onDismissMessage = viewModel::dismissUserMessage,
        modifier = modifier
    )
}
