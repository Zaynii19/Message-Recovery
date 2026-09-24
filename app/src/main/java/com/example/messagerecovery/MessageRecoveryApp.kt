package com.example.messagerecovery

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import com.example.messagerecovery.utils.PermissionManager
import com.example.messagerecovery.utils.ServiceWatchdog
import dagger.hilt.android.HiltAndroidApp
import java.io.File

@HiltAndroidApp
class MessageRecoveryApp : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        // Ensure the private internal vault directory exists
        val vaultDir = File(filesDir, "vault")
        if (!vaultDir.exists()) {
            vaultDir.mkdirs()
        }

        // Start master watchdog if permissions are already established
        if (PermissionManager.isNotificationListenerEnabled(this) ||
            PermissionManager.isAllFilesAccessGranted(this)
        ) {
            ServiceWatchdog.startMasterWatchdog(this)
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .crossfade(true)
            .build()
    }
}

