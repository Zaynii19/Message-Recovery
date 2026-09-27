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
import com.example.messagerecovery.utils.NotificationParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepositoryImpl @Inject constructor(
    private val threadDao: ThreadDao,
    private val messageDao: MessageDao,
    private val attachmentDao: AttachmentDao
) : MessageRepository {

    init {
        CoroutineScope(Dispatchers.IO).launch {
            // Run once at startup to clean up any bad threads persisted before this fix
            consolidateRedundantThreads()
        }
    }

    private suspend fun consolidateRedundantThreads() {
        try {
            val allThreads = threadDao.getAllThreads()
            for (thread in allThreads) {
                val cleanTitle = NotificationParser.sanitizeThreadTitle(thread.displayName)
                val canonicalThreadId = "${thread.packageName}_$cleanTitle"
                if (thread.id != canonicalThreadId) {
                    Log.d(TAG, "Consolidating redundant thread [${thread.id}] into [$canonicalThreadId]")
                    // 1. Delete conflicting messages from old thread
                    threadDao.deleteConflictingMessagesBeforeMerge(thread.id, canonicalThreadId)
                    // 2. Ensure canonical thread exists
                    val existing = threadDao.getThreadById(canonicalThreadId)
                    if (existing == null) {
                        threadDao.upsertThread(thread.copy(id = canonicalThreadId, displayName = cleanTitle))
                    }
                    // 3. Reassign remaining messages to canonical thread
                    threadDao.reassignMessagesToThread(thread.id, canonicalThreadId)
                    // 4. Delete old redundant thread
                    threadDao.deleteThreadById(thread.id)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error consolidating redundant threads", e)
        }
    }

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
        // Step 1: Canonicalize thread ID/name to strip count-suffix variants like "Dev team (5 messages)"
        // This ensures threads are ALWAYS keyed by their clean name, preventing duplicate rows.
        val canonicalDisplayName = NotificationParser.sanitizeThreadTitle(thread.displayName)
        val canonicalThreadId = "${thread.packageName}_$canonicalDisplayName"
        val canonicalThread = if (canonicalThreadId != thread.id || canonicalDisplayName != thread.displayName) {
            Log.d(TAG, "Canonicalized thread: [${thread.id}] -> [$canonicalThreadId] (display: '${thread.displayName}' -> '$canonicalDisplayName')")
            thread.copy(id = canonicalThreadId, displayName = canonicalDisplayName)
        } else {
            thread
        }
        // Also point the message to the canonical thread
        val canonicalMessage = if (message.threadId != canonicalThreadId) {
            message.copy(threadId = canonicalThreadId)
        } else {
            message
        }

        // Step 2: Memory Deduplication Check
        if (DeduplicationEngine.isDuplicateOrRecord(canonicalMessage.dedupHash)) {
            Log.d(TAG, "Dropped duplicate message via in-memory LRU cache: hash=${canonicalMessage.dedupHash}")
            return false // Duplicate dropped
        }

        // Step 3: Atomic Thread Upsert (always uses canonical ID)
        threadDao.upsertThread(canonicalThread.toEntity())

        // Step 4: Insert Message with SQLite UNIQUE Conflict Guard
        val rowId = messageDao.insertMessage(canonicalMessage.toEntity())
        if (rowId == -1L) {
            Log.d(TAG, "SQLite UNIQUE conflict guard prevented duplicate row insertion: hash=${canonicalMessage.dedupHash}")
            return false // SQLite UNIQUE constraint prevented duplicate insertion
        }

        Log.d(TAG, "Recorded message in Room: rowId=$rowId, thread=[$canonicalThreadId], sender='${canonicalMessage.senderName}', text=${canonicalMessage.rawText.take(40)}")

        // Step 5: Asynchronous Late-Binding with Orphaned Media (Proximity ±5s)
        val proximityWindow = 5000L
        val nearbyAttachment = attachmentDao.findUnlinkedAttachmentNearTime(
            packageName = canonicalThread.packageName,
            startTime = canonicalMessage.timestamp - proximityWindow,
            endTime = canonicalMessage.timestamp + proximityWindow
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