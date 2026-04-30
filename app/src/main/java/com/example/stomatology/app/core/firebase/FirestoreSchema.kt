package com.example.stomatology.app.core.firebase

object FirestoreCollections {
    const val APPOINTMENTS = "appointments"
    const val CLINICS = "clinics"
    const val USERS = "users"
}

object FirestoreFields {
    const val ADDRESS = "address"
    const val ABOUT_DOCTOR = "aboutDoctor"
    const val CLINIC_ID = "clinicId"
    const val CREATED_AT = "createdAt"
    const val DATE = "date"
    const val DESCRIPTION = "description"
    const val DISPLAY_NAME = "displayName"
    const val DOCTOR_ID = "doctorId"
    const val EMAIL = "email"
    const val FIRST_NAME = "firstName"
    const val EXPERIENCE_YEARS = "experienceYears"
    const val IMAGE_URL = "imageUrl"
    const val LAST_NAME = "lastName"
    const val LATITUDE = "latitude"
    const val LONGITUDE = "longitude"
    const val NAME = "name"
    const val PATIENT_ID = "patientId"
    const val PHONE = "phone"
    const val PHOTO_URL = "photoUrl"
    const val PRICE_FROM = "priceFrom"
    const val PRICE_LIST = "priceList"
    const val RATING = "rating"
    const val REQUESTED_ROLE = "requestedRole"
    const val REQUEST_STATUS = "requestStatus"
    const val REVIEWS = "reviews"
    const val ROLE = "role"
    const val SERVICES = "services"
    const val SPECIALTY = "specialty"
    const val STATUS = "status"
    const val TIME = "time"
    const val UID = "uid"
    const val UPDATED_AT = "updatedAt"
}

object UserRoles {
    const val ADMIN = "admin"
    const val DOCTOR = "doctor"
    const val PATIENT = "patient"
}

object RoleRequestStatus {
    const val APPROVED = "approved"
    const val NONE = "none"
    const val PENDING = "pending"
    const val REJECTED = "rejected"
}
