package com.example.stomatology.app.presentation.doctor_dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stomatology.app.core.firebase.FirestoreCollections
import com.example.stomatology.app.core.firebase.FirestoreFields
import com.example.stomatology.app.domain.model.Appointment
import com.example.stomatology.app.domain.model.AppointmentStatus
import com.example.stomatology.app.domain.repository.AppointmentRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class DoctorDashboardUiState(
    val doctorName: String = "Дәрігер",
    val doctorSpecialty: String = "",
    val pendingCount: Int = 0,
    val acceptedTodayCount: Int = 0,
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val recentAppointments: List<Appointment> = emptyList(),
    val nextAppointment: Appointment? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class DoctorDashboardViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(DoctorDashboardUiState())
    val uiState: StateFlow<DoctorDashboardUiState> = _uiState

    private val doctorId: String
        get() = auth.currentUser?.uid.orEmpty()

    init {
        loadDoctorProfile()
        observeAppointments()
    }

    private fun loadDoctorProfile() {
        val currentDoctorId = doctorId

        if (currentDoctorId.isBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Дәрігер аккаунты табылмады"
                )
            }
            return
        }

        viewModelScope.launch {
            try {
                val snapshot = firestore.collection(FirestoreCollections.USERS)
                    .document(currentDoctorId)
                    .get()
                    .await()

                val firstName = snapshot.getString(FirestoreFields.FIRST_NAME).orEmpty()
                val lastName = snapshot.getString(FirestoreFields.LAST_NAME).orEmpty()
                val displayName = snapshot.getString(FirestoreFields.DISPLAY_NAME)
                    ?.takeIf { it.isNotBlank() }
                    ?: "$firstName $lastName".trim()

                _uiState.update {
                    it.copy(
                        doctorName = displayName.ifBlank { "Дәрігер" },
                        doctorSpecialty = snapshot.getString(FirestoreFields.SPECIALTY).orEmpty()
                    )
                }
            } catch (_: Exception) {
                // Keep dashboard usable even if profile fetch fails.
            }
        }
    }

    private fun observeAppointments() {
        val currentDoctorId = doctorId

        if (currentDoctorId.isBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Дәрігер аккаунты табылмады"
                )
            }
            return
        }

        viewModelScope.launch {
            appointmentRepository.getAppointmentsForDoctor(currentDoctorId)
                .collectLatest { appointments ->
                    val today = LocalDate.now()
                    val nowTimeMinutes = LocalTime.now().hour * 60 + LocalTime.now().minute

                    val sortedAppointments = appointments.sortedWith(
                        compareBy<Appointment>(
                            { parseAppointmentDate(it.date) ?: LocalDate.MAX },
                            { parseTimeToMinutes(it.time) ?: Int.MAX_VALUE },
                            { it.createdAt }
                        )
                    )

                    val acceptedToday = appointments.count { item ->
                        item.status == AppointmentStatus.ACCEPTED &&
                            parseAppointmentDate(item.date) == today
                    }

                    val upcoming = sortedAppointments.firstOrNull { item ->
                        val date = parseAppointmentDate(item.date) ?: return@firstOrNull false
                        val time = parseTimeToMinutes(item.time) ?: Int.MAX_VALUE
                        val isActiveStatus =
                            item.status == AppointmentStatus.ACCEPTED || item.status == AppointmentStatus.PENDING

                        isActiveStatus && (date > today || (date == today && time >= nowTimeMinutes))
                    } ?: sortedAppointments.firstOrNull { item ->
                        item.status == AppointmentStatus.ACCEPTED || item.status == AppointmentStatus.PENDING
                    }

                    _uiState.update {
                        it.copy(
                            pendingCount = appointments.count { item ->
                                item.status == AppointmentStatus.PENDING
                            },
                            acceptedTodayCount = acceptedToday,
                            completedCount = appointments.count { item ->
                                item.status == AppointmentStatus.COMPLETED
                            },
                            totalCount = appointments.size,
                            recentAppointments = sortedAppointments.take(6),
                            nextAppointment = upcoming,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    private fun parseAppointmentDate(value: String): LocalDate? {
        if (value.isBlank()) {
            return null
        }

        val formatters = listOf(
            DateTimeFormatter.ISO_DATE,
            DateTimeFormatter.ofPattern("dd.MM.yyyy")
        )

        return formatters.firstNotNullOfOrNull { formatter ->
            runCatching { LocalDate.parse(value, formatter) }.getOrNull()
        }
    }

    private fun parseTimeToMinutes(value: String): Int? {
        if (value.isBlank()) {
            return null
        }

        return runCatching {
            val parsed = LocalTime.parse(value, DateTimeFormatter.ofPattern("HH:mm"))
            parsed.hour * 60 + parsed.minute
        }.getOrNull()
    }
}
