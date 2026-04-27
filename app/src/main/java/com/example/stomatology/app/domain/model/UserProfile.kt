package com.example.stomatology.app.domain.model

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val role: String = "patient",
    val firstName: String = "",
    val lastName: String = "",
    val displayName: String = "",
    val phone: String = "",
    val createdAt: Long = 0L
)