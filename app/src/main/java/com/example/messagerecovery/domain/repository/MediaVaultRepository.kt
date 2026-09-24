package com.example.messagerecovery.domain.repository

import com.example.messagerecovery.domain.model.MediaAttachment
import kotlinx.coroutines.flow.Flow

interface MediaVaultRepository {

    fun getAllAttachmentsFlow(): Flow<List<MediaAttachment>>

    fun getAttachmentsByTypeFlow(mimeTypePrefix: String): Flow<List<MediaAttachment>>

    fun getAttachmentsByPackageFlow(packageName: String): Flow<List<MediaAttachment>>

    fun getAttachmentsForThreadFlow(threadId: String): Flow<List<MediaAttachment>>

    suspend fun recordAttachment(attachment: MediaAttachment): Long

    suspend fun existsByContentHash(contentHash: String): Boolean

    suspend fun correlateAttachmentWithMessage(attachmentId: Long, messageId: Long)

    suspend fun deleteAttachment(id: Long)
}
