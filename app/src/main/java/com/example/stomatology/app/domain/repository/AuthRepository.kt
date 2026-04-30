package com.example.stomatology.app.domain.repository

import com.example.stomatology.app.core.firebase.UserRoles

interface AuthRepository {
    suspend fun signInWithEmail(email: String, pass: String): Result<Boolean>

    suspend fun signUpWithEmail(
        email: String,
        pass: String,
        firstName: String,
        lastName: String,
        phone: String,
        requestedRole: String = UserRoles.PATIENT,
        specialty: String = "",
        clinicId: String = ""
    ): Result<Boolean>

    fun isUserAuthenticated(): Boolean
    fun getCurrentUserId(): String?
    fun signOut()

    suspend fun getUserRole(uid: String): Result<String>
    suspend fun getDoctorRequestInfo(uid: String): Result<DoctorRequestInfo>

    suspend fun saveUserProfile(
        uid: String,
        email: String,
        role: String = UserRoles.PATIENT,
        firstName: String = "",
        lastName: String = "",
        phone: String = ""
    ): Result<Unit>
}

data class DoctorRequestInfo(
    val requestedRole: String,
    val requestStatus: String
)
