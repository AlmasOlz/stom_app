package com.example.stomatology.app.data.repository

import com.example.stomatology.app.domain.model.Appointment
import com.example.stomatology.app.domain.model.AppointmentStatus
import com.example.stomatology.app.domain.repository.AppointmentRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AppointmentRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AppointmentRepository {

    override fun getAppointmentsForDoctor(doctorId: String): Flow<List<Appointment>> {
        return firestore.collection("appointments")
            .whereEqualTo("doctorId", doctorId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Appointment::class.java)
            }
    }

    override fun getAppointmentsForPatient(patientId: String): Flow<List<Appointment>> {
        return firestore.collection("appointments")
            .whereEqualTo("patientId", patientId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Appointment::class.java)
            }
    }

    override suspend fun updateStatus(appointmentId: String, status: AppointmentStatus) {
        firestore.collection("appointments")
            .document(appointmentId)
            .update("status", status)
            .await()
    }
}
