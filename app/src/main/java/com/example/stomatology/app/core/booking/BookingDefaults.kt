package com.example.stomatology.app.core.booking

object BookingDefaults {
    // Казак тілінде "сағат" дұрыс жазылуы
    const val DEFAULT_DURATION = "1 \u0441\u0430\u0493\u0430\u0442"
    const val DATE_OPTION_DAYS = 5

    /**
     * Қызметтер тізімі (басты бетпен сәйкес) — 1-қадамда таңдау үшін.
     * Сүзгі логикасы [requiredSpecialtyKeywords] мамандық атауларымен жұмыс істейді.
     */
    val BOOKING_SERVICE_TITLES: List<String> = listOf(
        "Тіс жұлу",
        "Протездеу",
        "Пломба / Канал",
        "Имплант",
        "Брекет"
    )

    val DEFAULT_TIME_SLOTS = listOf(
        "09:00",
        "10:00",
        "11:00",
        "14:00",
        "15:00",
        "16:00"
    )

    val FULL_DAY_TIME_SLOTS = listOf(
        "08:00",
        "09:00",
        "10:00",
        "11:00",
        "12:00",
        "13:30",
        "14:30",
        "15:00",
        "16:00",
        "17:00",
        "18:00",
        "19:00"
    )
}
