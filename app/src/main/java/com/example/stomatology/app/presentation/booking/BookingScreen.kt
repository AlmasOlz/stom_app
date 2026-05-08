package com.example.stomatology.app.presentation.booking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.stomatology.app.domain.model.AvailableSlot
import com.example.stomatology.app.domain.model.DoctorOption
import com.example.stomatology.app.presentation.theme.PrimaryBlue
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    clinicId: String,
    serviceName: String,
    onBookingComplete: () -> Unit,
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
            scrollModifier = Modifier.verticalScroll(scrollState)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingScreenContent(
    padding: PaddingValues,
    state: BookingUiState,
    viewModel: BookingViewModel,
    scrollModifier: Modifier = Modifier
) {
    val selectedDoctor = state.doctors.firstOrNull { doctor -> doctor.uid == state.doctorId }
    val hasClinicId = state.clinicId.isNotBlank()
    val clinicDataUnavailable = hasClinicId && state.clinicName.isBlank() && !state.isLoading
    val userErrorMessage = sanitizeBookingErrorForUi(state.error)

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
                    Text("OK", color = PrimaryBlue)
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
            .then(scrollModifier)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (hasClinicId) {
                "Клиника: ${state.clinicName.ifBlank { "—" }}"
            } else {
                "Клиника: таңдалмаған"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlue
        )

        if (clinicDataUnavailable) {
            Text(
                text = "Клиника деректері жүктелмеді",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        ClickableTextField(
            value = state.selectedDate,
            enabled = hasClinicId,
            label = "Қабылдау күні",
            placeholder = "Күнді таңдаңыз",
            onClick = { viewModel.onShowDatePicker(true) }
        )

        ClickableTextField(
            value = state.selectedTime,
            label = "Қабылдау уақыты",
            placeholder = if (state.selectedDate.isEmpty()) {
                "Алдымен күнді таңдаңыз"
            } else {
                "Уақытты таңдаңыз"
            },
            enabled = hasClinicId && state.selectedDate.isNotEmpty() && state.availableSlots.isNotEmpty(),
            onClick = { viewModel.onShowTimePicker(true) }
        )

        if (state.selectedDate.isNotBlank() && state.availableSlots.isEmpty()) {
            Text(
                text = "Бұл күні дәрігер қабылдамайды немесе мереке күні",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Column {
            Text(
                text = "Маман",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
            Spacer(modifier = Modifier.height(4.dp))

            ExposedDropdownMenuBox(
                expanded = state.isDoctorMenuExpanded,
                onExpandedChange = { expanded -> viewModel.onDoctorMenuExpandedChange(expanded) },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = state.doctorName,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Дәрігерді таңдаңыз") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.isDoctorMenuExpanded)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )

                DropdownMenu(
                    expanded = state.isDoctorMenuExpanded,
                    onDismissRequest = { viewModel.onDoctorMenuExpandedChange(false) },
                    modifier = Modifier.background(Color.White)
                ) {
                    state.doctors.forEach { doctor ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(doctor.name)
                                    if (doctor.specialty.isNotBlank()) {
                                        Text(
                                            text = doctor.specialty,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.Gray
                                        )
                                    }
                                    if (doctor.experienceYears > 0) {
                                        Text(
                                            text = "Тәжірибе: ${doctor.experienceYears} жыл",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            },
                            onClick = { viewModel.onDoctorSelected(doctor) }
                        )
                    }
                }
            }

            if (state.doctors.isEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Бұл клиникада дәрігер жоқ",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        ReadOnlyTextField(label = "Қызмет", value = state.direction)
        ReadOnlyTextField(label = "Ұзақтығы", value = state.duration)

        selectedDoctor?.let { doctor ->
            DoctorPreviewCard(doctor = doctor)
        }

        userErrorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = PrimaryBlue
            )
        } else {
            Button(
                onClick = { viewModel.confirmBooking() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = hasClinicId &&
                        state.selectedDate.isNotEmpty() &&
                        state.selectedTime.isNotEmpty() &&
                        state.doctorName.isNotEmpty(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Жазылуды растау", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ClickableTextField(
    value: String,
    label: String,
    placeholder: String,
    enabled: Boolean = true,
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
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = false,
            readOnly = true,
            colors = OutlinedTextFieldDefaults.colors(
                disabledContainerColor = Color.White,
                disabledTextColor = Color.Black,
                disabledBorderColor = Color.LightGray,
                disabledLabelColor = PrimaryBlue
            )
        )
    }
}

@Composable
fun ReadOnlyTextField(label: String, value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White,
            unfocusedTextColor = Color.Black,
            unfocusedLabelColor = Color.Gray,
            unfocusedBorderColor = Color.LightGray
        )
    )
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
                            text = "Толы",
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

@Composable
private fun DoctorPreviewCard(doctor: DoctorOption) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F4FA)),
                contentAlignment = Alignment.Center
            ) {
                if (doctor.photoUrl.isNotBlank()) {
                    AsyncImage(
                        model = doctor.photoUrl,
                        contentDescription = "Doctor avatar",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = doctor.name.take(1).uppercase(),
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(doctor.name, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(
                    text = doctor.specialty.ifBlank { "Стоматолог" },
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
                if (doctor.experienceYears > 0) {
                    Text(
                        text = "Тәжірибе: ${doctor.experienceYears} жыл",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (doctor.aboutDoctor.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = doctor.aboutDoctor,
                        color = Color.DarkGray,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 3
                    )
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