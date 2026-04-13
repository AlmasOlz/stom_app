package com.example.stomatology.app.domain.model

data class Appointment(
    val id: String = "",
    val userId: String = "",
    val clinicId: String = "",
    val doctorName: String = "",
    val date: String = "",
    val time: String = "",
    val status: String = "Upcoming"
)