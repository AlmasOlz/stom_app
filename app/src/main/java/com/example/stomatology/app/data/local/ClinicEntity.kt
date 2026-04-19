package com.example.stomatology.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clinics")
data class ClinicEntity(
    @PrimaryKey val id: String,
    val name: String,
    val rating: Double,
    val reviews: Int,
    val address: String,
    val services: String,
    val imageUrl: String = "",
    val priceFrom: Int,
    val description: String
)