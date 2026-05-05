package com.example.stomatology.app.domain.repository

import com.example.stomatology.app.domain.model.Appointment
import com.example.stomatology.app.domain.model.AppointmentStatus
import com.example.stomatology.app.domain.model.AvailableSlot
import kotlinx.coroutines.flow.Flow

interface AppointmentRepository {
    fun getAppointmentsForDoctor(doctorId: String): Flow<List<Appointment>>

    fun getAppointmentsForPatient(patientId: String): Flow<List<Appointment>>

    suspend fun createAppointment(
        appointment: Appointment,
        slotCapacity: Int = 1
    )

    suspend fun updateStatus(
        appointmentId: String,
        status: AppointmentStatus,
        changedBy: String,
        reason: String? = null,
        rescheduledFromId: String? = null
    )

    suspend fun generateAvailableSlots(
        doctorId: String,
        clinicId: String,
        date: String
    ): List<AvailableSlot>

    suspend fun rescheduleAppointment(
        appointmentId: String,
        newDate: String,
        newTime: String,
        changedBy: String,
        reason: String? = null
    )

    suspend fun markNoShow(
        appointmentId: String,
        changedBy: String,
        reason: String? = null
    )

    suspend fun cancelAppointmentWithSlotRelease(
        appointmentId: String,
        changedBy: String,
        reason: String
    )
}

class SlotAlreadyBookedException(
    message: String = "Бұл уақыт бос емес"
) : IllegalStateException(message)

class AppointmentValidationException(
    message: String
) : IllegalArgumentException(message)
