package com.example.messagerecovery.service

import com.example.messagerecovery.utils.MediaVaultCloner
import com.example.messagerecovery.utils.NotificationParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationParserTest {

    @Test
    fun whatsAppDeletionTriggers_correctlyIdentified() {
        val pkg = NotificationParser.PKG_WHATSAPP
        assertTrue(NotificationParser.isDeletionNotification(pkg, "This message was deleted"))
        assertTrue(NotificationParser.isDeletionNotification(pkg, "You deleted this message"))
        assertFalse(NotificationParser.isDeletionNotification(pkg, "Hey, what are you doing?"))
    }

    @Test
    fun whatsAppBusinessDeletionTriggers_correctlyIdentified() {
        val pkg = NotificationParser.PKG_WHATSAPP_BUSINESS
        assertTrue(NotificationParser.isDeletionNotification(pkg, "This message was deleted"))
        assertTrue(NotificationParser.isDeletionNotification(pkg, "You deleted this message"))
        assertFalse(NotificationParser.isDeletionNotification(pkg, "Your order has been confirmed."))
    }

    @Test
    fun messengerDeletionTriggers_correctlyIdentified() {
        val pkg = NotificationParser.PKG_MESSENGER
        assertTrue(NotificationParser.isDeletionNotification(pkg, "John unsent a message"))
        assertTrue(NotificationParser.isDeletionNotification(pkg, "This message was unsent"))
        assertFalse(NotificationParser.isDeletionNotification(pkg, "Are we meeting tomorrow?"))
    }

    @Test
    fun instagramDeletionTriggers_correctlyIdentified() {
        val pkg = NotificationParser.PKG_INSTAGRAM
        assertTrue(NotificationParser.isDeletionNotification(pkg, "alice unsent a message"))
        assertFalse(NotificationParser.isDeletionNotification(pkg, "Check out this reel"))
    }

    @Test
    fun instagramNoise_correctlyFiltered() {
        assertTrue(NotificationParser.isInstagramNoise("liked your message"))
        assertTrue(NotificationParser.isInstagramNoise("Active now"))
        assertTrue(NotificationParser.isInstagramNoise("started a live video"))
        assertTrue(NotificationParser.isInstagramNoise("mentioned you in a story"))
        assertFalse(NotificationParser.isInstagramNoise("Hey! How are you doing?"))
    }

    @Test
    fun whatsAppNoise_correctlyFiltered() {
        assertTrue(NotificationParser.isWhatsAppNoise("Checking for new messages"))
        assertTrue(NotificationParser.isWhatsAppNoise("Backup in progress"))
        assertTrue(NotificationParser.isWhatsAppNoise("Missed voice call"))
        assertTrue(NotificationParser.isWhatsAppNoise("WhatsApp Web is currently active"))
        assertTrue(NotificationParser.isWhatsAppNoise("3 new messages"))
        assertFalse(NotificationParser.isWhatsAppNoise("Hey are you coming today?"))
    }

    @Test
    fun messengerNoise_correctlyFiltered() {
        assertTrue(NotificationParser.isMessengerNoise("Missed call"))
        assertTrue(NotificationParser.isMessengerNoise("Incoming video call"))
        assertTrue(NotificationParser.isMessengerNoise("Active now"))
        assertTrue(NotificationParser.isMessengerNoise("reacted to your message"))
        assertFalse(NotificationParser.isMessengerNoise("Let's meet at 5pm"))
    }

    @Test
    fun isNoiseNotification_dispatchesProperly() {
        assertTrue(NotificationParser.isNoiseNotification(NotificationParser.PKG_WHATSAPP, "Checking for new messages"))
        assertTrue(NotificationParser.isNoiseNotification(NotificationParser.PKG_MESSENGER, "Missed call"))
        assertTrue(NotificationParser.isNoiseNotification(NotificationParser.PKG_INSTAGRAM, "liked your message"))
        assertTrue(NotificationParser.isNoiseNotification(NotificationParser.PKG_WHATSAPP, "2% (14m left)"))
        assertTrue(NotificationParser.isNoiseNotification(NotificationParser.PKG_WHATSAPP, "88 MB"))
        assertFalse(NotificationParser.isNoiseNotification(NotificationParser.PKG_WHATSAPP, "Hello!"))
    }

    @Test
    fun transferAndProgressText_correctlyIdentified() {
        assertTrue(NotificationParser.isTransferOrProgressText("2% (14m left)"))
        assertTrue(NotificationParser.isTransferOrProgressText("11% (2m left)"))
        assertTrue(NotificationParser.isTransferOrProgressText("88 MB"))
        assertTrue(NotificationParser.isTransferOrProgressText("49 MB"))
        assertTrue(NotificationParser.isTransferOrProgressText("2"))
        assertFalse(NotificationParser.isTransferOrProgressText("Hello, how are you?"))
    }

    @Test
    fun invalidTitles_correctlyIdentified() {
        assertTrue(NotificationParser.isInvalidTitle("88 MB"))
        assertTrue(NotificationParser.isInvalidTitle("Recents"))
        assertTrue(NotificationParser.isInvalidTitle("01"))
        assertTrue(NotificationParser.isInvalidTitle("2"))
        assertTrue(NotificationParser.isInvalidTitle("WhatsApp"))
        assertFalse(NotificationParser.isInvalidTitle("Haider TechSaSoft"))
        assertFalse(NotificationParser.isInvalidTitle("Bacteria 🦠 Zong"))
    }

    @Test
    fun mediaVaultCloner_resolvesMimeTypesCorrectly() {
        assertEquals("IMAGE", MediaVaultCloner.resolveMimeType("jpg"))
        assertEquals("IMAGE", MediaVaultCloner.resolveMimeType("PNG"))
        assertEquals("VIDEO", MediaVaultCloner.resolveMimeType("mp4"))
        assertEquals("AUDIO", MediaVaultCloner.resolveMimeType("opus"))
        assertEquals("AUDIO", MediaVaultCloner.resolveMimeType("m4a"))
        assertEquals("DOCUMENT", MediaVaultCloner.resolveMimeType("pdf"))
        assertEquals("UNKNOWN", MediaVaultCloner.resolveMimeType("xyz"))
    }
}
