package com.example.messagerecovery.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.messagerecovery.service.RecoveryNotificationListener
import com.example.messagerecovery.service.WatchdogForegroundService

object ServiceWatchdog {
    private const val TAG = "ServiceWatchdog"

    /**
     * Requests Android's NotificationManager to establish or restore the bind
     * to RecoveryNotificationListener using the official Android API.
     */
    fun forceRebindNotificationListener(context: Context) {
        try {
            Log.d(TAG, "Requesting RecoveryNotificationListener rebind via official NotificationListenerService.requestRebind")
            val component = ComponentName(context, RecoveryNotificationListener::class.java)
            android.service.notification.NotificationListenerService.requestRebind(component)
            Log.d(TAG, "Successfully requested NotificationListener rebind")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request NotificationListener rebind", e)
        }
    }

    /**
     * Launches the master WatchdogForegroundService in the foreground.
     */
    fun startMasterWatchdog(context: Context) {
        try {
            Log.d(TAG, "Launching WatchdogForegroundService via ContextCompat.startForegroundService")
            val intent = Intent(context, WatchdogForegroundService::class.java)
            ContextCompat.startForegroundService(context, intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start WatchdogForegroundService", e)
        }
    }
}
