package com.example.stomatology.app.presentation.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stomatology.app.domain.model.Appointment
import com.example.stomatology.app.domain.model.AppointmentStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

data class BookingUiState(
    val clinicId: String = "",
    val selectedDate: String = "",
    val selectedTime: String = "",
    val doctorId: String = "",
    val doctorName: String = "",
    val clinicName: String = "",
    val direction: String = "",
    val duration: String = "1 час",
    val isLoading: Boolean = false,
    val isBooked: Boolean = false,
    val showSuccessDialog: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState

    fun initClinic(clinicId: String) {
        if (_uiState.value.clinicId.isBlank()) {
            _uiState.update {
                it.copy(
                    clinicId = clinicId,
                    clinicName = if (it.clinicName.isBlank()) "OneDent" else it.clinicName
                )
            }
        }
    }

    fun onDateSelected(date: String) {
        _uiState.update { it.copy(selectedDate = date, error = null) }
    }

    fun onTimeSelected(time: String) {
        _uiState.update { it.copy(selectedTime = time, error = null) }
    }

    fun onDoctorSelected(doctorId: String, doctorName: String) {
        _uiState.update {
            it.copy(
                doctorId = doctorId,
                doctorName = doctorName,
                error = null
            )
        }
    }

    fun onDoctorNameChange(value: String) {
        _uiState.update { it.copy(doctorName = value, error = null) }
    }

    fun onClinicNameChange(value: String) {
        _uiState.update { it.copy(clinicName = value, error = null) }
    }

    fun onDirectionChange(value: String) {
        _uiState.update { it.copy(direction = value, error = null) }
    }

    fun onDurationChange(value: String) {
        _uiState.update { it.copy(duration = value, error = null) }
    }

    fun confirmBooking() {
        val state = _uiState.value

        if (state.clinicId.isBlank()) {
            _uiState.update { it.copy(error = "Не выбрана клиника") }
            return
        }

        if (state.selectedDate.isBlank()) {
            _uiState.update { it.copy(error = "Выберите дату") }
            return
        }

        if (state.selectedTime.isBlank()) {
            _uiState.update { it.copy(error = "Выберите время") }
            return
        }

        if (state.doctorName.isBlank()) {
            _uiState.update { it.copy(error = "Введите имя врача") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val currentUser = auth.currentUser
            val patientId = currentUser?.uid

            if (patientId == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Пользователь не авторизован"
                    )
                }
                return@launch
            }

            val appointment = Appointment(
                id = UUID.randomUUID().toString(),
                patientId = patientId,
                patientName = currentUser.displayName ?: "",
                patientPhone = currentUser.phoneNumber ?: "",
                clinicId = state.clinicId,
                clinicName = state.clinicName.ifBlank { "OneDent" },
                doctorId = "doctor_1",
                doctorName = state.doctorName,
                service = state.direction,
                date = state.selectedDate,
                time = state.selectedTime,
                duration = state.duration,
                status = AppointmentStatus.PENDING,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            try {
                firestore.collection("appointments")
                    .document(appointment.id)
                    .set(appointment)
                    .await()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isBooked = true,
                        showSuccessDialog = true,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Не удалось записаться"
                    )
                }
            }
        }
    }

    fun dismissSuccessDialog() {
        _uiState.update { it.copy(showSuccessDialog = false) }
    }

    fun resetBookingState() {
        _uiState.value = BookingUiState()
    }
}