package com.example.stomatology.app.domain.repository

interface AuthRepository {
    suspend fun signInWithEmail(email: String, pass: String): Result<Boolean>

    suspend fun signUpWithEmail(
        email: String,
        pass: String,
        firstName: String,
        lastName: String,
        phone: String
    ): Result<Boolean>

    fun isUserAuthenticated(): Boolean
    fun getCurrentUserId(): String?
    fun signOut()

    suspend fun getUserRole(uid: String): Result<String>

    suspend fun saveUserProfile(
        uid: String,
        email: String,
        role: String = "patient",
        firstName: String = "",
        lastName: String = "",
        phone: String = ""
    ): Result<Unit>
}