package com.example.stomatology.app.presentation.records

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stomatology.app.domain.model.Appointment
import com.example.stomatology.app.domain.model.AppointmentStatus
import com.example.stomatology.app.domain.model.AvailableSlot
import com.example.stomatology.app.domain.repository.AppointmentRepository
import com.example.stomatology.app.domain.repository.AppointmentValidationException
import com.example.stomatology.app.domain.repository.SlotAlreadyBookedException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

enum class MyRecordActionState {
    Idle,
    Loading,
    Success,
    SlotAlreadyBooked,
    ValidationError,
    GeneralError
}

sealed class AppointmentsUiState {
    data object Loading : AppointmentsUiState()
    data object Empty : AppointmentsUiState()
    data class Success(val appointments: List<Appointment>) : AppointmentsUiState()
    data class Error(val message: String) : AppointmentsUiState()
}

data class MyRecordsUiState(
    val appointments: List<Appointment> = emptyList(),
    val isLoading: Boolean = true,
    val isSlotsLoading: Boolean = false,
    val rescheduleSlots: List<AvailableSlot> = emptyList(),
    val appointmentsUiState: AppointmentsUiState = AppointmentsUiState.Loading,
    val actionState: MyRecordActionState = MyRecordActionState.Idle,
    val error: String? = null
)

@HiltViewModel
class MyRecordsViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyRecordsUiState())
    val uiState: StateFlow<MyRecordsUiState> = _uiState

    private var appointmentsJob: Job? = null

    private val patientId: String?
        get() = auth.currentUser?.uid

    private val patientEmail: String?
        get() = auth.currentUser?.email

    init {
        observeAppointments()
    }

    fun retryLoadAppointments() {
        observeAppointments()
    }

    fun appointmentsForTab(tabIndex: Int): List<Appointment> {
        val all = _uiState.value.appointments
        val today = LocalDate.now()
        return when (tabIndex) {
            1 -> all.filter { isUpcomingAppointment(it, today) }
            2 -> all.filter { isPastAppointment(it, today) }
            else -> all
        }
    }

    fun loadRescheduleSlots(appointment: Appointment, date: String) {
        if (appointment.doctorId.isBlank() || appointment.clinicId.isBlank() || date.isBlank()) {
            _uiState.update {
                it.copy(
                    actionState = MyRecordActionState.ValidationError,
                    error = "Дерек толық емес"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSlotsLoading = true,
                    error = null
                )
            }

            try {
                val slots = appointmentRepository.generateAvailableSlots(
                    doctorId = appointment.doctorId,
                    clinicId = appointment.clinicId,
                    date = date
                )

                _uiState.update {
                    it.copy(
                        isSlotsLoading = false,
                        rescheduleSlots = slots
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSlotsLoading = false,
                        rescheduleSlots = emptyList(),
                        actionState = MyRecordActionState.GeneralError,
                        error = e.message ?: "Бос уақыттарды жүктеу мүмкін болмады"
                    )
                }
            }
        }
    }

    fun rescheduleAppointment(
        appointmentId: String,
        newDate: String,
        newTime: String
    ) {
        val uid = patientId
        if (uid.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    actionState = MyRecordActionState.ValidationError,
                    error = "Қолданушы табылмады. Қайта кіріңіз."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    actionState = MyRecordActionState.Loading,
                    error = null
                )
            }

            try {
                appointmentRepository.rescheduleAppointment(
                    appointmentId = appointmentId,
                    newDate = newDate,
                    newTime = newTime,
                    changedBy = uid,
                    reason = "Пациент уақытты өзгертті"
                )
                _uiState.update {
                    it.copy(
                        actionState = MyRecordActionState.Success,
                        error = null
                    )
                }
            } catch (e: SlotAlreadyBookedException) {
                _uiState.update {
                    it.copy(
                        actionState = MyRecordActionState.SlotAlreadyBooked,
                        error = "Бұл уақыт бос емес"
                    )
                }
            } catch (e: AppointmentValidationException) {
                _uiState.update {
                    it.copy(
                        actionState = MyRecordActionState.ValidationError,
                        error = e.message ?: "Тексеру қатесі"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        actionState = MyRecordActionState.GeneralError,
                        error = e.message ?: "Уақытты өзгерту мүмкін болмады"
                    )
                }
            }
        }
    }

    fun cancelAppointment(appointmentId: String, reason: String) {
        val uid = patientId
        if (uid.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    actionState = MyRecordActionState.ValidationError,
                    error = "Қолданушы табылмады. Қайта кіріңіз."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    actionState = MyRecordActionState.Loading,
                    error = null
                )
            }

            try {
                appointmentRepository.cancelAppointmentWithSlotRelease(
                    appointmentId = appointmentId,
                    changedBy = uid,
                    reason = reason
                )
                _uiState.update {
                    it.copy(
                        actionState = MyRecordActionState.Success,
                        error = null
                    )
                }
            } catch (e: AppointmentValidationException) {
                _uiState.update {
                    it.copy(
                        actionState = MyRecordActionState.ValidationError,
                        error = e.message ?: "Тексеру қатесі"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        actionState = MyRecordActionState.GeneralError,
                        error = e.message ?: "Бас тарту мүмкін болмады"
                    )
                }
            }
        }
    }

    private fun observeAppointments() {
        appointmentsJob?.cancel()

        val uid = patientId
        if (uid.isNullOrBlank()) {
            Log.e(
                APPOINTMENTS_DEBUG_TAG,
                "state=error reason=no_user uid=$uid email=$patientEmail projectId=${FirebaseFirestore.getInstance().app.options.projectId}"
            )
            _uiState.update {
                it.copy(
                    appointments = emptyList(),
                    isLoading = false,
                    appointmentsUiState = AppointmentsUiState.Error("Қолданушы табылмады. Қайта кіріңіз."),
                    actionState = MyRecordActionState.ValidationError,
                    error = "Қолданушы табылмады. Қайта кіріңіз."
                )
            }
            return
        }

        appointmentsJob = viewModelScope.launch {
            appointmentRepository.getAppointmentsForPatient(uid)
                .onStart {
                    val projectId = FirebaseFirestore.getInstance().app.options.projectId
                    Log.d(
                        APPOINTMENTS_DEBUG_TAG,
                        "state=loading uid=$uid email=$patientEmail projectId=$projectId source=MyRecordsViewModel"
                    )
                    _uiState.update {
                        it.copy(
                            isLoading = true,
                            appointmentsUiState = AppointmentsUiState.Loading,
                            error = null
                        )
                    }
                }
                .catch { throwable ->
                    val friendlyError = mapLoadError(throwable)
                    Log.e(
                        APPOINTMENTS_DEBUG_TAG,
                        "state=error uid=$uid email=$patientEmail message=${throwable.message}",
                        throwable
                    )
                    _uiState.update {
                        it.copy(
                            appointments = emptyList(),
                            isLoading = false,
                            appointmentsUiState = AppointmentsUiState.Error(friendlyError),
                            actionState = MyRecordActionState.GeneralError,
                            error = friendlyError
                        )
                    }
                }
                .collectLatest { appointments ->
                    val sortedAppointments = appointments.sortedByDescending { item -> item.createdAt }
                    val nextState = if (sortedAppointments.isEmpty()) {
                        AppointmentsUiState.Empty
                    } else {
                        AppointmentsUiState.Success(sortedAppointments)
                    }
                    val today = LocalDate.now()
                    val upcomingCount = sortedAppointments.count { isUpcomingAppointment(it, today) }
                    val pastCount = sortedAppointments.count { isPastAppointment(it, today) }
                    Log.d(
                        APPOINTMENTS_DEBUG_TAG,
                        "state=success uid=$uid total=${sortedAppointments.size} upcoming=$upcomingCount past=$pastCount"
                    )
                    _uiState.update {
                        it.copy(
                            appointments = sortedAppointments,
                            isLoading = false,
                            appointmentsUiState = nextState,
                            error = null
                        )
                    }
                }
        }
    }

    private fun isUpcomingAppointment(
        appointment: Appointment,
        today: LocalDate
    ): Boolean {
        if (appointment.status == AppointmentStatus.COMPLETED ||
            appointment.status == AppointmentStatus.CANCELLED ||
            appointment.status == AppointmentStatus.NO_SHOW
        ) {
            return false
        }
        val parsedDate = parseAppointmentDate(appointment.date)
        if (parsedDate != null) {
            return !parsedDate.isBefore(today)
        }
        return appointment.status == AppointmentStatus.PENDING ||
            appointment.status == AppointmentStatus.CONFIRMED ||
            appointment.status == AppointmentStatus.RESCHEDULED
    }

    private fun isPastAppointment(
        appointment: Appointment,
        today: LocalDate
    ): Boolean {
        if (appointment.status == AppointmentStatus.COMPLETED ||
            appointment.status == AppointmentStatus.CANCELLED ||
            appointment.status == AppointmentStatus.NO_SHOW
        ) {
            return true
        }
        val parsedDate = parseAppointmentDate(appointment.date) ?: return false
        return parsedDate.isBefore(today)
    }

    private fun parseAppointmentDate(value: String): LocalDate? {
        val normalized = value.trim()
        if (normalized.isEmpty()) {
            return null
        }
        val formatters = listOf(
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ISO_DATE,
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
        )
        return formatters.firstNotNullOfOrNull { formatter ->
            runCatching { LocalDate.parse(normalized, formatter) }.getOrNull()
        }
    }

    private fun mapLoadError(throwable: Throwable): String {
        val firestoreError = throwable as? FirebaseFirestoreException
        return when (firestoreError?.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Жазбаларды көруге рұқсат жоқ."
            FirebaseFirestoreException.Code.UNAVAILABLE -> "Интернет байланысын тексеріңіз."
            FirebaseFirestoreException.Code.FAILED_PRECONDITION -> {
                "Деректерді оқу мүмкін болмады. Кейінірек қайталап көріңіз."
            }
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> {
                "Сұрауға жауап беру уақыты аяқталды. Қайталап көріңіз."
            }
            else -> {
                val lower = throwable.message.orEmpty().lowercase()
                when {
                    lower.contains("permission") || lower.contains("denied") -> {
                        "Жазбаларды көруге рұқсат жоқ."
                    }
                    lower.contains("timeout") || lower.contains("deadline") -> {
                        "Сұрауға жауап беру уақыты аяқталды. Қайталап көріңіз."
                    }
                    lower.contains("network") ||
                        lower.contains("unavailable") ||
                        lower.contains("failed to connect") ||
                        lower.contains("unable to resolve host") -> {
                        "Интернет байланысын тексеріңіз."
                    }
                    else -> "Жазбаларды жүктеу кезінде қате пайда болды."
                }
            }
        }
    }

    companion object {
        private const val APPOINTMENTS_DEBUG_TAG = "APPOINTMENTS_DEBUG"
    }
}
