package com.example.stomatology.app.presentation.doctor_dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stomatology.app.domain.model.Appointment
import com.example.stomatology.app.domain.model.AppointmentStatus
import com.example.stomatology.app.domain.repository.AppointmentRepository
import com.example.stomatology.app.domain.repository.AppointmentValidationException
import com.example.stomatology.app.domain.repository.SlotAlreadyBookedException
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

enum class AppointmentActionState {
    Idle,
    Loading,
    Success,
    SlotAlreadyBooked,
    ValidationError,
    GeneralError
}

data class DoctorAppointmentUiState(
    val appointments: List<Appointment> = emptyList(),
    val isLoading: Boolean = true,
    val actionState: AppointmentActionState = AppointmentActionState.Idle,
    val error: String? = null
)

@HiltViewModel
class DoctorAppointmentViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(DoctorAppointmentUiState())
    val uiState: StateFlow<DoctorAppointmentUiState> = _uiState

    private val doctorId: String
        get() = auth.currentUser?.uid.orEmpty()

    init {
        observeAppointments()
    }

    fun accept(id: String) {
        updateStatus(id, AppointmentStatus.CONFIRMED)
    }

    fun reject(id: String) {
        launchAction {
            appointmentRepository.cancelAppointmentWithSlotRelease(
                appointmentId = id,
                changedBy = doctorId,
                reason = "Дәрігер бас тартты"
            )
        }
    }

    fun complete(id: String) {
        updateStatus(id, AppointmentStatus.COMPLETED)
    }

    fun markNoShow(id: String) {
        launchAction {
            appointmentRepository.markNoShow(
                appointmentId = id,
                changedBy = doctorId,
                reason = "Пациент қабылдауға келмеді"
            )
        }
    }

    private fun observeAppointments() {
        val currentDoctorId = doctorId
        if (currentDoctorId.isBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    actionState = AppointmentActionState.ValidationError,
                    error = "Дәрігер авторизациядан өтпеген"
                )
            }
            return
        }

        viewModelScope.launch {
            appointmentRepository.getAppointmentsForDoctor(currentDoctorId)
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            appointments = emptyList(),
                            isLoading = false,
                            actionState = AppointmentActionState.GeneralError,
                            error = mapLoadError(throwable)
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

    private fun mapLoadError(throwable: Throwable): String {
        val firestoreError = throwable as? FirebaseFirestoreException
        return when (firestoreError?.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Jazbalardy koruge ruqsat joq."
            FirebaseFirestoreException.Code.UNAVAILABLE -> "Internet bailanysyn tekseriniz."
            else -> "Jazbalardy jukteu kezinde qate paida boldy."
        }
    }

    private fun updateStatus(id: String, status: AppointmentStatus) {
        launchAction {
            appointmentRepository.updateStatus(
                appointmentId = id,
                status = status,
                changedBy = doctorId
            )
        }
    }

    private fun launchAction(action: suspend () -> Unit) {
        if (doctorId.isBlank()) {
            _uiState.update {
                it.copy(
                    actionState = AppointmentActionState.ValidationError,
                    error = "Дәрігер авторизациядан өтпеген"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    actionState = AppointmentActionState.Loading,
                    error = null
                )
            }

            try {
                action()
                _uiState.update {
                    it.copy(
                        actionState = AppointmentActionState.Success,
                        error = null
                    )
                }
            } catch (e: SlotAlreadyBookedException) {
                _uiState.update {
                    it.copy(
                        actionState = AppointmentActionState.SlotAlreadyBooked,
                        error = "Бұл уақыт бос емес"
                    )
                }
            } catch (e: AppointmentValidationException) {
                _uiState.update {
                    it.copy(
                        actionState = AppointmentActionState.ValidationError,
                        error = e.message ?: "Тексеру қатесі"
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        actionState = AppointmentActionState.GeneralError,
                        error = "Статусты жаңарту мүмкін болмады"
                    )
                }
            }
        }
    }
}
