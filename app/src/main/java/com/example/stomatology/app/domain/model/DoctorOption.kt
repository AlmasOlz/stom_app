package com.example.stomatology.app.domain.model

data class DoctorOption(
    val uid: String = "",
    val displayName: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = ""
) {
    val name: String
        get() = displayName.ifBlank { "$firstName $lastName".trim() }
}