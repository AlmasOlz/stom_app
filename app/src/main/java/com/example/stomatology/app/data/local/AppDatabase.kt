package com.example.stomatology.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ClinicEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clinicDao(): ClinicDao
}