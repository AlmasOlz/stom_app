package com.example.stomatology.app.data.repository

import com.example.stomatology.app.domain.model.Appointment
import com.example.stomatology.app.domain.model.AppointmentStatus
import com.example.stomatology.app.domain.repository.AppointmentRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AppointmentRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AppointmentRepository {

    override fun getAppointmentsForDoctor(doctorId: String): Flow<List<Appointment>> = callbackFlow {
        val listener = firestore.collection("appointments")
            .whereEqualTo("doctorId", doctorId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val appointments = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Appointment::class.java)
                }.orEmpty()

                trySend(appointments)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun updateStatus(
        appointmentId: String,
        status: AppointmentStatus
    ) {
        firestore.collection("appointments")
            .document(appointmentId)
            .update(
                mapOf(
                    "status" to status.name,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            .await()
    }
}