package com.example.messagerecovery.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.messagerecovery.data.db.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Query("SELECT * FROM messages WHERE thread_id = :threadId ORDER BY timestamp ASC")
    fun getMessagesForThreadFlow(threadId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE thread_id = :threadId AND sender_name = :senderName AND is_deleted = 0 AND timestamp >= :windowStart ORDER BY timestamp DESC LIMIT 1")
    suspend fun findMostRecentUnflaggedMessage(
        threadId: String,
        senderName: String,
        windowStart: Long
    ): MessageEntity?

    @Query("UPDATE messages SET is_deleted = 1, deletion_timestamp = :deletionTime WHERE id = :messageId")
    suspend fun flagMessageAsDeleted(messageId: Long, deletionTime: Long)

    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: Long): MessageEntity?

    @Query("SELECT COUNT(*) FROM messages WHERE is_deleted = 1")
    fun getDeletedMessagesCountFlow(): Flow<Int>
}
