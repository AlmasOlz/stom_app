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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class DoctorDashboardUiState(
    val doctorName: String = "Нуржан Сатжанов",
    val pendingCount: Int = 0,
    val acceptedTodayCount: Int = 0,
    val completedCount: Int = 0,
    val recentAppointments: List<Appointment> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class DoctorDashboardViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DoctorDashboardUiState())
    val uiState: StateFlow<DoctorDashboardUiState> = _uiState

    private val doctorId = "doctor_1"

    init {
        observeAppointments()
    }

    private fun observeAppointments() {
        viewModelScope.launch {
            appointmentRepository.getAppointmentsForDoctor(doctorId).collectLatest { appointments ->
                val sortedAppointments = appointments.sortedWith(
                    compareBy<Appointment> { it.date }.thenBy { it.time }
                )

                val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)

                val pendingCount = appointments.count { it.status == AppointmentStatus.PENDING }
                val acceptedTodayCount = appointments.count {
                    it.status == AppointmentStatus.ACCEPTED && it.date == today
                }
                val completedCount = appointments.count { it.status == AppointmentStatus.COMPLETED }

                val recentAppointments = sortedAppointments.take(5)

                val doctorName = appointments.firstOrNull()?.doctorName
                    ?.takeIf { it.isNotBlank() }
                    ?: "Нуржан Сатжанов"

                _uiState.update {
                    it.copy(
                        doctorName = doctorName,
                        pendingCount = pendingCount,
                        acceptedTodayCount = acceptedTodayCount,
                        completedCount = completedCount,
                        recentAppointments = recentAppointments,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }
}