package com.example.stomatology.app.domain.model

import com.example.stomatology.app.core.booking.BookingDefaults


enum class AppointmentStatus {
    PENDING,
    CONFIRMED,
    COMPLETED,
    CANCELLED,
    NO_SHOW,
    RESCHEDULED;

    companion object {
        fun fromStorage(value: String?): AppointmentStatus {
            return when (value?.trim()?.uppercase()) {
                "PENDING" -> PENDING
                "CONFIRMED", "ACCEPTED" -> CONFIRMED
                "COMPLETED" -> COMPLETED
                "CANCELLED", "REJECTED" -> CANCELLED
                "NO_SHOW" -> NO_SHOW
                "RESCHEDULED" -> RESCHEDULED
                else -> PENDING
            }
        }
    }
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
    val duration: String = BookingDefaults.DEFAULT_DURATION,
    val status: AppointmentStatus = AppointmentStatus.PENDING,
    val cancelledBy: String? = null,
    val cancelReason: String? = null,
    val cancelledAt: Long? = null,
    val completedAt: Long? = null,
    val rescheduledFromId: String? = null,
    val previousDate: String? = null,
    val previousTime: String? = null,
    val statusChangedBy: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
