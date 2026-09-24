package com.example.messagerecovery.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "threads")
data class ThreadEntity(
    @PrimaryKey
    val id: String, // Format: packageName_contactIdentifier (e.g. "com.whatsapp_+123456789")
    @ColumnInfo(name = "package_name")
    val packageName: String,
    @ColumnInfo(name = "display_name")
    val displayName: String,
    @ColumnInfo(name = "avatar_path")
    val avatarPath: String? = null,
    @ColumnInfo(name = "last_snippet")
    val lastSnippet: String? = null,
    @ColumnInfo(name = "last_timestamp")
    val lastTimestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "has_deleted_messages")
    val hasDeletedMessages: Boolean = false
)
