package com.example.stomatology.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stomatology.app.core.firebase.FirestoreCollections
import com.example.stomatology.app.core.firebase.FirestoreFields
import com.example.stomatology.app.core.firebase.RoleRequestStatus
import com.example.stomatology.app.core.firebase.UserRoles
import com.example.stomatology.app.domain.repository.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state

    fun bootstrapSession() {
        if (_state.value.isSessionChecked) return

        if (!authRepository.isUserAuthenticated()) {
            _state.value = _state.value.copy(isSessionChecked = true)
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, message = null)

            val uid = authRepository.getCurrentUserId()
            if (uid == null) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    isSessionChecked = true,
                    error = "User id not found"
                )
                return@launch
            }

            val roleResult = authRepository.getUserRole(uid)
            if (roleResult.isFailure) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    isSessionChecked = true,
                    error = mapAuthError(roleResult.exceptionOrNull()?.message, "Failed to load user role")
                )
                return@launch
            }

            val role = roleResult.getOrNull().orEmpty()
            if (isPendingDoctorRequest(uid, role)) {
                authRepository.signOut()
                _state.value = _state.value.copy(
                    isLoading = false,
                    isSessionChecked = true,
                    isSuccess = false,
                    role = null,
                    error = "Дәрігер өтінімі әлі әкімшімен расталмаған."
                )
                return@launch
            }

            _state.value = _state.value.copy(
                isLoading = false,
                isSessionChecked = true,
                isSuccess = true,
                role = role
            )
        }
    }

    fun onFirstNameChange(value: String) {
        _state.value = _state.value.copy(firstName = value)
    }

    fun onLastNameChange(value: String) {
        _state.value = _state.value.copy(lastName = value)
    }

    fun onPhoneChange(value: String) {
        _state.value = _state.value.copy(phone = value)
    }

    fun onEmailChange(value: String) {
        _state.value = _state.value.copy(email = value)
    }

    fun onPasswordChange(value: String) {
        _state.value = _state.value.copy(password = value)
    }

    fun onRequestedRoleChange(value: String) {
        val role = if (value == UserRoles.DOCTOR) UserRoles.DOCTOR else UserRoles.PATIENT
        _state.value = _state.value.copy(
            requestedRole = role,
            message = null,
            doctorRequestSubmitted = false
        )
        if (role == UserRoles.DOCTOR) {
            loadClinicsIfNeeded()
        }
    }

    fun onSpecialtyChange(value: String) {
        _state.value = _state.value.copy(specialty = value)
    }

    fun onClinicIdChange(value: String) {
        _state.value = _state.value.copy(clinicId = value)
    }

    fun clearSuccess() {
        _state.value = _state.value.copy(isSuccess = false)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null, doctorRequestSubmitted = false)
    }

    fun loadClinicsIfNeeded() {
        if (_state.value.clinics.isNotEmpty() || _state.value.isClinicsLoading) {
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isClinicsLoading = true, error = null)
            try {
                val snapshot = firestore.collection(FirestoreCollections.CLINICS).get().await()
                val clinics = snapshot.documents
                    .mapNotNull { doc ->
                        val name = doc.getString(FirestoreFields.NAME).orEmpty().trim()
                        if (name.isBlank()) null else AuthClinicOption(id = doc.id, name = name)
                    }
                    .sortedBy { clinic -> clinic.name.lowercase() }

                _state.value = _state.value.copy(
                    clinics = clinics,
                    isClinicsLoading = false,
                    error = if (clinics.isEmpty()) "Базада клиникалар табылмады. Әкімші панелінен клиника қосыңыз." else null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isClinicsLoading = false,
                    error = mapAuthError(e.message, "Клиникаларды жүктеу мүмкін болмады.")
                )
            }
        }
    }
    fun login() {
        val email = normalizeEmailInput(_state.value.email)
        val pass = _state.value.password.trim()

        if (email.isBlank() || pass.isBlank()) {
            _state.value = _state.value.copy(error = "Электрондық пошта мен құпия сөзді енгізіңіз.")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, message = null)

            val result = authRepository.signInWithEmail(email, pass)
            if (result.isFailure) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = mapAuthError(result.exceptionOrNull()?.message, "Кіру мүмкін болмады.")
                )
                return@launch
            }

            val uid = authRepository.getCurrentUserId()
            if (uid == null) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Қолданушы табылмады. Қайта кіріңіз."
                )
                return@launch
            }

            val roleResult = authRepository.getUserRole(uid)
            if (roleResult.isFailure) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = mapAuthError(roleResult.exceptionOrNull()?.message, "Қолданушы рөлін жүктеу мүмкін болмады.")
                )
                return@launch
            }

            val role = roleResult.getOrNull().orEmpty()
            if (isPendingDoctorRequest(uid, role)) {
                authRepository.signOut()
                _state.value = _state.value.copy(
                    isLoading = false,
                    isSuccess = false,
                    role = null,
                    error = "Дәрігер өтінімі әлі әкімшімен расталмаған."
                )
                return@launch
            }

            _state.value = _state.value.copy(
                password = "",
                isLoading = false,
                isSessionChecked = true,
                isSuccess = true,
                role = role,
                error = null
            )
        }
    }

    fun signUp() {
        val firstName = _state.value.firstName.trim()
        val lastName = _state.value.lastName.trim()
        val phone = _state.value.phone.trim()
        val email = normalizeEmailInput(_state.value.email)
        val pass = _state.value.password.trim()
        val requestedRole = _state.value.requestedRole
        val specialty = _state.value.specialty.trim()
        val clinicId = _state.value.clinicId.trim()

        if (firstName.isBlank() || lastName.isBlank() || phone.isBlank() || email.isBlank() || pass.isBlank()) {
            _state.value = _state.value.copy(error = "Барлық өрістерді толтырыңыз.")
            return
        }

        if (pass.length < 6) {
            _state.value = _state.value.copy(error = "Құпия сөз кемінде 6 таңбадан тұруы керек.")
            return
        }

        if (requestedRole == UserRoles.DOCTOR) {
            if (specialty.isBlank()) {
                _state.value = _state.value.copy(error = "Дәрігер мамандығын көрсетіңіз.")
                return
            }
            if (clinicId.isBlank()) {
                _state.value = _state.value.copy(error = "Клиниканы таңдаңыз.")
                return
            }
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, message = null)

            val result = authRepository.signUpWithEmail(
                email = email,
                pass = pass,
                firstName = firstName,
                lastName = lastName,
                phone = phone,
                requestedRole = requestedRole,
                specialty = specialty,
                clinicId = clinicId
            )

            if (result.isFailure) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = mapAuthError(result.exceptionOrNull()?.message, "Тіркелу мүмкін болмады.")
                )
                return@launch
            }

            if (requestedRole == UserRoles.DOCTOR) {
                authRepository.signOut()
                _state.value = _state.value.copy(
                    password = "",
                    isLoading = false,
                    isSuccess = false,
                    doctorRequestSubmitted = true,
                    message = "Дәрігер өтінімі жіберілді. Әкімші растауын күтіңіз."
                )
            } else {
                _state.value = _state.value.copy(
                    password = "",
                    isLoading = false,
                    isSessionChecked = true,
                    isSuccess = true,
                    role = UserRoles.PATIENT
                )
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _state.value = AuthUiState(isSessionChecked = true)
    }

    private suspend fun isPendingDoctorRequest(uid: String, role: String): Boolean {
        if (role != UserRoles.PATIENT) return false
        val doctorRequest = authRepository.getDoctorRequestInfo(uid)
        val info = doctorRequest.getOrNull()
        return info?.requestedRole == UserRoles.DOCTOR &&
            info.requestStatus == RoleRequestStatus.PENDING
    }
    private fun mapAuthError(rawMessage: String?, fallback: String): String {
        val message = rawMessage.orEmpty()
        val lower = message.lowercase()

        return when {
            lower.contains("blocked all requests from this device due to unusual activity") ||
                lower.contains("we have blocked all requests from this device") ||
                lower.contains("too many requests") ||
                lower.contains("try again later") -> {
                "Бұл құрылғыдан сұраулар уақытша бұғатталды. Біраз уақыттан кейін қайта кіріп көріңіз."
            }

            lower.contains("email address is badly formatted") ||
                lower.contains("badly formatted") ||
                lower.contains("invalid email") ||
                lower.contains("malformed") -> {
                "Электрондық поштаның пішімі қате."
            }

            lower.contains("email address is already in use") || lower.contains("email already in use") -> {
                "Бұл email бұрын тіркелген."
            }

            lower.contains("password is invalid") ||
                lower.contains("there is no user record") ||
                lower.contains("user not found") ||
                lower.contains("the supplied auth credential is incorrect") -> {
                "Email немесе құпия сөз қате."
            }

            lower.contains("missing or insufficient permissions") || lower.contains("permission-denied") -> {
                "Firestore рұқсаты жеткіліксіз."
            }

            lower.contains("network error") || lower.contains("timeout") -> {
                "Интернет байланысын тексеріңіз."
            }

            else -> message.ifBlank { fallback }
        }
    }

    private fun normalizeEmailInput(raw: String): String {
        return raw
            .trim()
            .replace("\u00A0", "")
            .replace("\\s+".toRegex(), "")
    }
}
