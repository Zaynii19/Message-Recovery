package com.example.messagerecovery.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object MediaExport {
    private const val TAG = "MediaExport"

    suspend fun exportToPublicStorage(
        context: Context,
        sourceFilePath: String,
        mimeType: String
    ): Result<Uri> = withContext(Dispatchers.IO) {
        val sourceFile = File(sourceFilePath)
        if (!sourceFile.exists()) {
            return@withContext Result.failure(IllegalArgumentException("Source file does not exist"))
        }

        val filename = sourceFile.name
        val resolver = context.contentResolver

        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, resolveRealMimeType(filename, mimeType))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val collectionUri: Uri = when {
                mimeType.startsWith("IMAGE", ignoreCase = true) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MessageRecovery")
                    }
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
                mimeType.startsWith("VIDEO", ignoreCase = true) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/MessageRecovery")
                    }
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }
                mimeType.startsWith("AUDIO", ignoreCase = true) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + "/MessageRecovery")
                    }
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                else -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/MessageRecovery")
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI
                    } else {
                        // Pre-Q fallback to direct file copy into Downloads directory
                        val destDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        val target = File(destDir, filename)
                        FileInputStream(sourceFile).use { input ->
                            FileOutputStream(target).use { output ->
                                input.copyTo(output)
                            }
                        }
                        return@withContext Result.success(Uri.fromFile(target))
                    }
                }
            }

            val itemUri = resolver.insert(collectionUri, contentValues)
                ?: return@withContext Result.failure(IllegalStateException("Failed to create MediaStore entry"))

            resolver.openOutputStream(itemUri)?.use { outputStream ->
                FileInputStream(sourceFile).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return@withContext Result.failure(IllegalStateException("Failed to open output stream for MediaStore"))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(itemUri, contentValues, null, null)
            }

            Log.d(TAG, "Successfully exported media to public storage: $filename -> $itemUri")
            Result.success(itemUri)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export media $sourceFilePath to public storage", e)
            Result.failure(e)
        }
    }

    private fun resolveRealMimeType(filename: String, category: String): String {
        val ext = filename.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            "mp4" -> "video/mp4"
            "3gp" -> "video/3gpp"
            "opus" -> "audio/opus"
            "m4a" -> "audio/mp4"
            "mp3" -> "audio/mpeg"
            "ogg" -> "audio/ogg"
            "pdf" -> "application/pdf"
            "doc", "docx" -> "application/msword"
            else -> when (category.uppercase()) {
                "IMAGE" -> "image/*"
                "VIDEO" -> "video/*"
                "AUDIO" -> "audio/*"
                else -> "application/octet-stream"
            }
        }
    }
}
