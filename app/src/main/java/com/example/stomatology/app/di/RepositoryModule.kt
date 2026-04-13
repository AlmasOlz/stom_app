package com.example.stomatology.app.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// We use fully qualified names here to guarantee Hilt finds them
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAppRepository(
        appRepositoryImpl: com.example.stomatology.app.data.repository.AppRepositoryImpl
    ): com.example.stomatology.app.domain.repository.AppRepository
}