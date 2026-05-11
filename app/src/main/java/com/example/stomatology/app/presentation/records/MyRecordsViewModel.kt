package com.example.stomatology.app.presentation.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stomatology.app.core.firebase.currentUserIdFlow
import com.example.stomatology.app.domain.model.Appointment
import com.example.stomatology.app.domain.model.AppointmentStatus
import com.example.stomatology.app.domain.model.isRecordsPastTab
import com.example.stomatology.app.domain.model.isRecordsUpcomingTab
import com.example.stomatology.app.domain.model.scheduleInstantMillis
import com.example.stomatology.app.domain.model.AvailableSlot
import com.example.stomatology.app.domain.repository.AppointmentRepository
import com.example.stomatology.app.domain.repository.AppointmentValidationException
import com.example.stomatology.app.domain.repository.SlotAlreadyBookedException
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyRecordsUiState())
    val uiState: StateFlow<MyRecordsUiState> = _uiState

    private val patientId: String?
        get() = auth.currentUser?.uid

    init {
        viewModelScope.launch {
            auth.currentUserIdFlow().collectLatest { uid ->
                if (uid.isNullOrBlank()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            appointments = emptyList(),
                            error = null,
                            actionState = MyRecordActionState.Idle
                        )
                    }
                    return@collectLatest
                }
                _uiState.update { it.copy(isLoading = true, error = null) }
                try {
                    appointmentRepository.getAppointmentsForPatient(uid).collectLatest { appointments ->
                        _uiState.update {
                            it.copy(
                                appointments = appointments.sortedByDescending { it.scheduleInstantMillis() },
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            appointments = emptyList(),
                            error = e.message ?: "Жазбаларды жүктеу мүмкін болмады"
                        )
                    }
                }
            }
        }
    }

    fun upcomingAppointments(): List<Appointment> {
        return _uiState.value.appointments.filter { it.isRecordsUpcomingTab() }
    }

    fun pastAppointments(): List<Appointment> {
        return _uiState.value.appointments.filter { it.isRecordsPastTab() }
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
                    error = "Пайдаланушы авторизациядан өтпеген"
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
                    error = "Пайдаланушы авторизациядан өтпеген"
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

}
