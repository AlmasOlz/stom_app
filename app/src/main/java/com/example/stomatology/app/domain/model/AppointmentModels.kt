package com.example.stomatology.app.domain.model


enum class AppointmentStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    COMPLETED,
    CANCELLED
}

data class Appointment(
    val id: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val patientPhone: String = "",
    val clinicId: String = "",
    val clinicName: String = "",
    val doctorId: String = "",
    val doctorName: String = "",
    val service: String = "",
    val date: String = "",
    val time: String = "",
    val duration: String = "1 час",
    val status: AppointmentStatus = AppointmentStatus.PENDING,
    val rejectionReason: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)