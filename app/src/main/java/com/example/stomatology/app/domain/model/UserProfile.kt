package com.example.stomatology.app.domain.model

import com.example.stomatology.app.core.firebase.UserRoles

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val role: String = UserRoles.PATIENT,
    val firstName: String = "",
    val lastName: String = "",
    val displayName: String = "",
    val phone: String = "",
    val photoUrl: String = "",
    val clinicId: String = "",
    val specialty: String = "",
    val experienceYears: Int = 0,
    val aboutDoctor: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
