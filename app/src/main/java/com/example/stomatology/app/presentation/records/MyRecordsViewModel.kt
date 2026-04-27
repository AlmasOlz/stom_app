package com.example.stomatology.app.presentation.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stomatology.app.domain.model.Appointment
import com.example.stomatology.app.domain.model.AppointmentStatus
import com.example.stomatology.app.domain.repository.AppointmentRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyRecordsUiState(
    val appointments: List<Appointment> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class MyRecordsViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyRecordsUiState())
    val uiState: StateFlow<MyRecordsUiState> = _uiState

    init {
        observeAppointments()
    }

    private fun observeAppointments() {
        val patientId = auth.currentUser?.uid

        if (patientId == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Пользователь не авторизован"
                )
            }
            return
        }

        viewModelScope.launch {
            appointmentRepository.getAppointmentsForPatient(patientId)
                .collectLatest { appointments ->
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

    fun upcomingAppointments(): List<Appointment> {
        return _uiState.value.appointments.filter {
            it.status == AppointmentStatus.PENDING || it.status == AppointmentStatus.ACCEPTED
        }
    }

    fun pastAppointments(): List<Appointment> {
        return _uiState.value.appointments.filter {
            it.status == AppointmentStatus.COMPLETED ||
                    it.status == AppointmentStatus.REJECTED ||
                    it.status == AppointmentStatus.CANCELLED
        }
    }
}