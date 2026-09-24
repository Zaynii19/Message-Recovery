package com.example.messagerecovery.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.messagerecovery.R
import com.example.messagerecovery.utils.PermissionManager
import com.example.messagerecovery.utils.ServiceWatchdog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

@AndroidEntryPoint
class WatchdogForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "WatchdogForegroundService created. Armed as master supervisor.")
        startSilentForegroundNotification()
        startSupervisedServices()
        startPeriodicWatchdogTicker()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "WatchdogForegroundService received onStartCommand. Re-evaluating supervised services.")
        // Trigger an immediate check whenever a command/broadcast arrives
        serviceScope.launch {
            checkAndRebindServices()
        }
        return START_STICKY
    }

    private fun startSilentForegroundNotification() {
        val channelId = "watchdog_immortality_channel"
        val channelName = "System Integrity Service"

        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_MIN
        ).apply {
            description = "Keeps message recovery engine persistent in background"
            setShowBadge(false)
            enableVibration(false)
            setSound(null, null)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Message Recovery Active")
            .setContentText("Background integrity engine running")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .setSilent(true)
            .build()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    2001,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(2001, notification)
            }
            Log.d(TAG, "WatchdogForegroundService promoted to foreground successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to startForeground: ${e.message}", e)
            stopSelf()
        }
    }

    override fun onTimeout(startId: Int, fgsType: Int) {
        Log.w(TAG, "WatchdogForegroundService onTimeout triggered for Type $fgsType (startId $startId). Stopping gracefully.")
        stopSelf(startId)
    }

    private fun startSupervisedServices() {
        if (PermissionManager.isAllFilesAccessGranted(this)) {
            val mediaIntent = Intent(this, MediaFileObserverService::class.java)
            startService(mediaIntent)
        }
    }

    private fun startPeriodicWatchdogTicker() {
        serviceScope.launch {
            while (isActive) {
                delay(15.minutes) // 15 Minutes
                checkAndRebindServices()
            }
        }
    }

    private fun checkAndRebindServices() {
        Log.d(TAG, "Watchdog supervisor running health check and rebind routine...")
        // Ensure MediaFileObserverService is running if storage access is available
        if (PermissionManager.isAllFilesAccessGranted(this)) {
            val mediaIntent = Intent(this, MediaFileObserverService::class.java)
            startService(mediaIntent)
        }

        // If Notification Listener is granted in settings, force re-bind to resurrect dropped IPC
        if (PermissionManager.isNotificationListenerEnabled(this)) {
            ServiceWatchdog.forceRebindNotificationListener(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.w(TAG, "WatchdogForegroundService onDestroy called")
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "WatchdogService"
    }
}
