package com.example.messagerecovery.di

import com.example.messagerecovery.data.repository.MessageRepositoryImpl
import com.example.messagerecovery.data.repository.MediaVaultRepositoryImpl
import com.example.messagerecovery.domain.repository.MediaVaultRepository
import com.example.messagerecovery.domain.repository.MessageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMessageRepository(
        impl: MessageRepositoryImpl
    ): MessageRepository

    @Binds
    @Singleton
    abstract fun bindMediaVaultRepository(
        impl: MediaVaultRepositoryImpl
    ): MediaVaultRepository
}
