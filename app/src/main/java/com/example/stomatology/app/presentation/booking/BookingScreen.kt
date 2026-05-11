package com.example.stomatology.app.presentation.booking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.stomatology.app.core.booking.BookingDefaults
import com.example.stomatology.app.domain.model.AvailableSlot
import com.example.stomatology.app.domain.model.DoctorOption
import com.example.stomatology.app.presentation.theme.PrimaryBlue
import java.util.Calendar
import java.util.TimeZone


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    clinicId: String,
    serviceName: String,
    onBookingComplete: () -> Unit,
    onDismiss: () -> Unit = {},
    viewModel: BookingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(clinicId, serviceName) {
        viewModel.initClinic(clinicId)
        viewModel.onDirectionChange(serviceName)
    }

    LaunchedEffect(state.isBooked) {
        if (state.isBooked) onBookingComplete()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Қабылдауға жазылу", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { if (!viewModel.tryWizardBack()) onDismiss() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Артқа",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        BookingScreenContent(
            padding = padding,
            state = state,
            viewModel = viewModel,
            scrollState = scrollState
        )
    }

    if (state.showSuccessDialog) {
        BookingSuccessDialog(
            doctorName = state.doctorName,
            onReturnHome = {
                viewModel.dismissSuccessDialog()
                onBookingComplete()
            },
            onDismiss = { viewModel.dismissSuccessDialog() }
        )
    }
}

@Composable
private fun BookingWizardProgress(current: BookingWizardStep) {
    val labels = listOf("Қызмет", "Дәрігер", "Күн & Уақыт", "Растау")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        labels.forEachIndexed { index, label ->
            val active = index == current.ordinal
            val done = index < current.ordinal
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                done -> PrimaryBlue
                                active -> Color.White
                                else -> Color(0xFFE8ECF0)
                            }
                        )
                        .then(
                            if (active && !done) {
                                Modifier.border(2.dp, PrimaryBlue, CircleShape)
                            } else {
                                Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (done) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text(
                            text = "${index + 1}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (active) PrimaryBlue else Color.Gray
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = label,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    color = if (active) PrimaryBlue else Color.Gray,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
    LinearProgressIndicator(
        progress = (current.ordinal + 1f) / 4f,
        modifier = Modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(RoundedCornerShape(3.dp)),
        color = PrimaryBlue,
        trackColor = Color(0xFFE0E7EF),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingServiceDropdown(
    state: BookingUiState,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelectTitle: (String) -> Unit,
    enabled: Boolean
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            value = state.direction,
            onValueChange = {},
            readOnly = true,
            placeholder = {
                Text("Қызметті таңдаңыз", color = Color.Gray.copy(alpha = 0.75f))
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            enabled = enabled,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = Color(0xFFD8DEE6),
                focusedLabelColor = PrimaryBlue,
                unfocusedLabelColor = Color.Gray
            )
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            BookingDefaults.BOOKING_SERVICE_TITLES.forEach { title ->
                DropdownMenuItem(
                    text = { Text(title) },
                    onClick = {
                        onSelectTitle(title)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingDoctorDropdown(
    state: BookingUiState,
    onDoctorSelected: (DoctorOption) -> Unit,
    onMenuExpandedChange: (Boolean) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = state.isDoctorMenuExpanded,
        onExpandedChange = onMenuExpandedChange
    ) {
        OutlinedTextField(
            value = state.doctorName,
            onValueChange = {},
            readOnly = true,
            placeholder = {
                Text("Дәрігерді таңдаңыз", color = Color.Gray.copy(alpha = 0.75f))
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.isDoctorMenuExpanded)
            },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            enabled = state.doctors.isNotEmpty(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = Color(0xFFD8DEE6),
                focusedLabelColor = PrimaryBlue,
                unfocusedLabelColor = Color.Gray
            )
        )
        DropdownMenu(
            expanded = state.isDoctorMenuExpanded,
            onDismissRequest = { onMenuExpandedChange(false) }
        ) {
            state.doctors.forEach { doctor ->
                DropdownMenuItem(
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(doctor.name, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(
                                text = doctor.specialty.ifBlank { "—" },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            if (doctor.experienceYears > 0) {
                                Text(
                                    text = "Тәжірибе: ${doctor.experienceYears} жыл",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    },
                    onClick = {
                        onDoctorSelected(doctor)
                        onMenuExpandedChange(false)
                    }
                )
            }
        }
    }
}

@Composable
private fun SelectionSheetField(
    value: String,
    label: String,
    placeholder: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) { onClick() }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text(label, color = PrimaryBlue) },
            placeholder = { Text(placeholder, color = PrimaryBlue.copy(alpha = 0.55f)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledContainerColor = Color(0xFFF2F4F7),
                disabledTextColor = Color.Black,
                disabledBorderColor = Color(0xFFD8DEE6),
                disabledLabelColor = PrimaryBlue,
                disabledPlaceholderColor = PrimaryBlue.copy(alpha = 0.65f)
            )
        )
    }
}

@Composable
private fun ConfirmSummaryCard(
    clinicLabel: String,
    service: String,
    doctorName: String,
    date: String,
    time: String,
    duration: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.35f))
    ) {
        Column {
            ConfirmSummaryRow(label = "Клиника", value = clinicLabel)
            HorizontalDivider(color = Color(0xFFE8ECF0))
            ConfirmSummaryRow(label = "Қызмет", value = service)
            HorizontalDivider(color = Color(0xFFE8ECF0))
            ConfirmSummaryRow(label = "Дәрігер", value = doctorName)
            HorizontalDivider(color = Color(0xFFE8ECF0))
            ConfirmSummaryRow(label = "Күні", value = date)
            HorizontalDivider(color = Color(0xFFE8ECF0))
            ConfirmSummaryRow(label = "Уақыты", value = time)
            HorizontalDivider(color = Color(0xFFE8ECF0))
            ConfirmSummaryRow(label = "Ұзақтығы", value = duration)
        }
    }
}

@Composable
private fun ConfirmSummaryRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF7A8490)
        )
        Text(
            text = value.ifBlank { "—" },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingScreenContent(
    padding: PaddingValues,
    state: BookingUiState,
    viewModel: BookingViewModel,
    scrollState: ScrollState
) {
    val hasClinicId = state.clinicId.isNotBlank()
    val clinicDataUnavailable = hasClinicId && state.clinicName.isBlank() && !state.isLoading
    val userErrorMessage = sanitizeBookingErrorForUi(state.error)
    var serviceMenuExpanded by remember { mutableStateOf(false) }

    val canGoNext = hasClinicId && when (state.currentStep) {
        BookingWizardStep.Service -> state.direction.isNotBlank()
        BookingWizardStep.Doctor -> state.doctorId.isNotBlank()
        BookingWizardStep.DateTime -> {
            val slotOk = state.availableSlots.any { it.time == state.selectedTime && it.isEnabled }
            state.selectedDate.isNotBlank() && state.selectedTime.isNotBlank() && slotOk
        }
        BookingWizardStep.Confirm -> false
    }

    if (state.showDatePicker) {
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
            onDismissRequest = { viewModel.onShowDatePicker(false) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val date = datePickerState.selectedDateMillis
                            ?.let { millis -> convertMillisToDate(millis) }
                            .orEmpty()
                        viewModel.onDateSelected(date)
                        viewModel.onShowDatePicker(false)
                    }
                ) {
                    Text("Таңдау", color = PrimaryBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onShowDatePicker(false) }) {
                    Text("Бас тарту")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (state.showTimePicker) {
        Dialog(onDismissRequest = { viewModel.onShowTimePicker(false) }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Бос уақытты таңдаңыз",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = PrimaryBlue
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    TimeSelectionGrid(
                        selectedTime = state.selectedTime,
                        slots = state.availableSlots,
                        onTimeSelect = { selected ->
                            viewModel.onTimeSelected(selected)
                            viewModel.onShowTimePicker(false)
                        }
                    )
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BookingWizardProgress(current = state.currentStep)

            if (clinicDataUnavailable) {
                Text(
                    text = "Клиника деректері жүктелмеді",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            when (state.currentStep) {
                BookingWizardStep.Service -> {
                    Text(
                        text = "Қандай қызмет керек?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.Black
                    )
                    Text(
                        text = if (hasClinicId) {
                            "Клиника: ${state.clinicName.ifBlank { "—" }}"
                        } else {
                            "Клиника: таңдалмаған"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    BookingServiceDropdown(
                        state = state,
                        expanded = serviceMenuExpanded,
                        onExpandedChange = { serviceMenuExpanded = it },
                        onSelectTitle = { title -> viewModel.selectServiceTitle(title) },
                        enabled = hasClinicId
                    )
                    if (state.direction.isNotBlank()) {
                        Text(
                            text = "Таңдалған қызметке сай дәрігерлер: ${state.doctors.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                BookingWizardStep.Doctor -> {
                    Text(
                        text = "Дәрігерді таңдаңыз",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.Black
                    )
                    BookingDoctorDropdown(
                        state = state,
                        onDoctorSelected = { doctor -> viewModel.onDoctorSelected(doctor) },
                        onMenuExpandedChange = { expanded ->
                            viewModel.onDoctorMenuExpandedChange(expanded)
                        }
                    )
                    if (state.doctors.isEmpty()) {
                        Text(
                            text = userErrorMessage
                                ?: "Бұл клиникада лайықты маман табылмады",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }

                BookingWizardStep.DateTime -> {
                    Text(
                        text = "Күн мен уақытты таңдаңыз",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.Black
                    )
                    SelectionSheetField(
                        value = state.selectedDate,
                        label = "Қабылдау күні",
                        placeholder = "Күнді таңдаңыз",
                        enabled = hasClinicId,
                        onClick = { viewModel.onShowDatePicker(true) }
                    )
                    SelectionSheetField(
                        value = state.selectedTime,
                        label = "Қабылдау уақыты",
                        placeholder = when {
                            state.selectedDate.isBlank() -> "Алдымен күнді таңдаңыз"
                            state.availableSlots.isEmpty() -> "Бұл күнге бос уақыт жоқ"
                            else -> "Уақытты таңдаңыз"
                        },
                        enabled = hasClinicId &&
                            state.selectedDate.isNotBlank() &&
                            state.doctorId.isNotBlank() &&
                            state.availableSlots.isNotEmpty(),
                        onClick = { viewModel.onShowTimePicker(true) }
                    )
                    OutlinedTextField(
                        value = state.duration,
                        onValueChange = viewModel::onDurationChange,
                        label = { Text("Ұзақтығы") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFD8DEE6),
                            focusedBorderColor = PrimaryBlue
                        )
                    )
                    when {
                        state.selectedDate.isBlank() -> {
                            Text(
                                text = "Алдымен қабылдау күнін таңдаңыз",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }

                        state.availableSlots.isEmpty() -> {
                            Text(
                                text = "Бұл күнге бос уақыт жоқ",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }

                BookingWizardStep.Confirm -> {
                    Text(
                        text = "Жазылуды растаңыз",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.Black
                    )
                    ConfirmSummaryCard(
                        clinicLabel = state.clinicName.ifBlank { "—" },
                        service = state.direction.ifBlank { "—" },
                        doctorName = state.doctorName.ifBlank { "—" },
                        date = state.selectedDate.ifBlank { "—" },
                        time = state.selectedTime.ifBlank { "—" },
                        duration = state.duration.ifBlank { "—" }
                    )
                }
            }

            userErrorMessage?.let { message ->
                if (state.currentStep != BookingWizardStep.Doctor || state.doctors.isNotEmpty()) {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Surface(
            tonalElevation = 2.dp,
            shadowElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                when {
                    state.currentStep == BookingWizardStep.Confirm && state.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(44.dp)
                                .align(Alignment.CenterHorizontally),
                            color = PrimaryBlue,
                            strokeWidth = 3.dp
                        )
                    }

                    state.currentStep == BookingWizardStep.Confirm -> {
                        Button(
                            onClick = { viewModel.confirmBooking() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            enabled = hasClinicId &&
                                state.selectedDate.isNotEmpty() &&
                                state.selectedTime.isNotEmpty() &&
                                state.doctorId.isNotEmpty(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue,
                                disabledContainerColor = Color(0xFFE0E4E8),
                                disabledContentColor = Color.White.copy(alpha = 0.75f)
                            )
                        ) {
                            Text("Жазылуды растау", fontWeight = FontWeight.Bold)
                        }
                    }

                    else -> {
                        Button(
                            onClick = { viewModel.goToNextStep() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            enabled = canGoNext,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue,
                                disabledContainerColor = Color(0xFFE0E4E8),
                                disabledContentColor = Color.White.copy(alpha = 0.75f)
                            )
                        ) {
                            Text("Келесі", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimeSelectionGrid(
    selectedTime: String,
    slots: List<AvailableSlot>,
    onTimeSelect: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.heightIn(max = 320.dp)
    ) {
        items(slots, key = { slot -> slot.time }) { slot ->
            val isSelected = slot.time == selectedTime
            val background = when {
                !slot.isEnabled -> Color(0xFFE2E6EA)
                isSelected -> PrimaryBlue
                else -> Color(0xFFF1F3F5)
            }
            val textColor = when {
                !slot.isEnabled -> Color(0xFF8A8F98)
                isSelected -> Color.White
                else -> Color.DarkGray
            }

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = slot.isEnabled) { onTimeSelect(slot.time) },
                color = background,
                shape = RoundedCornerShape(8.dp),
                border = if (isSelected) null else BorderStroke(1.dp, Color.LightGray)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = slot.time,
                        color = textColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                    if (!slot.isEnabled) {
                        Text(
                            text = "Толық",
                            color = textColor,
                            style = MaterialTheme.typography.labelSmall
                        )
                    } else if (slot.capacity > 1) {
                        Text(
                            text = "${slot.bookedCount}/${slot.capacity}",
                            color = textColor,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

private fun sanitizeBookingErrorForUi(rawMessage: String?): String? {
    val message = rawMessage?.trim().orEmpty()
    if (message.isBlank()) return null

    val lowered = message.lowercase()
    val technicalMarkers = listOf(
        "flow exception transparency",
        "kotlinx.coroutines.flow",
        "java.lang",
        "at com.",
        "at androidx.",
        "exception",
        "clinic("
    )
    val looksTechnical = technicalMarkers.any { marker -> lowered.contains(marker) } ||
            message.contains('\n')

    return if (looksTechnical) "Сұранысты орындау мүмкін болмады. Қайта көріңіз" else message
}