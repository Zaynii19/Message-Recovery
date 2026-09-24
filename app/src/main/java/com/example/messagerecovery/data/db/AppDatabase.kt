package com.example.messagerecovery.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.messagerecovery.data.db.dao.AttachmentDao
import com.example.messagerecovery.data.db.dao.MessageDao
import com.example.messagerecovery.data.db.dao.ThreadDao
import com.example.messagerecovery.data.db.entity.AttachmentEntity
import com.example.messagerecovery.data.db.entity.MessageEntity
import com.example.messagerecovery.data.db.entity.ThreadEntity

@Database(
    entities = [
        ThreadEntity::class,
        MessageEntity::class,
        AttachmentEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun threadDao(): ThreadDao
    abstract fun messageDao(): MessageDao
    abstract fun attachmentDao(): AttachmentDao

    companion object {
        const val DATABASE_NAME = "message_recovery.db"
    }
}
