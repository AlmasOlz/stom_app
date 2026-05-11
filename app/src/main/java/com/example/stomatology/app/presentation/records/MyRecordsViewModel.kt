package com.example.stomatology.app.presentation.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stomatology.app.domain.model.Appointment
import com.example.stomatology.app.domain.model.AppointmentStatus
import com.example.stomatology.app.domain.model.AvailableSlot
import com.example.stomatology.app.domain.repository.AppointmentRepository
import com.example.stomatology.app.domain.repository.AppointmentValidationException
import com.example.stomatology.app.domain.repository.SlotAlreadyBookedException
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
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
        observeAppointments()
    }

    fun upcomingAppointments(): List<Appointment> {
        return _uiState.value.appointments.filter {
            it.status == AppointmentStatus.PENDING ||
                it.status == AppointmentStatus.CONFIRMED ||
                it.status == AppointmentStatus.RESCHEDULED
        }
    }

    fun pastAppointments(): List<Appointment> {
        return _uiState.value.appointments.filter {
            it.status == AppointmentStatus.COMPLETED ||
                it.status == AppointmentStatus.CANCELLED ||
                it.status == AppointmentStatus.NO_SHOW
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

    private fun observeAppointments() {
        val uid = patientId
        if (uid == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    actionState = MyRecordActionState.ValidationError,
                    error = "Пайдаланушы авторизациядан өтпеген"
                )
            }
            return
        }

        viewModelScope.launch {
            appointmentRepository.getAppointmentsForPatient(uid)
                .catch { e ->
                    Log.e(TAG, "getAppointmentsForPatient failed uid=$uid", e)
                    val message = firestoreErrorMessage(e)
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

    private fun firestoreErrorMessage(e: Throwable): String {
        // Sometimes Firestore exceptions are wrapped inside another throwable,
        // so we walk the cause chain to find the real FirebaseFirestoreException.
        val fe = generateSequence(e) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
            ?: return e.message ?: "Жазбаларды жүктеу қатесі"
        return when (fe.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                "Firestore: рұқсат жоқ (rules немесе auth)"
            FirebaseFirestoreException.Code.FAILED_PRECONDITION ->
                "Firestore: индекс қажет — Logcat-тегі сілтемені Firebase Console-да ашыңыз"
            FirebaseFirestoreException.Code.UNAVAILABLE ->
                "Желі қолжетімсіз, кейінірек қайталаңыз"
            else -> fe.message ?: fe.code.name
        }
    }

    companion object {
        private const val TAG = "MyRecordsViewModel"
    }
}
