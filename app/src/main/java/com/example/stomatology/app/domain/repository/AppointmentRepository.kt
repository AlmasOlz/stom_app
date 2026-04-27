package com.example.stomatology.app.domain.repository

import com.example.stomatology.app.domain.model.Appointment
import com.example.stomatology.app.domain.model.AppointmentStatus
import kotlinx.coroutines.flow.Flow

interface AppointmentRepository {
    fun getAppointmentsForDoctor(doctorId: String): Flow<List<Appointment>>

    fun getAppointmentsForPatient(patientId: String): Flow<List<Appointment>>

    suspend fun updateStatus(
        appointmentId: String,
        status: AppointmentStatus
    )
}