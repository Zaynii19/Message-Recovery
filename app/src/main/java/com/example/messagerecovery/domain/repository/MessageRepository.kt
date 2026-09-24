package com.example.messagerecovery.domain.repository

import com.example.messagerecovery.domain.model.ChatThread
import com.example.messagerecovery.domain.model.RecoveredMessage
import kotlinx.coroutines.flow.Flow

interface MessageRepository {

    fun getThreadsFlow(): Flow<List<ChatThread>>

    fun getThreadsByPackageFlow(packageName: String): Flow<List<ChatThread>>

    fun getMessagesForThreadFlow(threadId: String): Flow<List<RecoveredMessage>>

    suspend fun recordIncomingMessage(
        thread: ChatThread,
        message: RecoveredMessage
    ): Boolean

    suspend fun recordDeletionEvent(
        packageName: String,
        threadId: String,
        senderName: String
    ): Boolean

    suspend fun deleteThread(threadId: String)
}
