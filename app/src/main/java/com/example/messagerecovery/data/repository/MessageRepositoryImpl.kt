package com.example.messagerecovery.data.repository

import android.util.Log
import com.example.messagerecovery.data.db.dao.AttachmentDao
import com.example.messagerecovery.data.db.dao.MessageDao
import com.example.messagerecovery.data.db.dao.ThreadDao
import com.example.messagerecovery.data.db.entity.MessageEntity
import com.example.messagerecovery.data.db.entity.ThreadEntity
import com.example.messagerecovery.domain.deduplication.DeduplicationEngine
import com.example.messagerecovery.domain.model.ChatThread
import com.example.messagerecovery.domain.model.RecoveredMessage
import com.example.messagerecovery.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepositoryImpl @Inject constructor(
    private val threadDao: ThreadDao,
    private val messageDao: MessageDao,
    private val attachmentDao: AttachmentDao
) : MessageRepository {

    override fun getThreadsFlow(): Flow<List<ChatThread>> =
        threadDao.getThreadsFlow().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getThreadsByPackageFlow(packageName: String): Flow<List<ChatThread>> =
        threadDao.getThreadsByPackageFlow(packageName).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getMessagesForThreadFlow(threadId: String): Flow<List<RecoveredMessage>> =
        messageDao.getMessagesForThreadFlow(threadId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun recordIncomingMessage(
        thread: ChatThread,
        message: RecoveredMessage
    ): Boolean {
        // Step 1: Memory Deduplication Check
        if (DeduplicationEngine.isDuplicateOrRecord(message.dedupHash)) {
            Log.d(TAG, "Dropped duplicate message via in-memory LRU cache: hash=${message.dedupHash}")
            return false // Duplicate dropped
        }

        // Step 2: Atomic Thread Upsert
        threadDao.upsertThread(thread.toEntity())

        // Step 3: Insert Message with SQLite UNIQUE Conflict Guard
        val rowId = messageDao.insertMessage(message.toEntity())
        if (rowId == -1L) {
            Log.d(TAG, "SQLite UNIQUE conflict guard prevented duplicate row insertion: hash=${message.dedupHash}")
            return false // SQLite UNIQUE constraint prevented duplicate insertion
        }

        Log.d(TAG, "Recorded message in Room: rowId=$rowId, thread=[${thread.id}], sender='${message.senderName}', text=${message.rawText.take(40)}")

        // Step 4: Asynchronous Late-Binding with Orphaned Media (Proximity ±5s)
        val proximityWindow = 5000L
        val nearbyAttachment = attachmentDao.findUnlinkedAttachmentNearTime(
            packageName = thread.packageName,
            startTime = message.timestamp - proximityWindow,
            endTime = message.timestamp + proximityWindow
        )
        if (nearbyAttachment != null) {
            Log.d(TAG, "Correlated orphaned media attachment [id=${nearbyAttachment.id}] with message [$rowId]")
            attachmentDao.linkAttachmentToMessage(nearbyAttachment.id, rowId)
        }

        return true
    }

    override suspend fun recordDeletionEvent(
        packageName: String,
        threadId: String,
        senderName: String
    ): Boolean {
        // 24-hour backward matching window
        val windowStart = System.currentTimeMillis() - (24 * 60 * 60 * 1000L)

        val targetMessage = messageDao.findMostRecentUnflaggedMessage(
            threadId = threadId,
            senderName = senderName,
            windowStart = windowStart
        )
        if (targetMessage == null) {
            Log.w(TAG, "No unflagged message found to delete in thread [$threadId] for sender '$senderName' within 24h window")
            return false
        }

        val deletionTime = System.currentTimeMillis()
        messageDao.flagMessageAsDeleted(targetMessage.id, deletionTime)
        threadDao.markThreadHasDeleted(threadId)
        Log.w(TAG, "Flagged message [id=${targetMessage.id}] as DELETED in thread [$threadId] at $deletionTime")
        return true
    }

    companion object {
        private const val TAG = "MessageRepo"
    }

    override suspend fun deleteThread(threadId: String) {
        threadDao.deleteThreadById(threadId)
    }

    private fun ThreadEntity.toDomain() = ChatThread(
        id = id,
        packageName = packageName,
        displayName = displayName,
        avatarPath = avatarPath,
        lastSnippet = lastSnippet,
        lastTimestamp = lastTimestamp,
        hasDeletedMessages = hasDeletedMessages
    )

    private fun ChatThread.toEntity() = ThreadEntity(
        id = id,
        packageName = packageName,
        displayName = displayName,
        avatarPath = avatarPath,
        lastSnippet = lastSnippet,
        lastTimestamp = lastTimestamp,
        hasDeletedMessages = hasDeletedMessages
    )

    private fun MessageEntity.toDomain() = RecoveredMessage(
        id = id,
        threadId = threadId,
        dedupHash = dedupHash,
        senderName = senderName,
        rawText = rawText,
        timestamp = timestamp,
        isDeleted = isDeleted,
        hasAttachment = hasAttachment,
        deletionTimestamp = deletionTimestamp
    )

    private fun RecoveredMessage.toEntity() = MessageEntity(
        id = id,
        threadId = threadId,
        dedupHash = dedupHash,
        senderName = senderName,
        rawText = rawText,
        timestamp = timestamp,
        isDeleted = isDeleted,
        hasAttachment = hasAttachment,
        deletionTimestamp = deletionTimestamp
    )
}