package com.example.messagerecovery.di

import android.content.Context
import androidx.room.Room
import com.example.messagerecovery.data.db.AppDatabase
import com.example.messagerecovery.data.db.dao.AttachmentDao
import com.example.messagerecovery.data.db.dao.MessageDao
import com.example.messagerecovery.data.db.dao.ThreadDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration(true)
        .build()
    }

    @Provides
    fun provideThreadDao(database: AppDatabase): ThreadDao = database.threadDao()

    @Provides
    fun provideMessageDao(database: AppDatabase): MessageDao = database.messageDao()

    @Provides
    fun provideAttachmentDao(database: AppDatabase): AttachmentDao = database.attachmentDao()
}
