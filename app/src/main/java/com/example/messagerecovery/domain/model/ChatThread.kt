package com.example.messagerecovery.domain.model

data class ChatThread(
    val id: String,
    val packageName: String,
    val displayName: String,
    val avatarPath: String? = null,
    val lastSnippet: String? = null,
    val lastTimestamp: Long,
    val hasDeletedMessages: Boolean = false
)
