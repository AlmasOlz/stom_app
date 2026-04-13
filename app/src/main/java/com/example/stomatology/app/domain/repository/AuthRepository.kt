package com.example.stomatology.app.domain.repository

interface AuthRepository {
    suspend fun signInWithEmail(email: String, pass: String): Result<Boolean>
    suspend fun signUpWithEmail(email: String, pass: String): Result<Boolean>
    fun isUserAuthenticated(): Boolean
    fun getCurrentUserId(): String?
}