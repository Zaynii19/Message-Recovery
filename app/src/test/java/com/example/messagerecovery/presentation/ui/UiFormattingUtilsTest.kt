package com.example.messagerecovery.presentation.ui

import androidx.compose.ui.graphics.Color
import com.example.messagerecovery.presentation.ui.chatlist.getAppColor
import com.example.messagerecovery.presentation.ui.conversation.formatAudioTime
import com.example.messagerecovery.presentation.ui.conversation.formatFileSize
import com.example.messagerecovery.presentation.ui.conversation.resolveAppDisplayName
import org.junit.Assert.assertEquals
import org.junit.Test

class UiFormattingUtilsTest {

    @Test
    fun resolveAppDisplayName_returnsExpectedAppNames() {
        assertEquals("WhatsApp", resolveAppDisplayName("com.whatsapp"))
        assertEquals("WhatsApp Business", resolveAppDisplayName("com.whatsapp.w4b"))
        assertEquals("Messenger", resolveAppDisplayName("com.facebook.orca"))
        assertEquals("Instagram Direct", resolveAppDisplayName("com.instagram.android"))
        assertEquals("Message Recovery", resolveAppDisplayName("com.unknown.app"))
    }

    @Test
    fun formatAudioTime_formatsSecondsAndMinutesCorrectly() {
        assertEquals("0:00", formatAudioTime(0))
        assertEquals("0:05", formatAudioTime(5_000))
        assertEquals("0:59", formatAudioTime(59_000))
        assertEquals("1:00", formatAudioTime(60_000))
        assertEquals("1:15", formatAudioTime(75_000))
        assertEquals("10:05", formatAudioTime(605_000))
    }

    @Test
    fun formatFileSize_scalesAppropriately() {
        assertEquals("500 B", formatFileSize(500))
        assertEquals("1 KB", formatFileSize(1024))
        assertEquals("50 KB", formatFileSize(50 * 1024))
        assertEquals("1.0 MB", formatFileSize(1024 * 1024))
        assertEquals("2.5 MB", formatFileSize((2.5 * 1024 * 1024).toLong()))
    }

    @Test
    fun getAppColor_assignsCorrectBrandColors() {
        assertEquals(Color(0xFF25D366), getAppColor("com.whatsapp"))
        assertEquals(Color(0xFF00A884), getAppColor("com.whatsapp.w4b"))
        assertEquals(Color(0xFF0084FF), getAppColor("com.facebook.orca"))
        assertEquals(Color(0xFFE1306C), getAppColor("com.instagram.android"))
    }
}
