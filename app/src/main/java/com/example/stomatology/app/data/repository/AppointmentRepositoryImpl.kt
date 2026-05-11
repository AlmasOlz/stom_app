package com.example.stomatology.app.data.repository

import android.util.Log
import com.example.stomatology.app.core.booking.BookingDefaults
import com.example.stomatology.app.core.firebase.FirestoreCollections
import com.example.stomatology.app.core.firebase.FirestoreFields
import com.example.stomatology.app.domain.model.Appointment
import com.example.stomatology.app.domain.model.AppointmentStatus
import com.example.stomatology.app.domain.model.AvailableSlot
import com.example.stomatology.app.domain.model.DoctorSchedule
import com.example.stomatology.app.domain.model.ScheduleBreak
import com.example.stomatology.app.domain.model.WorkingDaySchedule
import com.example.stomatology.app.domain.repository.AppointmentRepository
import com.example.stomatology.app.domain.repository.AppointmentValidationException
import com.example.stomatology.app.domain.repository.SlotAlreadyBookedException
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class AppointmentRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AppointmentRepository {

    override fun getAppointmentsForDoctor(doctorId: String): Flow<List<Appointment>> {
        Log.d(
            APPOINTMENTS_DEBUG_TAG,
            "query=appointments where doctorId==$doctorId projectId=${firestore.app.options.projectId}"
        )
        return firestore.collection(FirestoreCollections.APPOINTMENTS)
            .whereEqualTo(FirestoreFields.DOCTOR_ID, doctorId)
            .orderBy(FirestoreFields.CREATED_AT, Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                mapSnapshotToAppointments(
                    snapshot = snapshot,
                    queryName = "doctor",
                    queryField = FirestoreFields.DOCTOR_ID,
                    queryValue = doctorId
                )
            }
    }

    override fun getAppointmentsForPatient(patientId: String): Flow<List<Appointment>> {
        Log.d(
            APPOINTMENTS_DEBUG_TAG,
            "query=appointments where patientId==$patientId projectId=${firestore.app.options.projectId}"
        )
        return firestore.collection(FirestoreCollections.APPOINTMENTS)
            .whereEqualTo(FirestoreFields.PATIENT_ID, patientId)
            .orderBy(FirestoreFields.CREATED_AT, Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                mapSnapshotToAppointments(
                    snapshot = snapshot,
                    queryName = "patient",
                    queryField = FirestoreFields.PATIENT_ID,
                    queryValue = patientId
                )
            }
    }

    override suspend fun createAppointment(appointment: Appointment, slotCapacity: Int) {
        validateBookingInput(
            doctorId = appointment.doctorId,
            clinicId = appointment.clinicId,
            date = appointment.date,
            time = appointment.time
        )

        val now = System.currentTimeMillis()
        val appointmentId = appointment.id.ifBlank { UUID.randomUUID().toString() }
        val appointmentRef = appointmentsCollection().document(appointmentId)
        val slotRef = slotRef(appointment.doctorId, appointment.date, appointment.time)
        val statusHistoryRef = appointmentRef.collection(FirestoreCollections.STATUS_HISTORY).document()
        val targetCapacity = slotCapacity.coerceAtLeast(1)

        firestore.runTransaction { transaction ->
            val slotSnapshot = transaction.get(slotRef)
            val bookedCount = slotSnapshot.getLong(FirestoreFields.BOOKED_COUNT)?.toInt() ?: 0
            val capacity = (slotSnapshot.getLong(FirestoreFields.CAPACITY)?.toInt() ?: targetCapacity)
                .coerceAtLeast(1)

            if (bookedCount >= capacity) {
                throw SlotAlreadyBookedException()
            }

            val nextSlotPayload = upsertSlotPayload(
                slotSnapshot = slotSnapshot,
                doctorId = appointment.doctorId,
                clinicId = appointment.clinicId,
                date = appointment.date,
                time = appointment.time,
                capacity = capacity,
                appointmentId = appointmentId,
                increment = true,
                now = now
            )

            if (slotSnapshot.exists()) {
                transaction.update(slotRef, nextSlotPayload)
            } else {
                transaction.set(slotRef, nextSlotPayload)
            }

            val normalizedAppointment = appointment.copy(
                id = appointmentId,
                status = AppointmentStatus.PENDING,
                cancelledBy = null,
                cancelReason = null,
                cancelledAt = null,
                completedAt = null,
                rescheduledFromId = null,
                previousDate = null,
                previousTime = null,
                statusChangedBy = appointment.patientId,
                createdAt = if (appointment.createdAt > 0) appointment.createdAt else now,
                updatedAt = now
            )

            transaction.set(appointmentRef, normalizedAppointment.toFirestoreMap())
            transaction.set(
                statusHistoryRef,
                buildStatusHistoryMap(
                    from = null,
                    to = AppointmentStatus.PENDING.name,
                    by = appointment.patientId,
                    reason = null,
                    at = now
                )
            )
        }.await()
    }

    override suspend fun updateStatus(
        appointmentId: String,
        status: AppointmentStatus,
        changedBy: String,
        reason: String?,
        rescheduledFromId: String?
    ) {
        if (appointmentId.isBlank()) {
            throw AppointmentValidationException("appointmentId бос")
        }
        if (changedBy.isBlank()) {
            throw AppointmentValidationException("Статусты кім өзгерткені белгісіз")
        }

        when (status) {
            AppointmentStatus.CANCELLED -> {
                cancelAppointmentWithSlotRelease(
                    appointmentId = appointmentId,
                    changedBy = changedBy,
                    reason = reason ?: "Себеп көрсетілмеген"
                )
                return
            }

            AppointmentStatus.NO_SHOW -> {
                markNoShow(appointmentId = appointmentId, changedBy = changedBy, reason = reason)
                return
            }

            else -> Unit
        }

        val appointmentRef = appointmentsCollection().document(appointmentId)
        val historyRef = appointmentRef.collection(FirestoreCollections.STATUS_HISTORY).document()
        val now = System.currentTimeMillis()

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(appointmentRef)
            if (!snapshot.exists()) {
                throw AppointmentValidationException("Жазба табылмады")
            }

            val current = snapshot.toAppointment()
            val updates = mutableMapOf<String, Any>(
                FirestoreFields.STATUS to status.name,
                FirestoreFields.STATUS_CHANGED_BY to changedBy,
                FirestoreFields.UPDATED_AT to now
            )

            if (status == AppointmentStatus.COMPLETED) {
                updates[FirestoreFields.COMPLETED_AT] = now
            }

            if (status == AppointmentStatus.RESCHEDULED && !rescheduledFromId.isNullOrBlank()) {
                updates[FirestoreFields.RESCHEDULED_FROM_ID] = rescheduledFromId
            }

            transaction.update(appointmentRef, updates)
            transaction.set(
                historyRef,
                buildStatusHistoryMap(
                    from = current.status.name,
                    to = status.name,
                    by = changedBy,
                    reason = reason,
                    at = now
                )
            )
        }.await()
    }

    override suspend fun generateAvailableSlots(
        doctorId: String,
        clinicId: String,
        date: String
    ): List<AvailableSlot> {
        if (doctorId.isBlank() || date.isBlank()) return emptyList()

        val scheduleDoc = firestore.collection(FirestoreCollections.DOCTOR_SCHEDULES)
            .document(doctorId)
            .get()
            .await()
        val holidayDoc = firestore.collection(FirestoreCollections.DOCTOR_SCHEDULES)
            .document(doctorId)
            .collection(FirestoreCollections.HOLIDAYS)
            .document(normalizeSlotDate(date))
            .get()
            .await()

        val slotSnapshot = firestore.collection(FirestoreCollections.APPOINTMENT_SLOTS)
            .whereEqualTo(FirestoreFields.DOCTOR_ID, doctorId)
            .whereEqualTo(FirestoreFields.DATE, date)
            .get()
            .await()
            .documents
        val slotsByTime = slotSnapshot.associateBy { it.getString(FirestoreFields.TIME).orEmpty() }

        if (!scheduleDoc.exists()) {
            return BookingDefaults.DEFAULT_TIME_SLOTS.map { time ->
                val doc = slotsByTime[time]
                val capacity = (doc?.getLong(FirestoreFields.CAPACITY)?.toInt() ?: 1).coerceAtLeast(1)
                val booked = doc?.getLong(FirestoreFields.BOOKED_COUNT)?.toInt() ?: 0
                val full = booked >= capacity
                AvailableSlot(
                    time = time,
                    isEnabled = !full,
                    capacity = capacity,
                    bookedCount = booked,
                    isFull = full
                )
            }
        }

        if (holidayDoc.exists()) {
            return emptyList()
        }

        val schedule = scheduleDoc.toDoctorSchedule(doctorId = doctorId, fallbackClinicId = clinicId)
        val dayKey = dayOfWeekKey(date) ?: return emptyList()
        val daySchedule = schedule.workingDays[dayKey] ?: return emptyList()
        if (!daySchedule.enabled) {
            return emptyList()
        }

        val baseTimes = generateTimesBySchedule(
            startTime = daySchedule.startTime,
            endTime = daySchedule.endTime,
            slotDurationMin = schedule.slotDurationMin
        )
        if (baseTimes.isEmpty()) {
            return emptyList()
        }

        val dayBreaks = schedule.breaks.filter { item ->
            item.dayOfWeek.equals(dayKey, ignoreCase = true)
        }

        return baseTimes
            .filterNot { time ->
                intersectsBreak(
                    slotStart = time,
                    slotDurationMin = schedule.slotDurationMin,
                    breaks = dayBreaks
                )
            }
            .map { time ->
                val doc = slotsByTime[time]
                val capacity = (doc?.getLong(FirestoreFields.CAPACITY)?.toInt()
                    ?: schedule.capacityPerSlot).coerceAtLeast(1)
                val booked = doc?.getLong(FirestoreFields.BOOKED_COUNT)?.toInt() ?: 0
                val full = booked >= capacity
                AvailableSlot(
                    time = time,
                    isEnabled = !full,
                    capacity = capacity,
                    bookedCount = booked,
                    isFull = full
                )
            }
    }

    override suspend fun rescheduleAppointment(
        appointmentId: String,
        newDate: String,
        newTime: String,
        changedBy: String,
        reason: String?
    ) {
        if (appointmentId.isBlank()) throw AppointmentValidationException("Жазба ID бос")
        if (newDate.isBlank() || newTime.isBlank()) {
            throw AppointmentValidationException("Жаңа күн/уақыт міндетті")
        }
        if (changedBy.isBlank()) throw AppointmentValidationException("Пайдаланушы табылмады")

        val appointmentRef = appointmentsCollection().document(appointmentId)
        val now = System.currentTimeMillis()

        firestore.runTransaction { transaction ->
            val appointmentSnapshot = transaction.get(appointmentRef)
            if (!appointmentSnapshot.exists()) {
                throw AppointmentValidationException("Жазба табылмады")
            }
            val current = appointmentSnapshot.toAppointment()
            validateRescheduleStatus(current.status)

            if (current.date == newDate && current.time == newTime) {
                throw AppointmentValidationException("Жаңа уақыт бұрынғысымен бірдей")
            }

            val oldSlotRef = slotRef(current.doctorId, current.date, current.time)
            val newSlotRef = slotRef(current.doctorId, newDate, newTime)
            val oldSlotSnapshot = transaction.get(oldSlotRef)
            val newSlotSnapshot = transaction.get(newSlotRef)

            val scheduleDocRef = firestore.collection(FirestoreCollections.DOCTOR_SCHEDULES)
                .document(current.doctorId)
            val scheduleSnapshot = transaction.get(scheduleDocRef)
            val scheduleCapacity = (scheduleSnapshot.getLong(FirestoreFields.CAPACITY_PER_SLOT)?.toInt()
                ?: 1).coerceAtLeast(1)
            val newCapacity = (newSlotSnapshot.getLong(FirestoreFields.CAPACITY)?.toInt()
                ?: scheduleCapacity).coerceAtLeast(1)
            val newBooked = newSlotSnapshot.getLong(FirestoreFields.BOOKED_COUNT)?.toInt() ?: 0

            if (newBooked >= newCapacity) {
                throw SlotAlreadyBookedException()
            }

            if (oldSlotSnapshot.exists()) {
                val oldPayload = upsertSlotPayload(
                    slotSnapshot = oldSlotSnapshot,
                    doctorId = current.doctorId,
                    clinicId = current.clinicId,
                    date = current.date,
                    time = current.time,
                    capacity = (oldSlotSnapshot.getLong(FirestoreFields.CAPACITY)?.toInt() ?: scheduleCapacity)
                        .coerceAtLeast(1),
                    appointmentId = appointmentId,
                    increment = false,
                    now = now
                )
                transaction.update(oldSlotRef, oldPayload)
            }

            val newPayload = upsertSlotPayload(
                slotSnapshot = newSlotSnapshot,
                doctorId = current.doctorId,
                clinicId = current.clinicId,
                date = newDate,
                time = newTime,
                capacity = newCapacity,
                appointmentId = appointmentId,
                increment = true,
                now = now
            )

            if (newSlotSnapshot.exists()) {
                transaction.update(newSlotRef, newPayload)
            } else {
                transaction.set(newSlotRef, newPayload)
            }

            transaction.update(
                appointmentRef,
                mapOf(
                    FirestoreFields.DATE to newDate,
                    FirestoreFields.TIME to newTime,
                    FirestoreFields.STATUS to AppointmentStatus.RESCHEDULED.name,
                    FirestoreFields.PREVIOUS_DATE to current.date,
                    FirestoreFields.PREVIOUS_TIME to current.time,
                    FirestoreFields.RESCHEDULED_FROM_ID to buildSlotId(
                        doctorId = current.doctorId,
                        date = current.date,
                        time = current.time
                    ),
                    FirestoreFields.STATUS_CHANGED_BY to changedBy,
                    FirestoreFields.UPDATED_AT to now
                )
            )

            transaction.set(
                appointmentRef.collection(FirestoreCollections.STATUS_HISTORY).document(),
                buildStatusHistoryMap(
                    from = current.status.name,
                    to = AppointmentStatus.RESCHEDULED.name,
                    by = changedBy,
                    reason = reason ?: "Уақыт өзгертілді",
                    at = now
                )
            )
        }.await()
    }

    override suspend fun markNoShow(
        appointmentId: String,
        changedBy: String,
        reason: String?
    ) {
        if (appointmentId.isBlank()) throw AppointmentValidationException("Жазба ID бос")
        if (changedBy.isBlank()) throw AppointmentValidationException("Пайдаланушы табылмады")

        val appointmentRef = appointmentsCollection().document(appointmentId)
        val now = System.currentTimeMillis()

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(appointmentRef)
            if (!snapshot.exists()) {
                throw AppointmentValidationException("Жазба табылмады")
            }
            val current = snapshot.toAppointment()
            if (current.status == AppointmentStatus.CANCELLED || current.status == AppointmentStatus.COMPLETED) {
                throw AppointmentValidationException("Бұл жазбаға NO_SHOW қоюға болмайды")
            }

            transaction.update(
                appointmentRef,
                mapOf(
                    FirestoreFields.STATUS to AppointmentStatus.NO_SHOW.name,
                    FirestoreFields.STATUS_CHANGED_BY to changedBy,
                    FirestoreFields.UPDATED_AT to now
                )
            )
            transaction.set(
                appointmentRef.collection(FirestoreCollections.STATUS_HISTORY).document(),
                buildStatusHistoryMap(
                    from = current.status.name,
                    to = AppointmentStatus.NO_SHOW.name,
                    by = changedBy,
                    reason = reason,
                    at = now
                )
            )
        }.await()
    }

    override suspend fun cancelAppointmentWithSlotRelease(
        appointmentId: String,
        changedBy: String,
        reason: String
    ) {
        if (appointmentId.isBlank()) throw AppointmentValidationException("Жазба ID бос")
        if (changedBy.isBlank()) throw AppointmentValidationException("Пайдаланушы табылмады")
        if (reason.isBlank()) throw AppointmentValidationException("Бас тарту себебін жазыңыз")

        val appointmentRef = appointmentsCollection().document(appointmentId)
        val now = System.currentTimeMillis()

        firestore.runTransaction { transaction ->
            val appointmentSnapshot = transaction.get(appointmentRef)
            if (!appointmentSnapshot.exists()) {
                throw AppointmentValidationException("Жазба табылмады")
            }
            val current = appointmentSnapshot.toAppointment()
            if (current.status == AppointmentStatus.CANCELLED) {
                throw AppointmentValidationException("Жазба бұрыннан бас тартылған")
            }
            if (current.status == AppointmentStatus.COMPLETED) {
                throw AppointmentValidationException("Аяқталған қабылдауды бас тартуға болмайды")
            }

            val currentSlotRef = slotRef(current.doctorId, current.date, current.time)
            val currentSlotSnapshot = transaction.get(currentSlotRef)
            if (currentSlotSnapshot.exists()) {
                val capacity = (currentSlotSnapshot.getLong(FirestoreFields.CAPACITY)?.toInt() ?: 1)
                    .coerceAtLeast(1)
                val releasedPayload = upsertSlotPayload(
                    slotSnapshot = currentSlotSnapshot,
                    doctorId = current.doctorId,
                    clinicId = current.clinicId,
                    date = current.date,
                    time = current.time,
                    capacity = capacity,
                    appointmentId = appointmentId,
                    increment = false,
                    now = now
                )
                transaction.update(currentSlotRef, releasedPayload)
            }

            transaction.update(
                appointmentRef,
                mapOf(
                    FirestoreFields.STATUS to AppointmentStatus.CANCELLED.name,
                    FirestoreFields.CANCELLED_BY to changedBy,
                    FirestoreFields.CANCEL_REASON to reason,
                    FirestoreFields.CANCELLED_AT to now,
                    FirestoreFields.STATUS_CHANGED_BY to changedBy,
                    FirestoreFields.UPDATED_AT to now
                )
            )
            transaction.set(
                appointmentRef.collection(FirestoreCollections.STATUS_HISTORY).document(),
                buildStatusHistoryMap(
                    from = current.status.name,
                    to = AppointmentStatus.CANCELLED.name,
                    by = changedBy,
                    reason = reason,
                    at = now
                )
            )
        }.await()
    }

    private fun appointmentsCollection() = firestore.collection(FirestoreCollections.APPOINTMENTS)

    private fun slotRef(doctorId: String, date: String, time: String): DocumentReference {
        return firestore.collection(FirestoreCollections.APPOINTMENT_SLOTS)
            .document(buildSlotId(doctorId, date, time))
    }

    private fun validateBookingInput(
        doctorId: String,
        clinicId: String,
        date: String,
        time: String
    ) {
        if (doctorId.isBlank()) throw AppointmentValidationException("Дәрігер таңдалмаған")
        if (clinicId.isBlank()) throw AppointmentValidationException("Клиника таңдалмаған")
        if (date.isBlank()) throw AppointmentValidationException("Күн таңдалмаған")
        if (time.isBlank()) throw AppointmentValidationException("Уақыт таңдалмаған")
    }

    private fun validateRescheduleStatus(status: AppointmentStatus) {
        if (status == AppointmentStatus.CANCELLED ||
            status == AppointmentStatus.COMPLETED ||
            status == AppointmentStatus.NO_SHOW
        ) {
            throw AppointmentValidationException("Бұл жазбаны қайта жоспарлау мүмкін емес")
        }
    }

    private fun upsertSlotPayload(
        slotSnapshot: DocumentSnapshot,
        doctorId: String,
        clinicId: String,
        date: String,
        time: String,
        capacity: Int,
        appointmentId: String,
        increment: Boolean,
        now: Long
    ): Map<String, Any> {
        @Suppress("UNCHECKED_CAST")
        val currentIds = (slotSnapshot.get(FirestoreFields.APPOINTMENT_IDS) as? List<String>).orEmpty()
        val safeCapacity = capacity.coerceAtLeast(1)

        val nextIds = if (increment) {
            if (currentIds.contains(appointmentId)) {
                currentIds
            } else {
                currentIds + appointmentId
            }
        } else {
            currentIds.filterNot { it == appointmentId }
        }

        val nextBookedCount = if (increment) {
            (slotSnapshot.getLong(FirestoreFields.BOOKED_COUNT)?.toInt() ?: 0) + 1
        } else {
            val currentCount = slotSnapshot.getLong(FirestoreFields.BOOKED_COUNT)?.toInt()
                ?: currentIds.size
            (currentCount - 1).coerceAtLeast(0)
        }

        return if (slotSnapshot.exists()) {
            mapOf(
                FirestoreFields.BOOKED_COUNT to nextBookedCount,
                FirestoreFields.APPOINTMENT_IDS to nextIds,
                FirestoreFields.UPDATED_AT to now
            )
        } else {
            mapOf(
                FirestoreFields.DOCTOR_ID to doctorId,
                FirestoreFields.CLINIC_ID to clinicId,
                FirestoreFields.DATE to date,
                FirestoreFields.TIME to time,
                FirestoreFields.CAPACITY to safeCapacity,
                FirestoreFields.BOOKED_COUNT to nextBookedCount,
                FirestoreFields.APPOINTMENT_IDS to nextIds,
                FirestoreFields.CREATED_AT to now,
                FirestoreFields.UPDATED_AT to now
            )
        }
    }

    private fun generateTimesBySchedule(
        startTime: String,
        endTime: String,
        slotDurationMin: Int
    ): List<String> {
        val start = parseTime(startTime) ?: return emptyList()
        val end = parseTime(endTime) ?: return emptyList()
        if (slotDurationMin <= 0 || start >= end) return emptyList()

        val slots = mutableListOf<String>()
        var cursor = start
        while (cursor.plusMinutes(slotDurationMin.toLong()) <= end) {
            slots += cursor.format(TIME_FORMATTER)
            cursor = cursor.plusMinutes(slotDurationMin.toLong())
        }
        return slots
    }

    private fun intersectsBreak(
        slotStart: String,
        slotDurationMin: Int,
        breaks: List<ScheduleBreak>
    ): Boolean {
        val start = parseTime(slotStart) ?: return false
        val end = start.plusMinutes(slotDurationMin.toLong())

        return breaks.any { item ->
            val breakStart = parseTime(item.startTime) ?: return@any false
            val breakEnd = parseTime(item.endTime) ?: return@any false
            start < breakEnd && end > breakStart
        }
    }

    private fun dayOfWeekKey(date: String): String? {
        val parsed = parseDate(date) ?: return null
        return when (parsed.dayOfWeek) {
            DayOfWeek.MONDAY -> "monday"
            DayOfWeek.TUESDAY -> "tuesday"
            DayOfWeek.WEDNESDAY -> "wednesday"
            DayOfWeek.THURSDAY -> "thursday"
            DayOfWeek.FRIDAY -> "friday"
            DayOfWeek.SATURDAY -> "saturday"
            DayOfWeek.SUNDAY -> "sunday"
        }
    }

    private fun buildSlotId(doctorId: String, date: String, time: String): String {
        val safeDoctorId = doctorId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        return "${safeDoctorId}_${normalizeSlotDate(date)}_${normalizeSlotTime(time)}"
    }

    private fun normalizeSlotDate(date: String): String {
        val parsed = parseDate(date)
        if (parsed != null) {
            return parsed.format(DateTimeFormatter.BASIC_ISO_DATE)
        }
        return date.filter { it.isDigit() }.padStart(8, '0').takeLast(8)
    }

    private fun normalizeSlotTime(time: String): String {
        val parsed = parseTime(time)
        if (parsed != null) {
            return parsed.format(DateTimeFormatter.ofPattern("HHmm"))
        }
        return time.filter { it.isDigit() }.padStart(4, '0').takeLast(4)
    }

    private fun parseDate(date: String): LocalDate? {
        val value = date.trim()
        val formats = listOf(
            DateTimeFormatter.ISO_DATE,
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
        )
        return formats.firstNotNullOfOrNull { formatter ->
            runCatching { LocalDate.parse(value, formatter) }.getOrNull()
        }
    }

    private fun parseTime(time: String): LocalTime? {
        val value = time.trim()
        val formats = listOf(
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("H:mm")
        )
        return formats.firstNotNullOfOrNull { formatter ->
            runCatching { LocalTime.parse(value, formatter) }.getOrNull()
        }
    }

    private fun DocumentSnapshot.toDoctorSchedule(
        doctorId: String,
        fallbackClinicId: String
    ): DoctorSchedule {
        @Suppress("UNCHECKED_CAST")
        val rawWorkingDays = get(FirestoreFields.WORKING_DAYS) as? Map<String, Any>
        @Suppress("UNCHECKED_CAST")
        val rawBreaks = get(FirestoreFields.BREAKS) as? List<Map<String, Any>>

        val workingDays = rawWorkingDays
            ?.mapNotNull { entry ->
                val dayMap = entry.value as? Map<*, *> ?: return@mapNotNull null
                val enabled = dayMap[FirestoreFields.ENABLED] as? Boolean ?: false
                val start = dayMap[FirestoreFields.START_TIME] as? String ?: "09:00"
                val end = dayMap[FirestoreFields.END_TIME] as? String ?: "18:00"
                entry.key.lowercase(Locale.ROOT) to WorkingDaySchedule(
                    enabled = enabled,
                    startTime = start,
                    endTime = end
                )
            }
            ?.toMap()
            ?: emptyMap()

        val breaks = rawBreaks
            ?.map { raw ->
                ScheduleBreak(
                    dayOfWeek = raw[FirestoreFields.DAY_OF_WEEK]?.toString().orEmpty(),
                    startTime = raw[FirestoreFields.START_TIME]?.toString().orEmpty(),
                    endTime = raw[FirestoreFields.END_TIME]?.toString().orEmpty()
                )
            }
            ?: emptyList()

        return DoctorSchedule(
            doctorId = getString(FirestoreFields.DOCTOR_ID).orEmpty().ifBlank { doctorId },
            clinicId = getString(FirestoreFields.CLINIC_ID).orEmpty().ifBlank { fallbackClinicId },
            slotDurationMin = (getLong(FirestoreFields.SLOT_DURATION_MIN)?.toInt() ?: 60).coerceAtLeast(5),
            workingDays = workingDays,
            breaks = breaks,
            capacityPerSlot = (getLong(FirestoreFields.CAPACITY_PER_SLOT)?.toInt() ?: 1).coerceAtLeast(1),
            updatedAt = readLongField(FirestoreFields.UPDATED_AT) ?: 0L
        )
    }

    private fun Appointment.toFirestoreMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            FirestoreFields.ID to id,
            FirestoreFields.PATIENT_ID to patientId,
            FirestoreFields.PATIENT_NAME to patientName,
            FirestoreFields.PATIENT_PHONE to patientPhone,
            FirestoreFields.CLINIC_ID to clinicId,
            FirestoreFields.CLINIC_NAME to clinicName,
            FirestoreFields.DOCTOR_ID to doctorId,
            FirestoreFields.DOCTOR_NAME to doctorName,
            FirestoreFields.SERVICE to service,
            FirestoreFields.DATE to date,
            FirestoreFields.TIME to time,
            FirestoreFields.DURATION to duration,
            FirestoreFields.STATUS to status.name,
            FirestoreFields.CREATED_AT to createdAt,
            FirestoreFields.UPDATED_AT to updatedAt
        )

        cancelledBy?.let { map[FirestoreFields.CANCELLED_BY] = it }
        cancelReason?.let { map[FirestoreFields.CANCEL_REASON] = it }
        cancelledAt?.let { map[FirestoreFields.CANCELLED_AT] = it }
        completedAt?.let { map[FirestoreFields.COMPLETED_AT] = it }
        rescheduledFromId?.let { map[FirestoreFields.RESCHEDULED_FROM_ID] = it }
        previousDate?.let { map[FirestoreFields.PREVIOUS_DATE] = it }
        previousTime?.let { map[FirestoreFields.PREVIOUS_TIME] = it }
        statusChangedBy?.let { map[FirestoreFields.STATUS_CHANGED_BY] = it }

        return map
    }

    private fun DocumentSnapshot.toAppointment(): Appointment {
        return Appointment(
            id = readStringField(FirestoreFields.ID).takeIf { it.isNotBlank() } ?: id,
            patientId = readStringField(FirestoreFields.PATIENT_ID),
            patientName = readStringField(FirestoreFields.PATIENT_NAME),
            patientPhone = readStringField(FirestoreFields.PATIENT_PHONE),
            clinicId = readStringField(FirestoreFields.CLINIC_ID),
            clinicName = readStringField(FirestoreFields.CLINIC_NAME),
            doctorId = readStringField(FirestoreFields.DOCTOR_ID),
            doctorName = readStringField(FirestoreFields.DOCTOR_NAME),
            service = readStringField(FirestoreFields.SERVICE, "serviceName"),
            date = readDateField(FirestoreFields.DATE),
            time = readTimeField(FirestoreFields.TIME),
            duration = readStringField(FirestoreFields.DURATION).ifBlank { BookingDefaults.DEFAULT_DURATION },
            status = AppointmentStatus.fromStorage(readStringField(FirestoreFields.STATUS)),
            cancelledBy = readNullableStringField(FirestoreFields.CANCELLED_BY),
            cancelReason = readNullableStringField(FirestoreFields.CANCEL_REASON),
            cancelledAt = readLongField(FirestoreFields.CANCELLED_AT),
            completedAt = readLongField(FirestoreFields.COMPLETED_AT),
            rescheduledFromId = readNullableStringField(FirestoreFields.RESCHEDULED_FROM_ID),
            previousDate = readNullableDateField(FirestoreFields.PREVIOUS_DATE),
            previousTime = readNullableTimeField(FirestoreFields.PREVIOUS_TIME),
            statusChangedBy = readNullableStringField(FirestoreFields.STATUS_CHANGED_BY),
            createdAt = readLongField(FirestoreFields.CREATED_AT) ?: System.currentTimeMillis(),
            updatedAt = readLongField(FirestoreFields.UPDATED_AT) ?: System.currentTimeMillis()
        )
    }

    private fun mapSnapshotToAppointments(
        snapshot: QuerySnapshot,
        queryName: String,
        queryField: String,
        queryValue: String
    ): List<Appointment> {
        Log.d(
            APPOINTMENTS_DEBUG_TAG,
            "query=$queryName field=$queryField value=$queryValue snapshotSize=${snapshot.size()}"
        )
        val mapped = mutableListOf<Appointment>()
        snapshot.documents.forEach { document ->
            Log.d(
                APPOINTMENTS_DEBUG_TAG,
                "docId=${document.id} raw=${document.data}"
            )
            val appointment = runCatching { document.toAppointment() }
                .onFailure { throwable ->
                    Log.e(
                        APPOINTMENTS_DEBUG_TAG,
                        "mapping_failed docId=${document.id} message=${throwable.message}",
                        throwable
                    )
                }
                .getOrNull()
            if (appointment != null) {
                mapped += appointment
                Log.d(
                    APPOINTMENTS_DEBUG_TAG,
                    "mapping_success docId=${document.id} status=${appointment.status} date=${appointment.date} time=${appointment.time}"
                )
            }
        }
        Log.d(APPOINTMENTS_DEBUG_TAG, "query=$queryName finalMappedSize=${mapped.size}")
        return mapped
    }

    private fun DocumentSnapshot.readStringField(vararg fields: String): String {
        return readNullableStringField(*fields).orEmpty()
    }

    private fun DocumentSnapshot.readNullableStringField(vararg fields: String): String? {
        fields.forEach { field ->
            val value = get(field) ?: return@forEach
            val stringValue = value.toString().trim()
            if (stringValue.isNotEmpty()) {
                return stringValue
            }
        }
        return null
    }

    private fun DocumentSnapshot.readDateField(vararg fields: String): String {
        return readNullableDateField(*fields).orEmpty()
    }

    private fun DocumentSnapshot.readNullableDateField(vararg fields: String): String? {
        fields.forEach { field ->
            val value = get(field) ?: return@forEach
            when (value) {
                is Timestamp -> return value.toDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .format(DISPLAY_DATE_FORMATTER)
                is Long -> return formatDateFromMillis(value)
                is Int -> return formatDateFromMillis(value.toLong())
                is Double -> return formatDateFromMillis(value.toLong())
                else -> {
                    val dateAsText = value.toString().trim()
                    if (dateAsText.isNotEmpty()) {
                        return dateAsText
                    }
                }
            }
        }
        return null
    }

    private fun DocumentSnapshot.readTimeField(vararg fields: String): String {
        return readNullableTimeField(*fields).orEmpty()
    }

    private fun DocumentSnapshot.readNullableTimeField(vararg fields: String): String? {
        fields.forEach { field ->
            val value = get(field) ?: return@forEach
            when (value) {
                is Timestamp -> return value.toDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalTime()
                    .format(TIME_FORMATTER)
                is Long -> return formatTimeFromMillis(value)
                is Int -> return formatTimeFromMillis(value.toLong())
                is Double -> return formatTimeFromMillis(value.toLong())
                else -> {
                    val timeAsText = value.toString().trim()
                    if (timeAsText.isNotEmpty()) {
                        return timeAsText
                    }
                }
            }
        }
        return null
    }

    private fun formatDateFromMillis(millis: Long): String {
        return Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DISPLAY_DATE_FORMATTER)
    }

    private fun formatTimeFromMillis(millis: Long): String {
        return Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
            .format(TIME_FORMATTER)
    }

    private fun DocumentSnapshot.readLongField(field: String): Long? {
        return when (val value = get(field)) {
            null -> null
            is Long -> value
            is Int -> value.toLong()
            is Double -> value.toLong()
            is Timestamp -> value.toDate().time
            is String -> value.toLongOrNull()
            else -> null
        }
    }

    private fun buildStatusHistoryMap(
        from: String?,
        to: String,
        by: String,
        reason: String?,
        at: Long
    ): Map<String, Any> {
        val payload = mutableMapOf<String, Any>(
            FirestoreFields.STATUS_HISTORY_TO to to,
            FirestoreFields.STATUS_HISTORY_BY to by,
            FirestoreFields.STATUS_HISTORY_AT to at
        )
        from?.let { payload[FirestoreFields.STATUS_HISTORY_FROM] = it }
        reason?.takeIf { it.isNotBlank() }?.let { payload[FirestoreFields.STATUS_HISTORY_REASON] = it }
        return payload
    }

    companion object {
        private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        private val DISPLAY_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        private const val APPOINTMENTS_DEBUG_TAG = "APPOINTMENTS_DEBUG"
    }
}
