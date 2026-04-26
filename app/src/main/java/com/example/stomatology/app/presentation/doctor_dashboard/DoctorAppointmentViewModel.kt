package com.example.stomatology.app.presentation.doctor_dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stomatology.app.domain.model.Appointment
import com.example.stomatology.app.domain.model.AppointmentStatus
import com.example.stomatology.app.domain.repository.AppointmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DoctorAppointmentUiState(
    val appointments: List<Appointment> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class DoctorAppointmentViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DoctorAppointmentUiState())
    val uiState: StateFlow<DoctorAppointmentUiState> = _uiState

    private val doctorId = "doctor_1"

    init {
        observeAppointments()
    }

    private fun observeAppointments() {
        viewModelScope.launch {
            appointmentRepository.getAppointmentsForDoctor(doctorId).collectLatest { appointments ->
                _uiState.update {
                    it.copy(
                        appointments = appointments.sortedByDescending { item -> item.createdAt },
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }

    fun accept(id: String) {
        updateStatus(id, AppointmentStatus.ACCEPTED)
    }

    fun reject(id: String) {
        updateStatus(id, AppointmentStatus.REJECTED)
    }

    fun complete(id: String) {
        updateStatus(id, AppointmentStatus.COMPLETED)
    }

    private fun updateStatus(id: String, status: AppointmentStatus) {
        viewModelScope.launch {
            try {
                appointmentRepository.updateStatus(id, status)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Не удалось обновить статус")
                }
            }
        }
    }
}