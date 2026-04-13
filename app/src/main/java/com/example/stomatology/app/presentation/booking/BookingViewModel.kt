package com.example.stomatology.app.presentation.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.stomatology.app.domain.model.Appointment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _isBooked = MutableStateFlow(false)
    val isBooked: StateFlow<Boolean> = _isBooked

    fun bookAppointment(clinicId: String, doctorName: String, date: String, time: String) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            val appointment = Appointment(
                id = UUID.randomUUID().toString(),
                userId = userId,
                clinicId = clinicId,
                doctorName = doctorName,
                date = date,
                time = time
            )
            try {
                firestore.collection("appointments").document(appointment.id).set(appointment).await()
                _isBooked.value = true
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}