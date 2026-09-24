package com.example.messagerecovery.presentation.ui.chatlist

data class ChatThreadUiModel(
    val id: String,
    val packageName: String,
    val title: String,
    val lastSnippet: String,
    val lastTimestamp: Long,
    val unreadCount: Int = 0,
    val hasDeletedMessages: Boolean = false
)

data class ChatListUiState(
    val searchQuery: String = "",
    val selectedAppFilter: String = "ALL", // "ALL", "com.whatsapp", "com.whatsapp.w4b", "com.facebook.orca", "com.instagram.android"
    val onlyDeletedFilter: Boolean = false,
    val threads: List<ChatThreadUiModel> = emptyList(),
    val isLoading: Boolean = false
)
