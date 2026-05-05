package com.example.stomatology.app.domain.model

data class WorkingDaySchedule(
    val enabled: Boolean = false,
    val startTime: String = "09:00",
    val endTime: String = "18:00"
)

data class ScheduleBreak(
    val dayOfWeek: String = "",
    val startTime: String = "",
    val endTime: String = ""
)

data class DoctorSchedule(
    val doctorId: String = "",
    val clinicId: String = "",
    val slotDurationMin: Int = 60,
    val workingDays: Map<String, WorkingDaySchedule> = emptyMap(),
    val breaks: List<ScheduleBreak> = emptyList(),
    val capacityPerSlot: Int = 1,
    val updatedAt: Long = 0L
)

data class DoctorHoliday(
    val date: String = "",
    val reason: String = "",
    val createdAt: Long = 0L
)

data class AvailableSlot(
    val time: String,
    val isEnabled: Boolean,
    val capacity: Int,
    val bookedCount: Int,
    val isFull: Boolean
)
