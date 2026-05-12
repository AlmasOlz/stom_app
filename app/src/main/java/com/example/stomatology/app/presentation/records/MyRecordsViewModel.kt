package com.example.stomatology.app.presentation.records

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stomatology.app.BuildConfig
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
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

data class MyRecordsUiState(
    val appointments: List<Appointment> = emptyList(),
    val isLoading: Boolean = true,
    val isSlotsLoading: Boolean = false,
    val rescheduleSlots: List<AvailableSlot> = emptyList(),
    val actionState: MyRecordActionState = MyRecordActionState.Idle,
    val error: String? = null
)

@HiltViewModel
class MyRecordsViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyRecordsUiState())
    val uiState: StateFlow<MyRecordsUiState> = _uiState

    private var observeJob: Job? = null

    private val patientId: String?
        get() = auth.currentUser?.uid

    init {
        startObserveAppointments()
    }

    fun retryLoadAppointments() {
        _uiState.update {
            it.copy(
                isLoading = true,
                error = null,
                actionState = MyRecordActionState.Idle
            )
        }
        startObserveAppointments()
    }

    fun appointmentsForTab(tabIndex: Int): List<Appointment> {
        val now = LocalDateTime.now(DEFAULT_ZONE_ID)
        return _uiState.value.appointments.filter { appointment ->
            when (tabIndex) {
                TAB_UPCOMING -> isUpcomingAppointment(appointment, now)
                TAB_PAST -> isPastAppointment(appointment, now)
                else -> true
            }
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
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isSlotsLoading = false,
                        rescheduleSlots = emptyList(),
                        actionState = MyRecordActionState.GeneralError,
                        error = "Бос уақыттарды жүктеу мүмкін болмады"
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
                    error = ERROR_NO_USER
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
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        actionState = MyRecordActionState.GeneralError,
                        error = "Уақытты өзгерту мүмкін болмады"
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
                    error = ERROR_NO_USER
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
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        actionState = MyRecordActionState.GeneralError,
                        error = "Bas tartu mumkin bolmady"
                    )
                }
            }
        }
    }

    private fun startObserveAppointments() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            observeAppointments()
        }
    }

    private suspend fun observeAppointments() {
        val uid = patientId
        if (uid.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    actionState = MyRecordActionState.ValidationError,
                    error = ERROR_NO_USER
                )
            }
            return
        }

        logDebug(
            "load_start uid=$uid email=${auth.currentUser?.email.orEmpty()} " +
                "projectId=${firestore.app.options.projectId}"
        )

        appointmentRepository.getAppointmentsForPatient(uid)
            .catch { e ->
                Log.e(TAG, "getAppointmentsForPatient failed uid=$uid", e)
                val message = firestoreErrorMessage(e)
                logDebug("load_error uid=$uid message=$message raw=${e.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        appointments = emptyList(),
                        actionState = MyRecordActionState.GeneralError,
                        error = message
                    )
                }
            }
            .collectLatest { appointments ->
                val sorted = appointments.sortedByDescending { item -> item.createdAt }
                logDebug("load_success uid=$uid size=${sorted.size}")
                _uiState.update {
                    it.copy(
                        appointments = sorted,
                        isLoading = false,
                        error = null
                    )
                }
            }
    }

    private fun firestoreErrorMessage(e: Throwable): String {
        val fe = e as? FirebaseFirestoreException ?: return ERROR_LOAD_FAILED
        return when (fe.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Жазбаларды көруге рұқсат жоқ."
            FirebaseFirestoreException.Code.UNAVAILABLE -> "Интернет байланысын тексеріңіз."
            FirebaseFirestoreException.Code.FAILED_PRECONDITION -> ERROR_LOAD_FAILED
            else -> ERROR_LOAD_FAILED
        }
    }

    private fun isUpcomingAppointment(appointment: Appointment, now: LocalDateTime): Boolean {
        if (!appointment.status.isActiveStatus()) return false
        val appointmentDateTime = appointment.asDateTime() ?: return false
        return !appointmentDateTime.isBefore(now)
    }

    private fun isPastAppointment(appointment: Appointment, now: LocalDateTime): Boolean {
        if (appointment.status.isFinalStatus()) return true
        val appointmentDateTime = appointment.asDateTime() ?: return false
        return appointmentDateTime.isBefore(now)
    }

    private fun Appointment.asDateTime(): LocalDateTime? {
        val parsedDate = parseDate(date) ?: return null
        val parsedTime = parseTime(time) ?: LocalTime.MIDNIGHT
        return parsedDate.atTime(parsedTime)
    }

    private fun parseDate(raw: String): LocalDate? {
        val value = raw.trim()
        if (value.isBlank()) return null
        val formatters = listOf(
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ISO_DATE,
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
        )
        return formatters.firstNotNullOfOrNull { formatter ->
            runCatching { LocalDate.parse(value, formatter) }.getOrNull()
        }
    }

    private fun parseTime(raw: String): LocalTime? {
        val value = raw.trim()
        if (value.isBlank()) return null
        val formatters = listOf(
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("H:mm")
        )
        return formatters.firstNotNullOfOrNull { formatter ->
            runCatching { LocalTime.parse(value, formatter) }.getOrNull()
        }
    }

    private fun AppointmentStatus.isActiveStatus(): Boolean {
        return this == AppointmentStatus.PENDING ||
            this == AppointmentStatus.CONFIRMED ||
            this == AppointmentStatus.RESCHEDULED
    }

    private fun AppointmentStatus.isFinalStatus(): Boolean {
        return this == AppointmentStatus.COMPLETED ||
            this == AppointmentStatus.CANCELLED ||
            this == AppointmentStatus.NO_SHOW
    }

    private fun logDebug(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(DEBUG_TAG, message)
        }
    }

    companion object {
        private const val TAG = "MyRecordsViewModel"
        private const val DEBUG_TAG = "APPOINTMENTS_DEBUG"
        private const val TAB_UPCOMING = 1
        private const val TAB_PAST = 2
        private const val ERROR_NO_USER = "Қолданушы табылмады. Қайта кіріңіз."
        private const val ERROR_LOAD_FAILED = "Жазбаларды жүктеу кезінде қате пайда болды."
        private val DEFAULT_ZONE_ID: ZoneId = ZoneId.of("Asia/Almaty")
    }
}
