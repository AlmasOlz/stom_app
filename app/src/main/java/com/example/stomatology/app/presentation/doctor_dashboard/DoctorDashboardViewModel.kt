package com.example.stomatology.app.presentation.doctor_dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stomatology.app.core.firebase.FirestoreCollections
import com.example.stomatology.app.core.firebase.FirestoreFields
import com.example.stomatology.app.domain.model.Appointment
import com.example.stomatology.app.domain.model.AppointmentStatus
import com.example.stomatology.app.domain.repository.AppointmentRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
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

    private companion object {
        const val TAG = "DoctorDashboardVM"
        const val ERROR_LOAD_APPOINTMENTS = "Жазбаларды жүктеу кезінде қате пайда болды."
        const val ERROR_RETRY = "Қате пайда болды. Қайталап көріңіз."
    }

    private val _uiState = MutableStateFlow(DoctorDashboardUiState())
    val uiState: StateFlow<DoctorDashboardUiState> = _uiState

    private val doctorId: String
        get() = auth.currentUser?.uid.orEmpty()

    init {
        loadDoctorProfile()
        observeAppointments()
    }

    fun retryLoadAppointments() {
        _uiState.update { state -> state.copy(isLoading = true, error = null) }
        observeAppointments()
    }

    private fun loadDoctorProfile() {
        val currentDoctorId = doctorId
        val userDocPath = "${FirestoreCollections.USERS}/$currentDoctorId"
        Log.d(TAG, "load_profile authUid=$currentDoctorId userDocPath=$userDocPath")

        if (currentDoctorId.isBlank()) {
            _uiState.update {
                it.copy(isLoading = false, error = ERROR_RETRY)
            }
            return
        }

        viewModelScope.launch {
            try {
                val snapshot = firestore.collection(FirestoreCollections.USERS)
                    .document(currentDoctorId)
                    .get()
                    .await()

                if (!snapshot.exists()) {
                    Log.w(TAG, "users_doc_missing path=$userDocPath")
                    val fallbackSnapshot = firestore.collection(FirestoreCollections.USERS)
                        .whereEqualTo(FirestoreFields.AUTH_UID, currentDoctorId)
                        .limit(1)
                        .get()
                        .await()
                    val fallbackDocId = fallbackSnapshot.documents.firstOrNull()?.id
                    if (fallbackDocId != null) {
                        Log.w(
                            TAG,
                            "doctor_profile_doc_id_mismatch authUid=$currentDoctorId foundDocId=$fallbackDocId expectedDocId=$currentDoctorId"
                        )
                    }
                }

                val firstName = snapshot.getString(FirestoreFields.FIRST_NAME).orEmpty()
                val lastName = snapshot.getString(FirestoreFields.LAST_NAME).orEmpty()
                val displayName = snapshot.getString(FirestoreFields.DISPLAY_NAME)
                    ?.takeIf { value -> value.isNotBlank() }
                    ?: "$firstName $lastName".trim()

                _uiState.update {
                    it.copy(
                        doctorName = displayName.ifBlank { "Дәрігер" },
                        doctorSpecialty = snapshot.getString(FirestoreFields.SPECIALTY).orEmpty()
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "load_profile_failed authUid=$currentDoctorId path=$userDocPath", e)
                _uiState.update {
                    it.copy(doctorName = it.doctorName.ifBlank { "Дәрігер" })
                }
            }
        }
    }

    private fun observeAppointments() {
        val currentDoctorId = doctorId
        val userDocPath = "${FirestoreCollections.USERS}/$currentDoctorId"
        Log.d(
            TAG,
            "observe_appointments authUid=${auth.currentUser?.uid.orEmpty()} userDocPath=$userDocPath queryDoctorId=$currentDoctorId"
        )

        if (currentDoctorId.isBlank()) {
            _uiState.update {
                it.copy(isLoading = false, error = ERROR_LOAD_APPOINTMENTS)
            }
            return
        }

        viewModelScope.launch {
            appointmentRepository.getAppointmentsForDoctor(currentDoctorId)
                .catch { throwable ->
                    logDoctorDashboardError(
                        throwable = throwable,
                        currentAuthUid = auth.currentUser?.uid.orEmpty(),
                        queryDoctorId = currentDoctorId,
                        userDocPath = userDocPath
                    )
                    _uiState.update { state ->
                        state.copy(isLoading = false, error = mapLoadError(throwable))
                    }
                }
                .collectLatest { appointments ->
                    if (appointments.isEmpty()) {
                        Log.i(
                            TAG,
                            "appointments_empty authUid=${auth.currentUser?.uid.orEmpty()} queryDoctorId=$currentDoctorId"
                        )
                    }

                    val mismatchCount = appointments.count { it.doctorId != currentDoctorId }
                    if (mismatchCount > 0) {
                        Log.w(TAG, "doctorId_mismatch_count=$mismatchCount queryDoctorId=$currentDoctorId")
                    }

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
                        (item.status == AppointmentStatus.CONFIRMED ||
                            item.status == AppointmentStatus.RESCHEDULED) &&
                            parseAppointmentDate(item.date) == today
                    }

                    val upcoming = sortedAppointments.firstOrNull { item ->
                        val date = parseAppointmentDate(item.date) ?: return@firstOrNull false
                        val time = parseTimeToMinutes(item.time) ?: Int.MAX_VALUE
                        val isActiveStatus =
                            item.status == AppointmentStatus.CONFIRMED ||
                                item.status == AppointmentStatus.PENDING ||
                                item.status == AppointmentStatus.RESCHEDULED

                        isActiveStatus && (date > today || (date == today && time >= nowTimeMinutes))
                    } ?: sortedAppointments.firstOrNull { item ->
                        item.status == AppointmentStatus.CONFIRMED ||
                            item.status == AppointmentStatus.PENDING ||
                            item.status == AppointmentStatus.RESCHEDULED
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

    private fun mapLoadError(throwable: Throwable): String {
        val firestoreError = throwable as? FirebaseFirestoreException
        return when (firestoreError?.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Жазбаларды көруге рұқсат жоқ."
            FirebaseFirestoreException.Code.UNAVAILABLE -> "Интернет байланысын тексеріңіз."
            FirebaseFirestoreException.Code.FAILED_PRECONDITION -> ERROR_LOAD_APPOINTMENTS
            else -> ERROR_LOAD_APPOINTMENTS
        }
    }

    private fun logDoctorDashboardError(
        throwable: Throwable,
        currentAuthUid: String,
        queryDoctorId: String,
        userDocPath: String
    ) {
        val firestoreError = throwable as? FirebaseFirestoreException
        if (firestoreError != null) {
            when (firestoreError.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                    Log.e(
                        TAG,
                        "appointments_load_failed reason=permission_denied authUid=$currentAuthUid userDocPath=$userDocPath queryDoctorId=$queryDoctorId message=${firestoreError.message}",
                        firestoreError
                    )
                }

                FirebaseFirestoreException.Code.FAILED_PRECONDITION -> {
                    Log.e(
                        TAG,
                        "appointments_load_failed reason=missing_index_or_precondition authUid=$currentAuthUid userDocPath=$userDocPath queryDoctorId=$queryDoctorId message=${firestoreError.message}",
                        firestoreError
                    )
                }

                else -> {
                    Log.e(
                        TAG,
                        "appointments_load_failed reason=firestore_${firestoreError.code.name.lowercase()} authUid=$currentAuthUid userDocPath=$userDocPath queryDoctorId=$queryDoctorId message=${firestoreError.message}",
                        firestoreError
                    )
                }
            }
        } else {
            Log.e(
                TAG,
                "appointments_load_failed reason=unknown authUid=$currentAuthUid userDocPath=$userDocPath queryDoctorId=$queryDoctorId message=${throwable.message}",
                throwable
            )
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


