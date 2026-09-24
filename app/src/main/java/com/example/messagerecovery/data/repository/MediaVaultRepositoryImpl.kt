package com.example.messagerecovery.data.repository

import android.util.Log
import com.example.messagerecovery.data.db.dao.AttachmentDao
import com.example.messagerecovery.data.db.entity.AttachmentEntity
import com.example.messagerecovery.domain.model.MediaAttachment
import com.example.messagerecovery.domain.repository.MediaVaultRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaVaultRepositoryImpl @Inject constructor(
    private val attachmentDao: AttachmentDao
) : MediaVaultRepository {

    override fun getAllAttachmentsFlow(): Flow<List<MediaAttachment>> =
        attachmentDao.getAllAttachmentsFlow().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getAttachmentsByTypeFlow(mimeTypePrefix: String): Flow<List<MediaAttachment>> =
        attachmentDao.getAttachmentsByTypeFlow(mimeTypePrefix).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getAttachmentsByPackageFlow(packageName: String): Flow<List<MediaAttachment>> =
        attachmentDao.getAttachmentsByPackageFlow(packageName).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getAttachmentsForThreadFlow(threadId: String): Flow<List<MediaAttachment>> =
        attachmentDao.getAttachmentsForThreadFlow(threadId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun recordAttachment(attachment: MediaAttachment): Long {
        val rowId = attachmentDao.insertAttachment(attachment.toEntity())
        Log.d(TAG, "Recorded media attachment in Room [id=$rowId, mime=${attachment.mimeType}, size=${attachment.fileSize}, pkg=${attachment.packageName}]")
        return rowId
    }

    override suspend fun existsByContentHash(contentHash: String): Boolean {
        return attachmentDao.existsByContentHash(contentHash)
    }

    override suspend fun correlateAttachmentWithMessage(attachmentId: Long, messageId: Long) {
        Log.d(TAG, "Correlated media attachment [$attachmentId] with message [$messageId]")
        attachmentDao.linkAttachmentToMessage(attachmentId, messageId)
    }

    override suspend fun deleteAttachment(id: Long) {
        Log.d(TAG, "Deleted media attachment record [$id] from Room")
        attachmentDao.deleteAttachmentById(id)
    }

    companion object {
        private const val TAG = "MediaVaultRepo"
    }

    private fun AttachmentEntity.toDomain() = MediaAttachment(
        id = id,
        messageId = messageId,
        threadId = threadId,
        packageName = packageName,
        mimeType = mimeType,
        vaultPath = vaultPath,
        fileSize = fileSize,
        contentHash = contentHash,
        capturedTimestamp = capturedTimestamp
    )

    private fun MediaAttachment.toEntity() = AttachmentEntity(
        id = id,
        messageId = messageId,
        threadId = threadId,
        packageName = packageName,
        mimeType = mimeType,
        vaultPath = vaultPath,
        fileSize = fileSize,
        contentHash = contentHash,
        capturedTimestamp = capturedTimestamp
    )
}