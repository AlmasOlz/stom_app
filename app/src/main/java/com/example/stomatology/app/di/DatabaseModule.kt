package com.example.stomatology.app.di

import android.content.Context
import androidx.room.Room
import com.example.stomatology.app.data.local.AppDatabase
import com.example.stomatology.app.data.local.ClinicDao
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
            "stomatology_db"
        )
            .fallbackToDestructiveMigration() // 🔥 ВОТ ЭТО
            .build()
    }

    @Provides
    @Singleton
    fun provideClinicDao(
        db: AppDatabase
    ): ClinicDao = db.clinicDao()
}