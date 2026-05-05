package com.example.stomatology.app.presentation.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stomatology.app.core.booking.BookingDefaults
import com.example.stomatology.app.core.firebase.FirestoreCollections
import com.example.stomatology.app.core.firebase.FirestoreFields
import com.example.stomatology.app.core.firebase.UserRoles
import com.example.stomatology.app.domain.model.Appointment
import com.example.stomatology.app.domain.model.AppointmentStatus
import com.example.stomatology.app.domain.model.AvailableSlot
import com.example.stomatology.app.domain.model.DoctorOption
import com.example.stomatology.app.domain.repository.AppointmentRepository
import com.example.stomatology.app.domain.repository.AppointmentValidationException
import com.example.stomatology.app.domain.repository.SlotAlreadyBookedException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

enum class BookingSubmitState {
    Idle,
    Loading,
    Success,
    SlotAlreadyBooked,
    ValidationError,
    GeneralError
}

data class BookingUiState(
    val clinicId: String = "",
    val selectedDate: String = "",
    val selectedTime: String = "",
    val selectedSlotCapacity: Int = 1,
    val doctorId: String = "",
    val doctorName: String = "",
    val clinicName: String = "",
    val direction: String = "",
    val duration: String = BookingDefaults.DEFAULT_DURATION,
    val isLoading: Boolean = false,
    val isBooked: Boolean = false,
    val submitState: BookingSubmitState = BookingSubmitState.Idle,
    val showSuccessDialog: Boolean = false,
    val error: String? = null,
    val availableSlots: List<AvailableSlot> = BookingDefaults.DEFAULT_TIME_SLOTS.map {
        AvailableSlot(time = it, isEnabled = true, capacity = 1, bookedCount = 0, isFull = false)
    },
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    val doctors: List<DoctorOption> = emptyList(),
    val isDoctorMenuExpanded: Boolean = false
)

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    companion object {
        private const val ERROR_CLINIC_NOT_SELECTED = "Клиника таңдалмаған"
        private const val ERROR_CLINIC_NOT_LOADED = "Клиника деректері жүктелмеді"
        private const val ERROR_DATE_REQUIRED = "Күнді таңдаңыз"
        private const val ERROR_TIME_REQUIRED = "Уақытты таңдаңыз"
        private const val ERROR_DOCTOR_REQUIRED = "Дәрігерді таңдаңыз"
        private const val ERROR_SLOT_BUSY = "Бұл уақыт бос емес"
        private const val ERROR_AUTH_REQUIRED = "Қайта кіріп, жазылуды қайталаңыз"
        private const val ERROR_VALIDATION = "Енгізілген деректерді тексеріңіз"
        private const val ERROR_BOOKING_FAILED = "Жазылу орындалмады. Қайта көріңіз"
        private const val ERROR_DOCTORS_LOAD = "Дәрігерлерді жүктеу мүмкін болмады"
        private const val ERROR_SLOTS_LOAD = "Уақыттарды жүктеу мүмкін болмады"
    }

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState

    private var allDoctors: List<DoctorOption> = emptyList()

    fun initClinic(clinicId: String) {
        if (clinicId.isBlank()) {
            allDoctors = emptyList()
            _uiState.update {
                it.copy(
                    clinicId = "",
                    clinicName = "",
                    doctorId = "",
                    doctorName = "",
                    selectedDate = "",
                    selectedTime = "",
                    selectedSlotCapacity = 1,
                    doctors = emptyList(),
                    availableSlots = defaultSlots(),
                    submitState = BookingSubmitState.ValidationError,
                    error = ERROR_CLINIC_NOT_SELECTED
                )
            }
            return
        }

        val shouldLoadClinicName = _uiState.value.clinicId != clinicId ||
            _uiState.value.clinicName.isBlank()

        _uiState.update {
            it.copy(
                clinicId = clinicId,
                clinicName = if (it.clinicId == clinicId) it.clinicName else "",
                doctorId = "",
                doctorName = "",
                selectedDate = "",
                selectedTime = "",
                selectedSlotCapacity = 1,
                doctors = emptyList(),
                isDoctorMenuExpanded = false,
                availableSlots = defaultSlots(),
                submitState = BookingSubmitState.Idle,
                error = null
            )
        }

        loadDoctors(clinicId)
        if (shouldLoadClinicName) loadClinicName(clinicId)
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
        _uiState.update {
            it.copy(
                selectedDate = date,
                selectedTime = "",
                selectedSlotCapacity = 1,
                error = null
            )
        }
        refreshAvailableSlots()
    }

    fun onTimeSelected(time: String) {
        val slot = _uiState.value.availableSlots.firstOrNull { it.time == time }
        if (slot == null || !slot.isEnabled) {
            _uiState.update {
                it.copy(
                    submitState = BookingSubmitState.ValidationError,
                    error = ERROR_SLOT_BUSY
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                selectedTime = time,
                selectedSlotCapacity = slot.capacity.coerceAtLeast(1),
                error = null
            )
        }
    }

    fun onDoctorSelected(doctor: DoctorOption) {
        _uiState.update {
            it.copy(
                doctorId = doctor.uid,
                doctorName = doctor.name,
                selectedTime = "",
                selectedSlotCapacity = 1,
                isDoctorMenuExpanded = false,
                error = null
            )
        }
        refreshAvailableSlots()
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
                selectedTime = "",
                selectedSlotCapacity = 1,
                availableSlots = defaultSlots(),
                error = null
            )
        }
        applyDoctorFilter()
    }

    fun onDurationChange(value: String) {
        _uiState.update { it.copy(duration = value, error = null) }
    }

    fun dismissSuccessDialog() {
        _uiState.update { it.copy(showSuccessDialog = false) }
    }

    fun resetBookingState() {
        _uiState.value = BookingUiState()
        allDoctors = emptyList()
    }

    fun confirmBooking() {
        val state = _uiState.value
        if (state.clinicId.isBlank()) {
            setValidationError(ERROR_CLINIC_NOT_SELECTED)
            return
        }
        if (state.selectedDate.isBlank()) {
            setValidationError(ERROR_DATE_REQUIRED)
            return
        }
        if (state.selectedTime.isBlank()) {
            setValidationError(ERROR_TIME_REQUIRED)
            return
        }
        if (state.doctorId.isBlank()) {
            setValidationError(ERROR_DOCTOR_REQUIRED)
            return
        }

        val selectedSlot = state.availableSlots.firstOrNull { it.time == state.selectedTime }
        if (selectedSlot == null || !selectedSlot.isEnabled) {
            setValidationError(ERROR_SLOT_BUSY)
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isBooked = false,
                    submitState = BookingSubmitState.Loading,
                    error = null
                )
            }

            try {
                val currentUser = auth.currentUser
                    ?: throw AppointmentValidationException(ERROR_AUTH_REQUIRED)
                val uid = currentUser.uid

                val userSnapshot = firestore.collection(FirestoreCollections.USERS)
                    .document(uid)
                    .get()
                    .await()

                val firstName = userSnapshot.getString(FirestoreFields.FIRST_NAME).orEmpty()
                val lastName = userSnapshot.getString(FirestoreFields.LAST_NAME).orEmpty()
                val displayName = userSnapshot.getString(FirestoreFields.DISPLAY_NAME)
                    ?.takeIf { value -> value.isNotBlank() }
                    ?: "$firstName $lastName".trim()
                val phone = userSnapshot.getString(FirestoreFields.PHONE).orEmpty()
                val now = System.currentTimeMillis()

                val appointment = Appointment(
                    id = UUID.randomUUID().toString(),
                    patientId = uid,
                    patientName = displayName.ifBlank { "Пациент" },
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
                    statusChangedBy = uid,
                    createdAt = now,
                    updatedAt = now
                )

                appointmentRepository.createAppointment(
                    appointment = appointment,
                    slotCapacity = selectedSlot.capacity.coerceAtLeast(1)
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isBooked = true,
                        submitState = BookingSubmitState.Success,
                        showSuccessDialog = true,
                        error = null
                    )
                }
            } catch (e: SlotAlreadyBookedException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isBooked = false,
                        submitState = BookingSubmitState.SlotAlreadyBooked,
                        error = ERROR_SLOT_BUSY
                    )
                }
                refreshAvailableSlots()
            } catch (e: AppointmentValidationException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isBooked = false,
                        submitState = BookingSubmitState.ValidationError,
                        error = sanitizeErrorMessage(e.message, ERROR_VALIDATION)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isBooked = false,
                        submitState = BookingSubmitState.GeneralError,
                        error = sanitizeErrorMessage(e.message, ERROR_BOOKING_FAILED)
                    )
                }
            }
        }
    }

    private fun loadClinicName(clinicId: String) {
        if (clinicId.isBlank()) return

        viewModelScope.launch {
            runCatching {
                firestore.collection(FirestoreCollections.CLINICS)
                    .document(clinicId)
                    .get()
                    .await()
            }.onSuccess { snapshot ->
                val clinicName = snapshot.getString(FirestoreFields.NAME).orEmpty()
                _uiState.update {
                    it.copy(
                        clinicName = clinicName,
                        error = if (clinicName.isBlank() && it.error.isNullOrBlank()) {
                            ERROR_CLINIC_NOT_LOADED
                        } else {
                            it.error
                        }
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        error = it.error ?: sanitizeErrorMessage(
                            throwable.message,
                            ERROR_CLINIC_NOT_LOADED
                        )
                    )
                }
            }
        }
    }

    private fun loadDoctors(clinicId: String) {
        if (clinicId.isBlank()) return

        viewModelScope.launch {
            runCatching {
                firestore.collection(FirestoreCollections.USERS)
                    .whereEqualTo(FirestoreFields.ROLE, UserRoles.DOCTOR)
                    .whereEqualTo(FirestoreFields.CLINIC_ID, clinicId)
                    .get()
                    .await()
            }.onSuccess { snapshot ->
                allDoctors = snapshot.documents.mapNotNull { doc ->
                    val uid = doc.getString(FirestoreFields.UID) ?: doc.id
                    val firstName = doc.getString(FirestoreFields.FIRST_NAME).orEmpty()
                    val lastName = doc.getString(FirestoreFields.LAST_NAME).orEmpty()
                    val displayName = doc.getString(FirestoreFields.DISPLAY_NAME)
                        ?.takeIf { value -> value.isNotBlank() }
                        ?: "$firstName $lastName".trim()

                    DoctorOption(
                        uid = uid,
                        firstName = firstName,
                        lastName = lastName,
                        displayName = displayName,
                        email = doc.getString(FirestoreFields.EMAIL).orEmpty(),
                        phone = doc.getString(FirestoreFields.PHONE).orEmpty(),
                        specialty = doc.getString(FirestoreFields.SPECIALTY).orEmpty(),
                        clinicId = doc.getString(FirestoreFields.CLINIC_ID).orEmpty(),
                        photoUrl = doc.getString(FirestoreFields.PHOTO_URL).orEmpty(),
                        experienceYears = (doc.getLong(FirestoreFields.EXPERIENCE_YEARS) ?: 0L).toInt(),
                        aboutDoctor = doc.getString(FirestoreFields.ABOUT_DOCTOR).orEmpty()
                    )
                }
                applyDoctorFilter()
            }.onFailure { throwable ->
                allDoctors = emptyList()
                _uiState.update {
                    it.copy(
                        doctors = emptyList(),
                        doctorId = "",
                        doctorName = "",
                        availableSlots = defaultSlots(),
                        error = sanitizeErrorMessage(throwable.message, ERROR_DOCTORS_LOAD)
                    )
                }
            }
        }
    }

    private fun refreshAvailableSlots() {
        val state = _uiState.value
        if (state.clinicId.isBlank() || state.doctorId.isBlank() || state.selectedDate.isBlank()) {
            _uiState.update {
                it.copy(
                    availableSlots = defaultSlots(),
                    selectedTime = "",
                    selectedSlotCapacity = 1
                )
            }
            return
        }

        viewModelScope.launch {
            runCatching {
                appointmentRepository.generateAvailableSlots(
                    doctorId = state.doctorId,
                    clinicId = state.clinicId,
                    date = state.selectedDate
                )
            }.onSuccess { slots ->
                val selectedStillValid = slots.any { slot ->
                    slot.time == state.selectedTime && slot.isEnabled
                }
                val selectedCapacity = slots.firstOrNull { slot ->
                    slot.time == state.selectedTime
                }?.capacity?.coerceAtLeast(1) ?: 1

                _uiState.update {
                    it.copy(
                        availableSlots = slots,
                        selectedTime = if (selectedStillValid) it.selectedTime else "",
                        selectedSlotCapacity = if (selectedStillValid) selectedCapacity else 1,
                        error = if (slots.isEmpty()) "Бұл күнге бос уақыт жоқ" else null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        availableSlots = defaultSlots(),
                        selectedTime = "",
                        selectedSlotCapacity = 1,
                        error = sanitizeErrorMessage(throwable.message, ERROR_SLOTS_LOAD)
                    )
                }
            }
        }
    }

    private fun applyDoctorFilter() {
        val serviceName = _uiState.value.direction
        val requiredKeywords = requiredSpecialtyKeywords(serviceName)
        val filteredDoctors = allDoctors.filterByService(serviceName)
        val selectedDoctorValid = filteredDoctors.any { doctor ->
            doctor.uid == _uiState.value.doctorId
        }

        val nextError = when {
            allDoctors.isEmpty() -> "Бұл клиникада дәрігер тіркелмеген"
            requiredKeywords.isNotEmpty() && filteredDoctors.isEmpty() ->
                "\"$serviceName\" қызметіне сәйкес дәрігер табылмады"
            else -> null
        }

        _uiState.update {
            it.copy(
                doctors = filteredDoctors,
                doctorId = if (selectedDoctorValid) it.doctorId else "",
                doctorName = if (selectedDoctorValid) it.doctorName else "",
                selectedTime = "",
                selectedSlotCapacity = 1,
                availableSlots = defaultSlots(),
                error = nextError
            )
        }

        if (selectedDoctorValid && _uiState.value.selectedDate.isNotBlank()) {
            refreshAvailableSlots()
        }
    }

    private fun setValidationError(message: String) {
        _uiState.update {
            it.copy(
                submitState = BookingSubmitState.ValidationError,
                error = message
            )
        }
    }

    private fun sanitizeErrorMessage(rawMessage: String?, fallback: String): String {
        val message = rawMessage?.trim().orEmpty()
        if (message.isBlank()) return fallback

        val lowered = message.lowercase()
        val technicalMarkers = listOf(
            "flow exception transparency",
            "kotlinx.coroutines.flow",
            "java.lang",
            "at com.",
            "at androidx.",
            "exception",
            "clinic(",
            "stacktrace"
        )
        val isTechnical = technicalMarkers.any { marker -> lowered.contains(marker) } ||
            message.contains('\n')

        return if (isTechnical) fallback else message
    }

    private fun defaultSlots(): List<AvailableSlot> {
        return BookingDefaults.DEFAULT_TIME_SLOTS.map { time ->
            AvailableSlot(
                time = time,
                isEnabled = true,
                capacity = 1,
                bookedCount = 0,
                isFull = false
            )
        }
    }
}

private fun List<DoctorOption>.filterByService(serviceName: String): List<DoctorOption> {
    val requiredKeywords = requiredSpecialtyKeywords(serviceName)
    if (requiredKeywords.isEmpty()) return this

    return filter { doctor ->
        val specialty = doctor.specialty.normalizeForMatch()
        requiredKeywords.any { keyword -> specialty.contains(keyword) }
    }
}

private fun requiredSpecialtyKeywords(serviceName: String): Set<String> {
    val service = serviceName.normalizeForMatch()
    if (service.isBlank()) return emptySet()

    return when {
        service.contains("жұлу") || service.contains("удален") -> setOf("хирург")
        service.contains("имплан") -> setOf("хирург", "имплант")
        service.contains("брекет") || service.contains("элайнер") || service.contains("ортодонт") -> setOf("ортодонт")
        service.contains("протез") || service.contains("корон") || service.contains("винир") -> setOf("ортопед")
        service.contains("гнатолог") -> setOf("гнатолог")
        service.contains("пародонт") || service.contains("қызыл иек") -> setOf("пародонт")
        service.contains("балалар") || service.contains("детск") -> setOf("бала", "дет")
        service.contains("тазалау") || service.contains("гигиен") -> setOf("гигиенист", "терапевт")
        else -> emptySet()
    }
}

private fun String.normalizeForMatch(): String {
    return lowercase()
        .replace('ё', 'е')
        .replace('қ', 'к')
        .replace('ғ', 'г')
        .replace('ң', 'н')
        .replace('ү', 'у')
        .replace('ұ', 'у')
        .replace('ө', 'о')
        .replace('һ', 'х')
        .replace('і', 'и')
}
