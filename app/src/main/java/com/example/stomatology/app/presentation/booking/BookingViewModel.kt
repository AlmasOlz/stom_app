package com.example.stomatology.app.presentation.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stomatology.app.domain.model.Appointment
import com.example.stomatology.app.domain.model.AppointmentStatus
import com.example.stomatology.app.domain.model.DoctorOption
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
    val error: String? = null,
    val availableTimeSlots: List<String> = emptyList(),
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    val doctors: List<DoctorOption> = emptyList(),
    val isDoctorMenuExpanded: Boolean = false
)

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState

    init {
        loadDoctors()
    }

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

    private fun loadDoctors() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users")
                    .whereEqualTo("role", "doctor")
                    .get()
                    .await()

                val doctors = snapshot.documents.mapNotNull { doc ->
                    val uid = doc.getString("uid") ?: doc.id
                    val firstName = doc.getString("firstName") ?: ""
                    val lastName = doc.getString("lastName") ?: ""
                    val displayName = doc.getString("displayName") ?: "$firstName $lastName".trim()

                    DoctorOption(
                        uid = uid,
                        firstName = firstName,
                        lastName = lastName,
                        displayName = displayName,
                        email = doc.getString("email") ?: "",
                        phone = doc.getString("phone") ?: ""
                    )
                }

                _uiState.update { it.copy(doctors = doctors) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Не удалось загрузить врачей")
                }
            }
        }
    }

    fun onShowDatePicker(show: Boolean) {
        _uiState.update { it.copy(showDatePicker = show) }
    }

    fun onShowTimePicker(show: Boolean) {
        _uiState.update { it.copy(showTimePicker = show) }
    }

    fun onDoctorMenuExpandedChange(expanded: Boolean) {
        _uiState.update { it.copy(isDoctorMenuExpanded = expanded) }
    }

    fun onDateSelected(date: String) {
        _uiState.update { it.copy(selectedDate = date, error = null) }
    }

    fun onTimeSelected(time: String) {
        _uiState.update { it.copy(selectedTime = time, error = null) }
    }

    fun onDoctorSelected(doctor: DoctorOption) {
        _uiState.update {
            it.copy(
                doctorId = doctor.uid,
                doctorName = doctor.name,
                isDoctorMenuExpanded = false,
                error = null
            )
        }
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

        if (state.doctorId.isBlank()) {
            _uiState.update { it.copy(error = "Выберите врача") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val currentUser = auth.currentUser
                    ?: throw IllegalStateException("Пользователь не авторизован")

                val uid = currentUser.uid

                val userSnapshot = firestore.collection("users")
                    .document(uid)
                    .get()
                    .await()

                val firstName = userSnapshot.getString("firstName") ?: ""
                val lastName = userSnapshot.getString("lastName") ?: ""
                val displayName = userSnapshot.getString("displayName")
                    ?: "$firstName $lastName".trim()

                val phone = userSnapshot.getString("phone") ?: ""

                val appointment = Appointment(
                    id = UUID.randomUUID().toString(),
                    patientId = uid,
                    patientName = displayName,
                    patientPhone = phone,
                    clinicId = state.clinicId,
                    clinicName = state.clinicName.ifBlank { "OneDent" },
                    doctorId = state.doctorId,
                    doctorName = state.doctorName,
                    service = state.direction,
                    date = state.selectedDate,
                    time = state.selectedTime,
                    duration = state.duration,
                    status = AppointmentStatus.PENDING,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

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