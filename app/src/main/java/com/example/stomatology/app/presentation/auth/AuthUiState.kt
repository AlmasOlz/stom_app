package com.example.stomatology.app.presentation.auth

import com.example.stomatology.app.core.firebase.UserRoles

data class AuthClinicOption(
    val id: String,
    val name: String
)

data class AuthUiState(
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val email: String = "",
    val password: String = "",
    val requestedRole: String = UserRoles.PATIENT,
    val specialty: String = "",
    val clinicId: String = "",
    val clinics: List<AuthClinicOption> = emptyList(),
    val isClinicsLoading: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val doctorRequestSubmitted: Boolean = false,
    val isSuccess: Boolean = false,
    val role: String? = null,
    val isSessionChecked: Boolean = false
)
