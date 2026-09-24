package com.example.messagerecovery.service

import android.app.Service
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.FileObserver
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import com.example.messagerecovery.domain.repository.MediaVaultRepository
import com.example.messagerecovery.utils.MediaVaultCloner
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class MediaFileObserverService : Service() {

    @Inject
    lateinit var mediaVaultRepository: MediaVaultRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val activeObservers = mutableListOf<FileObserver>()
    private val watchedDirectories = ConcurrentHashMap.newKeySet<String>()
    private var mediaContentObserver: ContentObserver? = null
    private val recentlyProcessedPaths = ConcurrentHashMap<String, Long>()

    private fun shouldProcessPath(path: String): Boolean {
        val now = System.currentTimeMillis()
        val lastProcessed = recentlyProcessedPaths[path]
        if (lastProcessed != null && now - lastProcessed < DEBOUNCE_TTL_MS) {
            return false
        }
        recentlyProcessedPaths[path] = now

        // Periodic cleanup if map grows too large
        if (recentlyProcessedPaths.size > 500) {
            val cutoff = now - DEBOUNCE_TTL_MS
            recentlyProcessedPaths.entries.removeIf { it.value < cutoff }
        }
        return true
    }

    override fun onCreate() {
        super.onCreate()
        initializeDirectoryObservers()
        initializeMediaStoreObserver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun initializeDirectoryObservers() {
        val externalStorage = Environment.getExternalStorageDirectory()

        // 1. WhatsApp Paths (Scoped Storage + Legacy)
        val waBases = listOf(
            File(externalStorage, "Android/media/com.whatsapp/WhatsApp/Media"),
            File(externalStorage, "WhatsApp/Media")
        )
        for (waBase in waBases) {
            registerRecursiveDirectoryObserver(File(waBase, "WhatsApp Images"), RecoveryNotificationListener.PKG_WHATSAPP)
            registerRecursiveDirectoryObserver(File(waBase, "WhatsApp Video"), RecoveryNotificationListener.PKG_WHATSAPP)
            registerRecursiveDirectoryObserver(File(waBase, "WhatsApp Audio"), RecoveryNotificationListener.PKG_WHATSAPP)
            registerRecursiveDirectoryObserver(File(waBase, "WhatsApp Voice Notes"), RecoveryNotificationListener.PKG_WHATSAPP)
            registerRecursiveDirectoryObserver(File(waBase, "WhatsApp Documents"), RecoveryNotificationListener.PKG_WHATSAPP)
        }

        // 2. WhatsApp Business Paths (Scoped Storage + Legacy)
        val wabBases = listOf(
            File(externalStorage, "Android/media/com.whatsapp.w4b/WhatsApp Business/Media"),
            File(externalStorage, "WhatsApp Business/Media")
        )
        for (wabBase in wabBases) {
            registerRecursiveDirectoryObserver(File(wabBase, "WhatsApp Business Images"), RecoveryNotificationListener.PKG_WHATSAPP_BUSINESS)
            registerRecursiveDirectoryObserver(File(wabBase, "WhatsApp Business Video"), RecoveryNotificationListener.PKG_WHATSAPP_BUSINESS)
            registerRecursiveDirectoryObserver(File(wabBase, "WhatsApp Business Audio"), RecoveryNotificationListener.PKG_WHATSAPP_BUSINESS)
            registerRecursiveDirectoryObserver(File(wabBase, "WhatsApp Business Voice Notes"), RecoveryNotificationListener.PKG_WHATSAPP_BUSINESS)
            registerRecursiveDirectoryObserver(File(wabBase, "WhatsApp Business Documents"), RecoveryNotificationListener.PKG_WHATSAPP_BUSINESS)
        }

        // 3. Messenger Paths
        registerRecursiveDirectoryObserver(File(externalStorage, "Pictures/Messenger"), RecoveryNotificationListener.PKG_MESSENGER)
        registerRecursiveDirectoryObserver(File(externalStorage, "Movies/Messenger"), RecoveryNotificationListener.PKG_MESSENGER)
        registerRecursiveDirectoryObserver(File(externalStorage, "Download/Messenger"), RecoveryNotificationListener.PKG_MESSENGER)
        registerRecursiveDirectoryObserver(File(externalStorage, "Android/media/com.facebook.orca"), RecoveryNotificationListener.PKG_MESSENGER)

        // 4. Instagram Paths
        registerRecursiveDirectoryObserver(File(externalStorage, "Pictures/Instagram"), RecoveryNotificationListener.PKG_INSTAGRAM)
        registerRecursiveDirectoryObserver(File(externalStorage, "Movies/Instagram"), RecoveryNotificationListener.PKG_INSTAGRAM)
        registerRecursiveDirectoryObserver(File(externalStorage, "Download/Instagram"), RecoveryNotificationListener.PKG_INSTAGRAM)
        registerRecursiveDirectoryObserver(File(externalStorage, "Android/media/com.instagram.android"), RecoveryNotificationListener.PKG_INSTAGRAM)
    }

    private val OBSERVER_MASK = FileObserver.CLOSE_WRITE or FileObserver.MOVED_TO or FileObserver.CREATE

    @Suppress("DEPRECATION")
    private fun registerRecursiveDirectoryObserver(directory: File, packageName: String) {
        if (!directory.exists()) {
            directory.mkdirs()
        }

        watchDirectory(directory, packageName)

        // Also watch any existing subdirectories (e.g. WhatsApp Voice Notes/202638, WhatsApp Video/Private)
        directory.listFiles()?.forEach { file ->
            if (file.isDirectory && !file.name.startsWith(".")) {
                watchDirectory(file, packageName)
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun watchDirectory(directory: File, packageName: String) {
        val canonicalPath = try { directory.canonicalPath } catch (_: Exception) { directory.absolutePath }
        if (!watchedDirectories.add(canonicalPath)) {
            return // Already registered
        }

        val observer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            object : FileObserver(directory, OBSERVER_MASK) {
                override fun onEvent(event: Int, path: String?) {
                    handleFileEvent(directory, path, event, packageName)
                }
            }
        } else {
            object : FileObserver(directory.absolutePath, OBSERVER_MASK) {
                override fun onEvent(event: Int, path: String?) {
                    handleFileEvent(directory, path, event, packageName)
                }
            }
        }

        try {
            observer.startWatching()
            synchronized(activeObservers) {
                activeObservers.add(observer)
            }
            Log.d(TAG, "Watching media directory for $packageName: $canonicalPath")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start watching $canonicalPath", e)
        }
    }

    private fun handleFileEvent(directory: File, fileName: String?, event: Int, packageName: String) {
        fileName ?: return
        if (fileName.startsWith(".nomedia") || fileName.endsWith(".tmp")) return

        val targetFile = File(directory, fileName)

        // If a new subdirectory is created inside (e.g. WhatsApp Voice Notes creates 202638), watch it!
        if (targetFile.isDirectory) {
            if ((event and FileObserver.CREATE != 0) || (event and FileObserver.MOVED_TO != 0)) {
                watchDirectory(targetFile, packageName)
            }
            return
        }

        // Only process file modifications/writes or moves
        val isWriteOrMove = (event and FileObserver.CLOSE_WRITE != 0) || (event and FileObserver.MOVED_TO != 0)
        if (!isWriteOrMove) return

        if (!shouldProcessPath(targetFile.absolutePath)) {
            return
        }

        serviceScope.launch {
            // Wait for file download stabilization (prevents truncated/corrupted video & audio files)
            if (waitForFileStabilization(targetFile)) {
                MediaVaultCloner.cloneToVault(
                    context = this@MediaFileObserverService,
                    sourceFile = targetFile,
                    packageName = packageName,
                    mediaVaultRepository = mediaVaultRepository
                )
            }
        }
    }

    private suspend fun waitForFileStabilization(file: File): Boolean {
        if (!file.exists()) return false

        val extension = file.extension.lowercase()
        // Large media like videos, voice notes, audio, and documents take time to download
        val isHeavyMedia = extension in listOf(
            "mp4", "mkv", "3gp", "avi",
            "opus", "ogg", "mp3", "m4a", "wav",
            "pdf", "docx", "xlsx", "zip"
        )

        var prevSize = file.length()
        if (prevSize == 0L) {
            delay(300.milliseconds)
            prevSize = file.length()
            if (prevSize == 0L) return false
        }

        if (!isHeavyMedia) {
            return file.exists() && file.length() > 0L
        }

        // For video & audio: ensure file size is stable across a 500ms delay window
        var checks = 0
        while (checks < 10) {
            delay(500.milliseconds)
            val currentSize = file.length()
            if (currentSize == prevSize && currentSize > 0L) {
                return true
            }
            prevSize = currentSize
            checks++
        }
        return file.exists() && file.length() > 0L
    }

    private fun initializeMediaStoreObserver() {
        val handler = Handler(Looper.getMainLooper())
        mediaContentObserver = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                uri ?: return
                // Backup capture route when MediaStore emits new image/video creation
                serviceScope.launch {
                    processMediaStoreUri(uri)
                }
            }
        }

        contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            mediaContentObserver!!
        )
        contentResolver.registerContentObserver(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            true,
            mediaContentObserver!!
        )
        contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true,
            mediaContentObserver!!
        )
    }

    private fun resolvePackageFromPath(path: String): String? {
        val lower = path.lowercase()
        return when {
            lower.contains("com.whatsapp.w4b") || lower.contains("whatsapp business") -> RecoveryNotificationListener.PKG_WHATSAPP_BUSINESS
            lower.contains("com.whatsapp") || lower.contains("/whatsapp/") -> RecoveryNotificationListener.PKG_WHATSAPP
            lower.contains("com.facebook.orca") || lower.contains("/messenger/") -> RecoveryNotificationListener.PKG_MESSENGER
            lower.contains("com.instagram.android") || lower.contains("/instagram/") -> RecoveryNotificationListener.PKG_INSTAGRAM
            else -> null
        }
    }

    private suspend fun processMediaStoreUri(uri: Uri) {
        try {
            val projection = arrayOf(
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.OWNER_PACKAGE_NAME
            )
            contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val dataIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                    val pkgIndex = cursor.getColumnIndex(MediaStore.MediaColumns.OWNER_PACKAGE_NAME)

                    val path = if (dataIndex != -1) cursor.getString(dataIndex) else null
                    val pkg = if (pkgIndex != -1) cursor.getString(pkgIndex) else null

                    val resolvedPkg = (if (pkg in RecoveryNotificationListener.TARGET_PACKAGES) pkg else null)
                        ?: (if (path != null) resolvePackageFromPath(path) else null)

                    if (path != null && resolvedPkg != null) {
                        val file = File(path)
                        if (!shouldProcessPath(path)) {
                            return
                        }

                        if (waitForFileStabilization(file)) {
                            MediaVaultCloner.cloneToVault(
                                context = this@MediaFileObserverService,
                                sourceFile = file,
                                packageName = resolvedPkg,
                                mediaVaultRepository = mediaVaultRepository
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying MediaStore URI $uri", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.w(TAG, "MediaFileObserverService stopping all ${activeObservers.size} active observers")
        activeObservers.forEach { it.stopWatching() }
        activeObservers.clear()

        mediaContentObserver?.let {
            contentResolver.unregisterContentObserver(it)
        }
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "MediaObserverService"
        private const val DEBOUNCE_TTL_MS = 3000L
    }
}
