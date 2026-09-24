package com.example.messagerecovery.service

import android.app.Notification
import android.app.Person
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.os.BundleCompat
import com.example.messagerecovery.domain.deduplication.DeduplicationEngine
import com.example.messagerecovery.domain.model.ChatThread
import com.example.messagerecovery.domain.model.RecoveredMessage
import com.example.messagerecovery.domain.repository.MessageRepository
import com.example.messagerecovery.utils.NotificationParser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RecoveryNotificationListener : NotificationListenerService() {

    @Inject
    lateinit var messageRepository: MessageRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "RecoveryNotificationListener connected to Android notification system. Monitoring: $TARGET_PACKAGES")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.w(TAG, "RecoveryNotificationListener disconnected by Android OS! Requesting immediate rebind.")
        try {
            requestRebind(android.content.ComponentName(this, RecoveryNotificationListener::class.java))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request rebind on disconnect", e)
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn ?: return

        val packageName = sbn.packageName ?: return

        // Filter explicitly by target package names
        if (packageName !in TARGET_PACKAGES) {
            return
        }

        Log.d(TAG, "Incoming notification intercepted from target app: $packageName (key=${sbn.key})")

        serviceScope.launch {
            try {
                processNotification(sbn, packageName)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to process notification from $packageName", e)
            }
        }
    }

    private suspend fun processNotification(sbn: StatusBarNotification, packageName: String) {
        val notification = sbn.notification ?: return

        // 1. Reject ongoing notifications (media upload/download progress, active calls, backups)
        if (sbn.isOngoing || (notification.flags and Notification.FLAG_ONGOING_EVENT) != 0) {
            Log.d(TAG, "Dropped ongoing notification from $packageName (isOngoing=${sbn.isOngoing})")
            return
        }

        // 2. Reject notifications with progress indicators (file transfer progress)
        val extras = notification.extras ?: return
        /*if (extras.containsKey(Notification.EXTRA_PROGRESS) ||
            extras.getInt(Notification.EXTRA_PROGRESS_MAX, 0) > 0
        ) {
            Log.d(TAG, "Dropped progress notification from $packageName")
            return
        }*/

        // 3. Gate on notification category (reject progress, service, call, etc.)
        if (!NotificationParser.isAllowedCategory(notification.category)) {
            Log.d(TAG, "Dropped notification with non-message category: ${notification.category} for $packageName")
            return
        }

        // Extract base titles and text safely as CharSequence
        val rawConvTitle = extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString()?.trim()
        val rawTitle = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim()
            ?: extras.getCharSequence("android.title.big")?.toString()?.trim()
            ?: "Unknown"

        val conversationTitle = if (!rawConvTitle.isNullOrEmpty()) rawConvTitle else rawTitle

        // Attempt 1: AndroidX MessagingStyle extraction
        val messagingStyle = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification)
        if (messagingStyle != null && messagingStyle.messages.isNotEmpty()) {
            val styleTitle = messagingStyle.conversationTitle?.toString()?.trim()
                ?: conversationTitle
            val threadTitle = if (NotificationParser.isGenericAppName(styleTitle)) {
                messagingStyle.messages.firstOrNull()?.person?.name?.toString()?.trim() ?: styleTitle
            } else styleTitle

            for (msg in messagingStyle.messages) {
                val rawText = msg.text?.toString()?.trim() ?: ""
                val sender = msg.person?.name?.toString()?.trim()
                    ?: threadTitle
                val timestamp = if (msg.timestamp > 0) msg.timestamp else sbn.postTime

                if (rawText.isEmpty()) continue
                if (NotificationParser.isNoiseNotification(packageName, rawText)) {
                    Log.d(TAG, "Filtered noise message in MessagingStyle for $packageName: $rawText")
                    continue
                }

                handleMessageOrDeletion(
                    packageName = packageName,
                    threadDisplayName = threadTitle,
                    senderName = sender,
                    rawText = rawText,
                    timestamp = timestamp,
                    hasAttachment = msg.dataUri != null
                )
            }
            return
        }

        // Attempt 2: Framework EXTRA_MESSAGES parcels (Native WhatsApp/AOSP MessagingStyle)
        val frameworkMessages = extractFrameworkMessages(extras, conversationTitle, sbn.postTime)
        if (frameworkMessages.isNotEmpty()) {
            val threadTitle = if (NotificationParser.isGenericAppName(conversationTitle)) {
                frameworkMessages.first().senderName
            } else conversationTitle

            for (msg in frameworkMessages) {
                if (NotificationParser.isNoiseNotification(packageName, msg.text)) {
                    Log.d(TAG, "Filtered noise message in framework messages for $packageName: ${msg.text}")
                    continue
                }

                handleMessageOrDeletion(
                    packageName = packageName,
                    threadDisplayName = threadTitle,
                    senderName = msg.senderName,
                    rawText = msg.text,
                    timestamp = msg.timestamp,
                    hasAttachment = msg.hasAttachment
                )
            }
            return
        }

        // Attempt 3: InboxStyle EXTRA_TEXT_LINES (Older bundles or group summaries)
        val textLines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
        if (!textLines.isNullOrEmpty()) {
            for (line in textLines) {
                val lineStr = line?.toString()?.trim() ?: continue
                if (lineStr.isEmpty()) continue
                val (lineSender, lineMsg) = NotificationParser.extractSenderAndMessage(conversationTitle, lineStr)
                if (lineMsg.isEmpty() || NotificationParser.isNoiseNotification(packageName, lineMsg)) {
                    continue
                }

                handleMessageOrDeletion(
                    packageName = packageName,
                    threadDisplayName = if (NotificationParser.isGenericAppName(conversationTitle)) lineSender else conversationTitle,
                    senderName = lineSender,
                    rawText = lineMsg,
                    timestamp = sbn.postTime,
                    hasAttachment = false
                )
            }
            return
        }

        // Attempt 4: Fallback Single Notification (Instagram Direct, Messenger, Standard notifications)
        val rawText = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?: extras.getCharSequence(Notification.EXTRA_TEXT)
            ?: extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT))?.toString()?.trim() ?: ""

        if (rawText.isEmpty()) {
            return
        }

        // Parse Sender & Message (handles "Ali: hello" when title is "WhatsApp")
        val (finalSender, finalText) = NotificationParser.extractSenderAndMessage(conversationTitle, rawText)

        if (NotificationParser.isInvalidTitle(finalSender)) {
            Log.d(TAG, "Dropped fallback notification with invalid sender: '$finalSender' for $packageName")
            return
        }

        if (NotificationParser.isNoiseNotification(packageName, finalText)) {
            Log.d(TAG, "Filtered notification noise in fallback for $packageName: $finalText")
            return
        }

        handleMessageOrDeletion(
            packageName = packageName,
            threadDisplayName = finalSender,
            senderName = finalSender,
            rawText = finalText,
            timestamp = sbn.postTime,
            hasAttachment = false
        )
    }

    private fun extractFrameworkMessages(
        extras: Bundle,
        defaultSender: String,
        postTime: Long
    ): List<ExtractedFrameworkMessage> {
        val result = mutableListOf<ExtractedFrameworkMessage>()
        val messagesArray = BundleCompat.getParcelableArray(extras, Notification.EXTRA_MESSAGES, Parcelable::class.java)
            ?: return result

        for (item in messagesArray) {
            if (item is Bundle) {
                val text = item.getCharSequence("text")?.toString()?.trim()
                if (text.isNullOrEmpty()) continue

                val personSender = BundleCompat.getParcelable(item, "sender_person", Person::class.java)?.name?.toString()?.trim()

                val rawSender = personSender
                    ?: item.getCharSequence("sender")?.toString()?.trim()
                    ?: defaultSender

                val resolvedSender = if (rawSender.isBlank() || NotificationParser.isGenericAppName(rawSender)) {
                    defaultSender
                } else {
                    rawSender
                }

                val time = item.getLong("time", postTime).let { if (it > 0) it else postTime }
                val uri = BundleCompat.getParcelable(item, "uri", Uri::class.java)
                    ?: item.getString("data_uri")

                result.add(
                    ExtractedFrameworkMessage(
                        senderName = resolvedSender,
                        text = text,
                        timestamp = time,
                        hasAttachment = uri != null
                    )
                )
            }
        }
        return result
    }

    private data class ExtractedFrameworkMessage(
        val senderName: String,
        val text: String,
        val timestamp: Long,
        val hasAttachment: Boolean
    )

    private suspend fun handleMessageOrDeletion(
        packageName: String,
        threadDisplayName: String,
        senderName: String,
        rawText: String,
        timestamp: Long,
        hasAttachment: Boolean
    ) {
        val cleanThreadTitle = if (NotificationParser.isGenericAppName(threadDisplayName)) senderName else threadDisplayName
        if (cleanThreadTitle.isBlank() || NotificationParser.isInvalidTitle(cleanThreadTitle)) {
            Log.d(TAG, "Skipping message with invalid thread title: '$cleanThreadTitle'")
            return
        }

        val threadId = "${packageName}_$cleanThreadTitle"

        if (isDeletionNotification(packageName, rawText)) {
            Log.d(TAG, "Unsend/Deletion detected in $packageName for sender '$senderName'. Invoking recordDeletionEvent on thread [$threadId]")
            messageRepository.recordDeletionEvent(
                packageName = packageName,
                threadId = threadId,
                senderName = senderName
            )
        } else {
            recordMessage(
                packageName = packageName,
                threadId = threadId,
                displayName = cleanThreadTitle,
                senderName = senderName,
                rawText = rawText,
                timestamp = timestamp,
                hasAttachment = hasAttachment
            )
        }
    }

    private suspend fun recordMessage(
        packageName: String,
        threadId: String,
        displayName: String,
        senderName: String,
        rawText: String,
        timestamp: Long,
        hasAttachment: Boolean
    ) {
        val dedupHash = DeduplicationEngine.computeHash(
            packageName = packageName,
            threadId = threadId,
            senderName = senderName,
            rawText = rawText
        )

        Log.d(TAG, "Persisting message: pkg=$packageName, sender='$senderName', hash=$dedupHash, text=${rawText.take(40)}")

        val thread = ChatThread(
            id = threadId,
            packageName = packageName,
            displayName = displayName,
            lastSnippet = rawText,
            lastTimestamp = timestamp
        )

        val message = RecoveredMessage(
            threadId = threadId,
            dedupHash = dedupHash,
            senderName = senderName,
            rawText = rawText,
            timestamp = timestamp,
            hasAttachment = hasAttachment
        )

        val isNew = messageRepository.recordIncomingMessage(thread, message)
        if (isNew) {
            Log.d(TAG, "Message successfully logged into database for thread [$threadId]")
        } else {
            Log.d(TAG, "Duplicate message ignored by deduplication engine: hash=$dedupHash")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.w(TAG, "RecoveryNotificationListener onDestroy called")
        serviceScope.cancel()
    }

    companion object {
        private const val TAG = "RecoveryNotification"

        const val PKG_WHATSAPP = NotificationParser.PKG_WHATSAPP
        const val PKG_WHATSAPP_BUSINESS = NotificationParser.PKG_WHATSAPP_BUSINESS
        const val PKG_MESSENGER = NotificationParser.PKG_MESSENGER
        const val PKG_INSTAGRAM = NotificationParser.PKG_INSTAGRAM

        val TARGET_PACKAGES = NotificationParser.TARGET_PACKAGES

        fun isDeletionNotification(packageName: String, text: String): Boolean =
            NotificationParser.isDeletionNotification(packageName, text)
    }
}
