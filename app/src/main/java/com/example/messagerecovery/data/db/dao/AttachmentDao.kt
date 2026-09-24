package com.example.messagerecovery.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.messagerecovery.data.db.entity.AttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAttachment(attachment: AttachmentEntity): Long

    @Query("SELECT EXISTS(SELECT 1 FROM attachments WHERE content_hash = :hash)")
    suspend fun existsByContentHash(hash: String): Boolean

    @Query("SELECT * FROM attachments ORDER BY captured_timestamp DESC")
    fun getAllAttachmentsFlow(): Flow<List<AttachmentEntity>>

    @Query("SELECT * FROM attachments WHERE mime_type LIKE :mimeTypePrefix || '%' ORDER BY captured_timestamp DESC")
    fun getAttachmentsByTypeFlow(mimeTypePrefix: String): Flow<List<AttachmentEntity>>

    @Query("SELECT * FROM attachments WHERE package_name = :packageName ORDER BY captured_timestamp DESC")
    fun getAttachmentsByPackageFlow(packageName: String): Flow<List<AttachmentEntity>>

    @Query("SELECT * FROM attachments WHERE message_id = :messageId")
    fun getAttachmentsForMessageFlow(messageId: Long): Flow<List<AttachmentEntity>>

    @Query("SELECT * FROM attachments WHERE thread_id = :threadId ORDER BY captured_timestamp DESC")
    fun getAttachmentsForThreadFlow(threadId: String): Flow<List<AttachmentEntity>>

    @Query("UPDATE attachments SET message_id = :messageId WHERE id = :attachmentId")
    suspend fun linkAttachmentToMessage(attachmentId: Long, messageId: Long)

    @Query("SELECT * FROM attachments WHERE message_id IS NULL AND package_name = :packageName AND captured_timestamp BETWEEN :startTime AND :endTime ORDER BY captured_timestamp DESC LIMIT 1")
    suspend fun findUnlinkedAttachmentNearTime(
        packageName: String,
        startTime: Long,
        endTime: Long
    ): AttachmentEntity?

    @Query("DELETE FROM attachments WHERE id = :id")
    suspend fun deleteAttachmentById(id: Long)
}
