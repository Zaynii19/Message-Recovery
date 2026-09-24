package com.example.messagerecovery.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.messagerecovery.utils.PermissionManager
import com.example.messagerecovery.utils.ServiceWatchdog
class BootAndStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        val action = intent?.action ?: return

        Log.d(TAG, "BootAndStateReceiver received system broadcast: $action")

        if (action in RESURRECTION_ACTIONS) {
            Log.d(TAG, "Resurrection action matched [$action]! Restoring Watchdog and forcing NotificationListener re-bind.")
            // 1. Resurrect the Master Watchdog Service
            if (PermissionManager.isAllFilesAccessGranted(context)) {
                ServiceWatchdog.startMasterWatchdog(context)
            }

            // 2. Force re-bind NotificationListenerService
            if (PermissionManager.isNotificationListenerEnabled(context)) {
                ServiceWatchdog.forceRebindNotificationListener(context)
            }
        }
    }

    companion object {
        private const val TAG = "BootAndStateReceiver"
        val RESURRECTION_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_POWER_CONNECTED,
            Intent.ACTION_USER_PRESENT,
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON"
        )
    }
}
