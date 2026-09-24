package com.example.messagerecovery.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ThreadEntity::class,
            parentColumns = ["id"],
            childColumns = ["thread_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["thread_id"]),
        Index(value = ["dedup_hash"], unique = true),
        Index(value = ["timestamp"]),
        Index(value = ["thread_id", "is_deleted", "timestamp"]),
        Index(value = ["thread_id", "raw_text", "sender_name"], unique = true)
    ]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "thread_id")
    val threadId: String,
    @ColumnInfo(name = "dedup_hash")
    val dedupHash: String,
    @ColumnInfo(name = "sender_name")
    val senderName: String,
    @ColumnInfo(name = "raw_text")
    val rawText: String,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
    @ColumnInfo(name = "has_attachment")
    val hasAttachment: Boolean = false,
    @ColumnInfo(name = "deletion_timestamp")
    val deletionTimestamp: Long? = null
)
