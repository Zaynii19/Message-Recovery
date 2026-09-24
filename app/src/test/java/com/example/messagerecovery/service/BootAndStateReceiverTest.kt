package com.example.messagerecovery.service

import android.content.Intent
import com.example.messagerecovery.receiver.BootAndStateReceiver
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BootAndStateReceiverTest {

    @Test
    fun resurrectionActions_containsAllRequiredIntents() {
        val actions = BootAndStateReceiver.RESURRECTION_ACTIONS

        assertTrue(actions.contains(Intent.ACTION_BOOT_COMPLETED))
        assertTrue(actions.contains(Intent.ACTION_LOCKED_BOOT_COMPLETED))
        assertTrue(actions.contains(Intent.ACTION_MY_PACKAGE_REPLACED))
        assertTrue(actions.contains(Intent.ACTION_POWER_CONNECTED))
        assertTrue(actions.contains("android.intent.action.QUICKBOOT_POWERON"))
        assertTrue(actions.contains("com.htc.intent.action.QUICKBOOT_POWERON"))
    }

    @Test
    fun resurrectionActions_ignoresUnrelatedIntents() {
        val actions = BootAndStateReceiver.RESURRECTION_ACTIONS

        assertFalse(actions.contains(Intent.ACTION_AIRPLANE_MODE_CHANGED))
        assertFalse(actions.contains(Intent.ACTION_SCREEN_ON))
        assertFalse(actions.contains(Intent.ACTION_BATTERY_LOW))
        assertFalse(actions.contains("com.random.custom.ACTION"))
    }
}
