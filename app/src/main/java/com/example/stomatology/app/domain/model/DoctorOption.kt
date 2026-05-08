package com.example.stomatology.app.domain.model

data class DoctorOption(
    val docId: String = "",
    val uid: String = "",
    val authUid: String = "",
    val displayName: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val specialty: String = "",
    val clinicId: String = "",
    val photoUrl: String = "",
    val experienceYears: Int = 0,
    val aboutDoctor: String = ""
) {
    val name: String
        get() = displayName.ifBlank { "$firstName $lastName".trim() }

    val title: String
        get() = specialty.ifBlank { name }
}
