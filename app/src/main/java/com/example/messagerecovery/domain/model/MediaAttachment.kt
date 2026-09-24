package com.example.messagerecovery.domain.model

data class MediaAttachment(
    val id: Long = 0,
    val messageId: Long? = null,
    val threadId: String? = null,
    val packageName: String,
    val mimeType: String,
    val vaultPath: String,
    val fileSize: Long,
    val contentHash: String? = null,
    val capturedTimestamp: Long
)
