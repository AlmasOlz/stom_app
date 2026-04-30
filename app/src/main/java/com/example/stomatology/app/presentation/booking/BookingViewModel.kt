package com.example.stomatology.app.presentation.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stomatology.app.core.booking.BookingDefaults
import com.example.stomatology.app.core.firebase.FirestoreCollections
import com.example.stomatology.app.core.firebase.FirestoreFields
import com.example.stomatology.app.core.firebase.UserRoles
import com.example.stomatology.app.core.util.Resource
import com.example.stomatology.app.domain.model.Appointment
import com.example.stomatology.app.domain.model.AppointmentStatus
import com.example.stomatology.app.domain.model.DoctorOption
import com.example.stomatology.app.domain.repository.AppRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

data class BookingUiState(
    val clinicId: String = "",
    val selectedDate: String = "",
    val selectedTime: String = "",
    val doctorId: String = "",
    val doctorName: String = "",
    val clinicName: String = "",
    val direction: String = "",
    val duration: String = BookingDefaults.DEFAULT_DURATION,
    val isLoading: Boolean = false,
    val isBooked: Boolean = false,
    val showSuccessDialog: Boolean = false,
    val error: String? = null,
    val availableTimeSlots: List<String> = BookingDefaults.DEFAULT_TIME_SLOTS,
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    val doctors: List<DoctorOption> = emptyList(),
    val isDoctorMenuExpanded: Boolean = false
)

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val appRepository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState

    private var allDoctors: List<DoctorOption> = emptyList()

    fun initClinic(clinicId: String) {
        if (clinicId.isBlank()) return

        val shouldLoadClinicName = _uiState.value.clinicId != clinicId ||
            _uiState.value.clinicName.isBlank()

        _uiState.update {
            it.copy(
                clinicId = clinicId,
                clinicName = if (it.clinicId == clinicId) it.clinicName else "",
                doctorId = "",
                doctorName = "",
                doctors = emptyList(),
                isDoctorMenuExpanded = false,
                error = null
            )
        }

        loadDoctors(clinicId)
        if (shouldLoadClinicName) {
            loadClinicName(clinicId)
        }
    }

    private fun loadClinicName(clinicId: String) {
        viewModelScope.launch {
            try {
                val result = appRepository.getClinics().first { it !is Resource.Loading }
                if (result is Resource.Success) {
                    val clinicName = result.data
                        .firstOrNull { clinic -> clinic.id == clinicId }
                        ?.name
                        .orEmpty()

                    if (clinicName.isNotBlank()) {
                        _uiState.update { it.copy(clinicName = clinicName) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = it.error ?: (e.message ?: "Клиниканы жүктеу мүмкін болмады"))
                }
            }
        }
    }

    private fun loadDoctors(clinicId: String) {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection(FirestoreCollections.USERS)
                    .whereEqualTo(FirestoreFields.ROLE, UserRoles.DOCTOR)
                    .whereEqualTo(FirestoreFields.CLINIC_ID, clinicId)
                    .get()
                    .await()

                allDoctors = snapshot.documents.mapNotNull { doc ->
                    val uid = doc.getString(FirestoreFields.UID) ?: doc.id
                    val firstName = doc.getString(FirestoreFields.FIRST_NAME) ?: ""
                    val lastName = doc.getString(FirestoreFields.LAST_NAME) ?: ""
                    val displayName = doc.getString(FirestoreFields.DISPLAY_NAME)
                        ?: "$firstName $lastName".trim()

                    DoctorOption(
                        uid = uid,
                        firstName = firstName,
                        lastName = lastName,
                        displayName = displayName,
                        email = doc.getString(FirestoreFields.EMAIL) ?: "",
                        phone = doc.getString(FirestoreFields.PHONE) ?: "",
                        specialty = doc.getString(FirestoreFields.SPECIALTY).orEmpty(),
                        clinicId = doc.getString(FirestoreFields.CLINIC_ID).orEmpty(),
                        photoUrl = doc.getString(FirestoreFields.PHOTO_URL).orEmpty(),
                        experienceYears = (doc.getLong(FirestoreFields.EXPERIENCE_YEARS) ?: 0L).toInt(),
                        aboutDoctor = doc.getString(FirestoreFields.ABOUT_DOCTOR).orEmpty()
                    )
                }

                applyDoctorFilter()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Дәрігерлерді жүктеу мүмкін болмады")
                }
            }
        }
    }

    fun onShowDatePicker(show: Boolean) {
        _uiState.update { it.copy(showDatePicker = show) }
    }

    fun onShowTimePicker(show: Boolean) {
        _uiState.update { it.copy(showTimePicker = show) }
    }

    fun onDoctorMenuExpandedChange(expanded: Boolean) {
        _uiState.update { it.copy(isDoctorMenuExpanded = expanded) }
    }

    fun onDateSelected(date: String) {
        _uiState.update { it.copy(selectedDate = date, error = null) }
    }

    fun onTimeSelected(time: String) {
        _uiState.update { it.copy(selectedTime = time, error = null) }
    }

    fun onDoctorSelected(doctor: DoctorOption) {
        _uiState.update {
            it.copy(
                doctorId = doctor.uid,
                doctorName = doctor.name,
                isDoctorMenuExpanded = false,
                error = null
            )
        }
    }

    fun onClinicNameChange(value: String) {
        _uiState.update { it.copy(clinicName = value, error = null) }
    }

    fun onDirectionChange(value: String) {
        _uiState.update {
            it.copy(
                direction = value,
                doctorId = "",
                doctorName = "",
                error = null
            )
        }
        applyDoctorFilter()
    }

    fun onDurationChange(value: String) {
        _uiState.update { it.copy(duration = value, error = null) }
    }

    fun confirmBooking() {
        val state = _uiState.value

        if (state.clinicId.isBlank()) {
            _uiState.update { it.copy(error = "Клиника таңдалмаған") }
            return
        }
        if (state.selectedDate.isBlank()) {
            _uiState.update { it.copy(error = "Күнді таңдаңыз") }
            return
        }
        if (state.selectedTime.isBlank()) {
            _uiState.update { it.copy(error = "Уақытты таңдаңыз") }
            return
        }
        if (state.doctorId.isBlank()) {
            _uiState.update { it.copy(error = "Дәрігерді таңдаңыз") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val currentUser = auth.currentUser
                    ?: throw IllegalStateException("Пайдаланушы авторизациядан өтпеген")

                val uid = currentUser.uid
                val userSnapshot = firestore.collection(FirestoreCollections.USERS)
                    .document(uid)
                    .get()
                    .await()

                val firstName = userSnapshot.getString(FirestoreFields.FIRST_NAME) ?: ""
                val lastName = userSnapshot.getString(FirestoreFields.LAST_NAME) ?: ""
                val displayName = userSnapshot.getString(FirestoreFields.DISPLAY_NAME)
                    ?: "$firstName $lastName".trim()
                val phone = userSnapshot.getString(FirestoreFields.PHONE) ?: ""

                val busySnapshot = firestore.collection(FirestoreCollections.APPOINTMENTS)
                    .whereEqualTo(FirestoreFields.DOCTOR_ID, state.doctorId)
                    .whereEqualTo(FirestoreFields.DATE, state.selectedDate)
                    .whereEqualTo(FirestoreFields.TIME, state.selectedTime)
                    .get()
                    .await()

                val hasConflict = busySnapshot.documents.any { doc ->
                    val status = doc.getString(FirestoreFields.STATUS).orEmpty()
                    status == AppointmentStatus.PENDING.name || status == AppointmentStatus.ACCEPTED.name
                }

                if (hasConflict) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Бұл уақыт бос емес, басқа уақытты таңдаңыз"
                        )
                    }
                    return@launch
                }

                val appointment = Appointment(
                    id = UUID.randomUUID().toString(),
                    patientId = uid,
                    patientName = displayName,
                    patientPhone = phone,
                    clinicId = state.clinicId,
                    clinicName = state.clinicName.ifBlank { state.clinicId },
                    doctorId = state.doctorId,
                    doctorName = state.doctorName,
                    service = state.direction,
                    date = state.selectedDate,
                    time = state.selectedTime,
                    duration = state.duration,
                    status = AppointmentStatus.PENDING,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                firestore.collection(FirestoreCollections.APPOINTMENTS)
                    .document(appointment.id)
                    .set(appointment)
                    .await()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isBooked = true,
                        showSuccessDialog = true,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Жазылу мүмкін болмады"
                    )
                }
            }
        }
    }

    fun dismissSuccessDialog() {
        _uiState.update { it.copy(showSuccessDialog = false) }
    }

    fun resetBookingState() {
        _uiState.value = BookingUiState()
        allDoctors = emptyList()
    }

    private fun applyDoctorFilter() {
        val state = _uiState.value
        val serviceName = state.direction
        val filteredDoctors = allDoctors.filterByService(serviceName)
        val selectedDoctorValid = filteredDoctors.any { it.uid == state.doctorId }

        val nextError = when {
            allDoctors.isEmpty() -> "Бұл клиникада дәрігер тіркелмеген"
            filteredDoctors.isEmpty() -> "«$serviceName» қызметіне сәйкес дәрігер табылмады"
            else -> null
        }

        _uiState.update {
            it.copy(
                doctors = filteredDoctors,
                doctorId = if (selectedDoctorValid) it.doctorId else "",
                doctorName = if (selectedDoctorValid) it.doctorName else "",
                error = nextError
            )
        }
    }
}

private fun List<DoctorOption>.filterByService(serviceName: String): List<DoctorOption> {
    val requiredKeywords = requiredSpecialtyKeywords(serviceName)
    if (requiredKeywords.isEmpty()) {
        return this
    }

    return filter { doctor ->
        val specialty = doctor.specialty.normalizeForMatch()
        requiredKeywords.any { keyword -> specialty.contains(keyword) }
    }
}

private fun requiredSpecialtyKeywords(serviceName: String): Set<String> {
    val service = serviceName.normalizeForMatch()
    if (service.isBlank()) return emptySet()

    return when {
        service.contains("жулу") || service.contains("удален") -> setOf("хирург")
        service.contains("имплан") -> setOf("хирург", "имплант")
        service.contains("брекет") || service.contains("элайнер") || service.contains("ортодонт") -> setOf("ортодонт")
        service.contains("протез") || service.contains("корон") || service.contains("винир") -> setOf("ортопед")
        service.contains("гнатолог") -> setOf("гнатолог")
        service.contains("пародонт") || service.contains("кызыл иек") -> setOf("пародонт")
        service.contains("балалар") || service.contains("детск") -> setOf("дет", "балалар")
        service.contains("тазалау") || service.contains("гигиен") -> setOf("гигиенист", "терапевт")
        else -> emptySet()
    }
}

private fun String.normalizeForMatch(): String {
    return lowercase()
        .replace("ё", "е")
        .replace("қ", "к")
        .replace("ғ", "г")
        .replace("ә", "а")
        .replace("ө", "о")
        .replace("ү", "у")
        .replace("ұ", "у")
        .replace("ң", "н")
}
