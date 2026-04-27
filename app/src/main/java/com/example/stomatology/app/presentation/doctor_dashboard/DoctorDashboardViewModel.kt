package com.example.stomatology.app.presentation.doctor_dashboard

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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class DoctorDashboardUiState(
    val doctorName: String = "Доктор",
    val pendingCount: Int = 0,
    val acceptedTodayCount: Int = 0,
    val completedCount: Int = 0,
    val recentAppointments: List<Appointment> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class DoctorDashboardViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(DoctorDashboardUiState())
    val uiState: StateFlow<DoctorDashboardUiState> = _uiState

    private val doctorId: String
        get() = auth.currentUser?.uid.orEmpty()

    init {
        observeAppointments()
    }

    private fun observeAppointments() {
        val currentDoctorId = doctorId

        if (currentDoctorId.isBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Доктор не авторизован"
                )
            }
            return
        }

        viewModelScope.launch {
            appointmentRepository.getAppointmentsForDoctor(currentDoctorId)
                .collectLatest { appointments ->
                    val sortedAppointments = appointments.sortedWith(
                        compareBy<Appointment> { it.date }.thenBy { it.time }
                    )

                    val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)

                    _uiState.update {
                        it.copy(
                            doctorName = appointments.firstOrNull()?.doctorName
                                ?.takeIf { name -> name.isNotBlank() }
                                ?: "Доктор",
                            pendingCount = appointments.count { item ->
                                item.status == AppointmentStatus.PENDING
                            },
                            acceptedTodayCount = appointments.count { item ->
                                item.status == AppointmentStatus.ACCEPTED && item.date == today
                            },
                            completedCount = appointments.count { item ->
                                item.status == AppointmentStatus.COMPLETED
                            },
                            recentAppointments = sortedAppointments.take(5),
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }
}