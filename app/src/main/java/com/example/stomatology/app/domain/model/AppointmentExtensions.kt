package com.example.stomatology.app.domain.model

fun AppointmentStatus.toUiText(): String {
    return when (this) {
        AppointmentStatus.PENDING -> "Ожидает"
        AppointmentStatus.ACCEPTED -> "Принята"
        AppointmentStatus.REJECTED -> "Отклонена"
        AppointmentStatus.COMPLETED -> "Завершена"
        AppointmentStatus.CANCELLED -> "Отменена"
    }
}