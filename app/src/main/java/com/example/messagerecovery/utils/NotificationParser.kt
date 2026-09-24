package com.example.messagerecovery.utils

import android.app.Notification

object NotificationParser {
    const val PKG_WHATSAPP = "com.whatsapp"
    const val PKG_WHATSAPP_BUSINESS = "com.whatsapp.w4b"
    const val PKG_MESSENGER = "com.facebook.orca"
    const val PKG_INSTAGRAM = "com.instagram.android"

    val TARGET_PACKAGES = setOf(
        PKG_WHATSAPP,
        PKG_WHATSAPP_BUSINESS,
        PKG_MESSENGER,
        PKG_INSTAGRAM
    )

    private val PROGRESS_PERCENT_REGEX = Regex("""^\d+%\s*\(.*left\)$""", RegexOption.IGNORE_CASE)
    private val FILE_SIZE_REGEX = Regex("""^\d+(\.\d+)?\s*(MB|KB|GB|B|bytes)$""", RegexOption.IGNORE_CASE)
    private val PURE_NUMBER_REGEX = Regex("""^\d+$""")

    fun isTransferOrProgressText(text: String): Boolean {
        val trimmed = text.trim()
        return PROGRESS_PERCENT_REGEX.matches(trimmed) ||
                FILE_SIZE_REGEX.matches(trimmed) ||
                PURE_NUMBER_REGEX.matches(trimmed)
    }

    fun isGenericAppName(title: String): Boolean {
        val lower = title.trim().lowercase()
        return lower == "whatsapp" ||
                lower == "whatsapp business" ||
                lower == "messenger" ||
                lower == "instagram"
    }

    fun extractSenderAndMessage(title: String, text: String): Pair<String, String> {
        val trimmedTitle = title.trim()
        val trimmedText = text.trim()

        if (isGenericAppName(trimmedTitle)) {
            val colonIndex = trimmedText.indexOf(':')
            if (colonIndex in 1..<40) {
                val potentialSender = trimmedText.substring(0, colonIndex).trim()
                val potentialMsg = trimmedText.substring(colonIndex + 1).trim()
                if (potentialSender.isNotEmpty() && potentialMsg.isNotEmpty() && !isInvalidTitle(potentialSender)) {
                    return Pair(potentialSender, potentialMsg)
                }
            }
        }
        return Pair(trimmedTitle, trimmedText)
    }

    fun isInvalidTitle(title: String): Boolean {
        val trimmed = title.trim()
        val lower = trimmed.lowercase()
        return trimmed.isBlank() ||
                FILE_SIZE_REGEX.matches(trimmed) ||
                PROGRESS_PERCENT_REGEX.matches(trimmed) ||
                PURE_NUMBER_REGEX.matches(trimmed) ||
                lower == "recents" ||
                isGenericAppName(trimmed) ||
                lower == "backup" ||
                lower.startsWith("backup ") ||
                lower == "checking for new messages"
    }

    fun isDeletionNotification(packageName: String, text: String): Boolean {
        val lower = text.lowercase()
        return when (packageName) {
            PKG_WHATSAPP, PKG_WHATSAPP_BUSINESS -> {
                lower.contains("this message was deleted") ||
                        lower.contains("you deleted this message")
            }
            PKG_MESSENGER -> {
                lower.contains("unsent a message") ||
                        lower.contains("this message was unsent")
            }
            PKG_INSTAGRAM -> {
                lower.contains("unsent a message")
            }
            else -> false
        }
    }

    fun isWhatsAppNoise(text: String): Boolean {
        val lower = text.lowercase()
        return isTransferOrProgressText(text) ||
                lower.contains("checking for new messages") ||
                lower.contains("backup in progress") ||
                lower.contains("backing up") ||
                lower.contains("backup paused") ||
                lower.contains("waiting for this message") ||
                lower.contains("you may have new messages") ||
                lower.contains("incoming voice call") ||
                lower.contains("incoming video call") ||
                lower.contains("missed voice call") ||
                lower.contains("missed video call") ||
                lower.contains("missed group call") ||
                lower.contains("ongoing call") ||
                lower.contains("call ended") ||
                lower.contains("calling...") ||
                lower.contains("whatsapp web") ||
                lower.contains("messages and calls are end-to-end encrypted") ||
                lower.contains("connecting to whatsapp") ||
                lower.contains("restoring chat history") ||
                lower.contains("deleting messages") ||
                lower.matches(Regex("""^\d+\s+new\s+messages?$"""))
    }

    fun isMessengerNoise(text: String): Boolean {
        val lower = text.lowercase()
        return isTransferOrProgressText(text) ||
                lower.contains("incoming call") ||
                lower.contains("incoming video call") ||
                lower.contains("missed call") ||
                lower.contains("video chat") ||
                lower.contains("ended the call") ||
                lower.contains("calling...") ||
                lower.contains("active now") ||
                lower.contains("chat heads active") ||
                lower.contains("reacted to your message") ||
                lower.contains("reacted to")
    }

    fun isInstagramNoise(text: String): Boolean {
        val lower = text.lowercase()
        return isTransferOrProgressText(text) ||
                lower.contains("liked your message") ||
                lower.contains("liked a message") ||
                lower.contains("reacted to your message") ||
                lower.contains("reacted ") ||
                lower.contains("active now") ||
                lower.contains("started a live video") ||
                lower.contains("shared a live video") ||
                lower.contains("sent an attachment") ||
                lower.contains("mentioned you in a story") ||
                lower.contains("mentioned you in a post") ||
                lower.contains("mentioned you in a comment") ||
                lower.contains("started following you") ||
                lower.contains("requested to follow you") ||
                lower.contains("shared a post") ||
                lower.contains("shared a story") ||
                lower.contains("sent a reel") ||
                lower.contains("sent a post") ||
                lower.contains("incoming audio call") ||
                lower.contains("incoming video call") ||
                lower.contains("missed call")
    }

    fun isNoiseNotification(packageName: String, text: String): Boolean {
        if (text.isBlank()) return true
        if (isTransferOrProgressText(text)) return true
        return when (packageName) {
            PKG_WHATSAPP, PKG_WHATSAPP_BUSINESS -> isWhatsAppNoise(text)
            PKG_MESSENGER -> isMessengerNoise(text)
            PKG_INSTAGRAM -> isInstagramNoise(text)
            else -> false
        }
    }

    fun isAllowedCategory(category: String?): Boolean {
        if (category == null) return true // Preserve notifications where category was omitted by host app
        return when (category) {
            Notification.CATEGORY_SERVICE,
            Notification.CATEGORY_PROGRESS,
            Notification.CATEGORY_STATUS,
            Notification.CATEGORY_SYSTEM,
            Notification.CATEGORY_RECOMMENDATION,
            Notification.CATEGORY_PROMO,
            Notification.CATEGORY_EVENT,
            Notification.CATEGORY_ALARM,
            Notification.CATEGORY_NAVIGATION,
            Notification.CATEGORY_WORKOUT,
            Notification.CATEGORY_LOCATION_SHARING,
            Notification.CATEGORY_STOPWATCH,
            Notification.CATEGORY_CALL -> false
            else -> true
        }
    }
}