package com.example.messagerecovery.domain.model

data class RecoveredMessage(
    val id: Long = 0,
    val threadId: String,
    val dedupHash: String,
    val senderName: String,
    val rawText: String,
    val timestamp: Long,
    val isDeleted: Boolean = false,
    val hasAttachment: Boolean = false,
    val deletionTimestamp: Long? = null
)
