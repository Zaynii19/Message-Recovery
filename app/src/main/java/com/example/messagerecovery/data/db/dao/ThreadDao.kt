package com.example.messagerecovery.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.messagerecovery.data.db.entity.ThreadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ThreadDao {

    @Upsert
    suspend fun upsertThread(thread: ThreadEntity)

    @Query("SELECT * FROM threads ORDER BY last_timestamp DESC")
    fun getThreadsFlow(): Flow<List<ThreadEntity>>

    @Query("SELECT * FROM threads WHERE package_name = :packageName ORDER BY last_timestamp DESC")
    fun getThreadsByPackageFlow(packageName: String): Flow<List<ThreadEntity>>

    @Query("SELECT * FROM threads WHERE id = :threadId")
    suspend fun getThreadById(threadId: String): ThreadEntity?

    @Query("UPDATE threads SET has_deleted_messages = 1 WHERE id = :threadId")
    suspend fun markThreadHasDeleted(threadId: String)

    @Query("UPDATE threads SET last_snippet = :snippet, last_timestamp = :timestamp WHERE id = :threadId")
    suspend fun updateLastSnippet(threadId: String, snippet: String, timestamp: Long)

    @Query("DELETE FROM threads WHERE id = :threadId")
    suspend fun deleteThreadById(threadId: String)

    @Query("SELECT * FROM threads")
    suspend fun getAllThreads(): List<ThreadEntity>

    @Query("DELETE FROM messages WHERE thread_id = :oldThreadId AND dedup_hash IN (SELECT dedup_hash FROM messages WHERE thread_id = :canonicalThreadId)")
    suspend fun deleteConflictingMessagesBeforeMerge(oldThreadId: String, canonicalThreadId: String)

    @Query("UPDATE messages SET thread_id = :canonicalThreadId WHERE thread_id = :oldThreadId")
    suspend fun reassignMessagesToThread(oldThreadId: String, canonicalThreadId: String)
}
