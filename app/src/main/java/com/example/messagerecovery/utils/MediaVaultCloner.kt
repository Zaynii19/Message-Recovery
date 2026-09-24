package com.example.messagerecovery.utils

import android.content.Context
import android.util.Log
import com.example.messagerecovery.domain.model.MediaAttachment
import com.example.messagerecovery.domain.repository.MediaVaultRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.UUID

object MediaVaultCloner {
    private const val TAG = "MediaVaultCloner"

    /**
     * Clones an external source file into the protected sandbox directory (context.filesDir/vault/)
     * immediately upon detection to guard against host app unsend/unlink operations.
     */
    suspend fun cloneToVault(
        context: Context,
        sourceFile: File,
        packageName: String,
        mediaVaultRepository: MediaVaultRepository
    ): MediaAttachment? = withContext(Dispatchers.IO) {
        if (!sourceFile.exists() || !sourceFile.canRead() || sourceFile.length() == 0L) {
            return@withContext null
        }

        try {
            val contentHash = computeFileSha256(sourceFile)
            if (mediaVaultRepository.existsByContentHash(contentHash)) {
                Log.d(TAG, "Skipping duplicate media file by SHA-256 hash: $contentHash (${sourceFile.name})")
                return@withContext null
            }

            val extension = sourceFile.extension.ifEmpty { "bin" }
            val mimeType = resolveMimeType(extension)

            // Ensure vault subdirectory exists for the target package
            val packageVaultDir = File(context.filesDir, "vault/$packageName")
            if (!packageVaultDir.exists()) {
                packageVaultDir.mkdirs()
            }

            // Generate unique filename in sandbox
            val destinationFile = File(packageVaultDir, "${UUID.randomUUID()}.$extension")

            Log.d(TAG, "Cloning incoming media payload from $packageName: ${sourceFile.name} ($mimeType, ${sourceFile.length()} bytes)")

            // Atomic stream copy
            FileInputStream(sourceFile).use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }

            val attachment = MediaAttachment(
                packageName = packageName,
                mimeType = mimeType,
                vaultPath = destinationFile.absolutePath,
                fileSize = destinationFile.length(),
                contentHash = contentHash,
                capturedTimestamp = System.currentTimeMillis()
            )

            val recordId = mediaVaultRepository.recordAttachment(attachment)
            if (recordId == -1L) {
                Log.d(TAG, "Media insert conflict guard triggered (hash=$contentHash). Deleting redundant vault clone.")
                if (destinationFile.exists()) destinationFile.delete()
                return@withContext null
            }
            Log.d(TAG, "Media successfully cloned to vault: ${destinationFile.name} [id=$recordId, size=${attachment.fileSize} bytes]")
            return@withContext attachment.copy(id = recordId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clone file ${sourceFile.absolutePath} to vault", e)
            return@withContext null
        }
    }

    fun computeFileSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(8192)
        FileInputStream(file).use { input ->
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        val hashBytes = digest.digest()
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun resolveMimeType(extension: String): String {
        return when (extension.lowercase()) {
            "jpg", "jpeg", "png", "webp", "gif" -> "IMAGE"
            "mp4", "mkv", "3gp", "avi" -> "VIDEO"
            "opus", "ogg", "mp3", "m4a", "wav", "aac" -> "AUDIO"
            "pdf", "doc", "docx", "xls", "xlsx", "zip" -> "DOCUMENT"
            else -> "UNKNOWN"
        }
    }
}