package com.example.stomatology.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ClinicDao {

    @Query("SELECT * FROM clinics")
    fun getClinics(): Flow<List<ClinicEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ClinicEntity>)

    @Query("SELECT COUNT(*) FROM clinics")
    suspend fun count(): Int
}