package com.example.stomatology.app.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Қабылдау күн+уақыт (Firestore `date` / `time` өрістері). */
fun Appointment.scheduleDateTime(): LocalDateTime? {
    val d = parseAppointmentDate(date) ?: return null
    val t = parseAppointmentTime(time) ?: LocalTime.MIDNIGHT
    return LocalDateTime.of(d, t)
}

fun Appointment.scheduleInstantMillis(): Long {
    return scheduleDateTime()
        ?.atZone(ZoneId.systemDefault())
        ?.toInstant()
        ?.toEpochMilli()
        ?: maxOf(updatedAt, createdAt)
}

/** «Менің жазбаларым» → Алдағы табы. */
fun Appointment.isRecordsUpcomingTab(now: LocalDateTime = LocalDateTime.now(ZoneId.systemDefault())): Boolean {
    when (status) {
        AppointmentStatus.CANCELLED,
        AppointmentStatus.COMPLETED,
        AppointmentStatus.NO_SHOW -> return false
        else -> {
            val dt = scheduleDateTime() ?: return true
            return !dt.isBefore(now)
        }
    }
}

/** «Менің жазбаларым» → Өткен табы. */
fun Appointment.isRecordsPastTab(now: LocalDateTime = LocalDateTime.now(ZoneId.systemDefault())): Boolean {
    when (status) {
        AppointmentStatus.COMPLETED,
        AppointmentStatus.CANCELLED,
        AppointmentStatus.NO_SHOW -> return true
        else -> {
            val dt = scheduleDateTime() ?: return false
            return dt.isBefore(now)
        }
    }
}

private fun parseAppointmentDate(raw: String): LocalDate? {
    val value = raw.trim()
    if (value.isBlank()) return null
    val formatters = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("dd.MM.yyyy")
    )
    return formatters.firstNotNullOfOrNull { fmt ->
        runCatching { LocalDate.parse(value, fmt) }.getOrNull()
    }
}

private fun parseAppointmentTime(raw: String): LocalTime? {
    val value = raw.trim()
    if (value.isBlank()) return null
    val patterns = listOf("HH:mm", "H:mm")
    return patterns.firstNotNullOfOrNull { pattern ->
        runCatching {
            LocalTime.parse(value, DateTimeFormatter.ofPattern(pattern))
        }.getOrNull()
    }
}
