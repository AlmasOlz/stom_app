package com.example.stomatology.app.domain.model

fun AppointmentStatus.toUiText(): String {
    return when (this) {
        AppointmentStatus.PENDING -> "Күтілуде"
        AppointmentStatus.CONFIRMED -> "Расталды"
        AppointmentStatus.COMPLETED -> "Аяқталды"
        AppointmentStatus.CANCELLED -> "Бас тартылды"
        AppointmentStatus.NO_SHOW -> "Келмеді"
        AppointmentStatus.RESCHEDULED -> "Қайта жоспарланды"
    }
}
