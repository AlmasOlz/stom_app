package com.example.stomatology.app.presentation.records

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.stomatology.app.R
import com.example.stomatology.app.domain.model.Appointment
import com.example.stomatology.app.domain.model.AppointmentStatus
import com.example.stomatology.app.domain.model.AvailableSlot
import com.example.stomatology.app.domain.model.toUiText
import com.example.stomatology.app.presentation.booking.convertMillisToDate
import com.example.stomatology.app.presentation.components.AppBackButton
import com.example.stomatology.app.presentation.theme.PrimaryBlue
import java.util.Calendar
import java.util.TimeZone

@Composable
fun MyRecordsScreen(
    onBack: () -> Unit,
    viewModel: MyRecordsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.records_tab_all),
        stringResource(R.string.records_tab_upcoming),
        stringResource(R.string.records_tab_past)
    )
    var rescheduleTarget by remember { mutableStateOf<Appointment?>(null) }
    var cancelTarget by remember { mutableStateOf<Appointment?>(null) }
    var rescheduleDate by rememberSaveable { mutableStateOf("") }
    var rescheduleTime by rememberSaveable { mutableStateOf("") }
    var cancelReason by rememberSaveable { mutableStateOf("") }
    var showRescheduleDatePicker by remember { mutableStateOf(false) }

    val visibleAppointments = viewModel.appointmentsForTab(selectedTabIndex)

    LaunchedEffect(state.actionState) {
        if (state.actionState == MyRecordActionState.Success) {
            rescheduleTarget = null
            cancelTarget = null
            rescheduleDate = ""
            rescheduleTime = ""
            cancelReason = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            shadowElevation = 3.dp
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = 4.dp, end = 12.dp, top = 8.dp, bottom = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppBackButton(onClick = onBack, minimal = true)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.records_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    indicator = { tabPositions ->
                        if (selectedTabIndex < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                height = 3.dp,
                                color = PrimaryBlue
                            )
                        }
                    },
                    divider = {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.SemiBold else FontWeight.Medium
                                )
                            },
                            selectedContentColor = PrimaryBlue,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = PrimaryBlue,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Жазбалар жүктелуде...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            state.error != null && visibleAppointments.isEmpty() -> {
                EmptyRecordsState(
                    text = state.error ?: stringResource(R.string.records_error_generic),
                    actionText = "Қайталап көру",
                    onAction = { viewModel.retryLoadAppointments() }
                )
            }
            visibleAppointments.isEmpty() -> {
                EmptyRecordsState(
                    text = when (selectedTabIndex) {
                        1 -> stringResource(R.string.records_empty_upcoming)
                        2 -> stringResource(R.string.records_empty_past)
                        else -> stringResource(R.string.records_empty_all)
                    }
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(visibleAppointments, key = { item -> item.id }) { appointment ->
                        AppointmentCard(
                            appointment = appointment,
                            onReschedule = {
                                rescheduleTarget = appointment
                                rescheduleDate = appointment.date
                                rescheduleTime = ""
                                viewModel.loadRescheduleSlots(appointment, appointment.date)
                            },
                            onCancel = {
                                cancelTarget = appointment
                                cancelReason = ""
                            }
                        )
                    }
                }
            }
        }
    }

    if (showRescheduleDatePicker) {
        RescheduleDatePickerDialog(
            onDatePicked = { date ->
                showRescheduleDatePicker = false
                rescheduleDate = date
                rescheduleTime = ""
                rescheduleTarget?.let { appointment ->
                    viewModel.loadRescheduleSlots(appointment, date)
                }
            },
            onDismiss = { showRescheduleDatePicker = false }
        )
    }

    if (rescheduleTarget != null) {
        AlertDialog(
            onDismissRequest = { rescheduleTarget = null },
            title = { Text("Уақытты өзгерту") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Жаңа күн мен уақытты таңдаңыз",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = rescheduleDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Күн") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showRescheduleDatePicker = true }
                    )
                    OutlinedButton(onClick = { showRescheduleDatePicker = true }) {
                        Text("Күнді таңдау")
                    }

                    if (state.isSlotsLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        SlotChoiceGrid(
                            selectedTime = rescheduleTime,
                            slots = state.rescheduleSlots,
                            onSelect = { time -> rescheduleTime = time }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val target = rescheduleTarget ?: return@TextButton
                    viewModel.rescheduleAppointment(
                        appointmentId = target.id,
                        newDate = rescheduleDate,
                        newTime = rescheduleTime
                    )
                }) {
                    Text("Сақтау")
                }
            },
            dismissButton = {
                TextButton(onClick = { rescheduleTarget = null }) {
                    Text("Жабу")
                }
            }
        )
    }

    if (cancelTarget != null) {
        AlertDialog(
            onDismissRequest = { cancelTarget = null },
            title = { Text("Жазбадан бас тарту") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Бас тарту себебін жазыңыз")
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { value -> cancelReason = value },
                        label = { Text("Себеп") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val target = cancelTarget ?: return@TextButton
                    viewModel.cancelAppointment(
                        appointmentId = target.id,
                        reason = cancelReason
                    )
                }) {
                    Text("Растау")
                }
            },
            dismissButton = {
                TextButton(onClick = { cancelTarget = null }) {
                    Text("Жабу")
                }
            }
        )
    }
}

@Composable
private fun EmptyRecordsState(
    text: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 80.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = text, color = Color.Gray, style = MaterialTheme.typography.bodyLarge)
            if (!actionText.isNullOrBlank() && onAction != null) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = onAction) {
                    Text(actionText)
                }
            }
        }
    }
}

@Composable
private fun AppointmentCard(
    appointment: Appointment,
    onReschedule: () -> Unit,
    onCancel: () -> Unit
) {
    val isActive = appointment.status == AppointmentStatus.PENDING ||
        appointment.status == AppointmentStatus.CONFIRMED ||
        appointment.status == AppointmentStatus.RESCHEDULED

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = appointment.clinicName.ifBlank { "Клиника" },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Дәрігер: ${appointment.doctorName.ifBlank { "Көрсетілмеген" }}",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
            Text(
                text = "Қызмет: ${appointment.service.ifBlank { "-" }}",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
            Text(
                text = "Күні: ${appointment.date.ifBlank { "-" }}",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
            Text(
                text = "Уақыты: ${appointment.time.ifBlank { "-" }}",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f)
            )

            if (appointment.previousDate != null || appointment.previousTime != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Алдыңғы уақыт: ${appointment.previousDate.orEmpty()} ${appointment.previousTime.orEmpty()}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            StatusBadge(status = appointment.status)

            if (isActive) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onReschedule,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Color.White),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = PrimaryBlue
                        )
                    ) {
                        Text(
                            text = "Уақытты өзгерту",
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Button(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Color.White),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = PrimaryBlue
                        )
                    ) {
                        Text(
                            text = "Бас тарту",
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: AppointmentStatus) {
    Box(
        modifier = Modifier
            .background(
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = status.toUiText(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun SlotChoiceGrid(
    selectedTime: String,
    slots: List<AvailableSlot>,
    onSelect: (String) -> Unit
) {
    if (slots.isEmpty()) {
        Text("Бос слот жоқ", color = Color.Gray)
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        slots.chunked(3).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { slot ->
                    val enabled = slot.isEnabled
                    val selected = selectedTime == slot.time
                    val bg = when {
                        !enabled -> Color(0xFFE2E6EA)
                        selected -> PrimaryBlue
                        else -> Color(0xFFF1F3F5)
                    }
                    val fg = when {
                        !enabled -> Color(0xFF8A8F98)
                        selected -> Color.White
                        else -> Color.DarkGray
                    }

                    Box(
                        modifier = Modifier
                            .width(88.dp)
                            .background(bg, RoundedCornerShape(10.dp))
                            .clickable(enabled = enabled) { onSelect(slot.time) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = slot.time, color = fg, fontWeight = FontWeight.Medium)
                            if (!enabled) {
                                Text("Толық", color = fg, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RescheduleDatePickerDialog(
    onDatePicked: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                return utcTimeMillis >= calendar.timeInMillis
            }

            override fun isSelectableYear(year: Int): Boolean {
                return year >= Calendar.getInstance().get(Calendar.YEAR)
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val date = datePickerState.selectedDateMillis?.let { convertMillisToDate(it) }.orEmpty()
                onDatePicked(date)
            }) {
                Text("РўР°ТЈРґР°Сѓ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Р–Р°Р±Сѓ")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
