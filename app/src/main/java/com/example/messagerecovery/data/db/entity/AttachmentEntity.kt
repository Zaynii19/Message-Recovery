package com.example.messagerecovery.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attachments",
    foreignKeys = [
        ForeignKey(
            entity = MessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["message_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["message_id"]),
        Index(value = ["thread_id"]),
        Index(value = ["package_name"]),
        Index(value = ["mime_type"]),
        Index(value = ["captured_timestamp"]),
        Index(value = ["content_hash"], unique = true)
    ]
)
data class AttachmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "message_id")
    val messageId: Long? = null,
    @ColumnInfo(name = "thread_id")
    val threadId: String? = null,
    @ColumnInfo(name = "package_name")
    val packageName: String,
    @ColumnInfo(name = "mime_type")
    val mimeType: String,
    @ColumnInfo(name = "vault_path")
    val vaultPath: String,
    @ColumnInfo(name = "file_size")
    val fileSize: Long,
    @ColumnInfo(name = "content_hash")
    val contentHash: String? = null,
    @ColumnInfo(name = "captured_timestamp")
    val capturedTimestamp: Long = System.currentTimeMillis()
)
