package com.example.stomatology.app.data.repository

import com.example.stomatology.app.core.firebase.FirestoreCollections
import com.example.stomatology.app.core.firebase.FirestoreFields
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
        return firestore.collection(FirestoreCollections.APPOINTMENTS)
            .whereEqualTo(FirestoreFields.DOCTOR_ID, doctorId)
            .orderBy(FirestoreFields.CREATED_AT, Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Appointment::class.java)
            }
    }

    override fun getAppointmentsForPatient(patientId: String): Flow<List<Appointment>> {
        return firestore.collection(FirestoreCollections.APPOINTMENTS)
            .whereEqualTo(FirestoreFields.PATIENT_ID, patientId)
            .orderBy(FirestoreFields.CREATED_AT, Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Appointment::class.java)
            }
    }

    override suspend fun updateStatus(appointmentId: String, status: AppointmentStatus) {
        firestore.collection(FirestoreCollections.APPOINTMENTS)
            .document(appointmentId)
            .update(
                mapOf(
                    FirestoreFields.STATUS to status,
                    FirestoreFields.UPDATED_AT to System.currentTimeMillis()
                )
            )
            .await()
    }
}
