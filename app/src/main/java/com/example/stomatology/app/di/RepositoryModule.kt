package com.example.stomatology.app.di

import com.example.stomatology.app.data.repository.AppRepositoryImpl
import com.example.stomatology.app.data.repository.AppointmentRepositoryImpl
import com.example.stomatology.app.domain.repository.AppRepository
import com.example.stomatology.app.domain.repository.AppointmentRepository
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
    abstract fun bindAppRepository(
        impl: AppRepositoryImpl
    ): AppRepository

    @Binds
    @Singleton
    abstract fun bindAppointmentRepository(
        impl: AppointmentRepositoryImpl
    ): AppointmentRepository
}