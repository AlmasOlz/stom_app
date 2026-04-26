package com.example.stomatology.app.presentation.booking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.stomatology.app.presentation.theme.PrimaryBlue
import java.text.SimpleDateFormat
import java.util.*

// Датаны форматтау функциясы
fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}

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

    LaunchedEffect(clinicId) {
        viewModel.initClinic(clinicId)
        viewModel.onDirectionChange(serviceName)
    }

    LaunchedEffect(state.isBooked) {
        if (state.isBooked) {
            onBookingComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Запись на приём", fontWeight = FontWeight.Bold) },
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
    // --- 1. ДИАЛОГ ВЫБОРА ДАТЫ ---
    if (state.showDatePicker) {
        // Создаем состояние календаря с ограничением дат
        val datePickerState = rememberDatePickerState(
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    // Получаем текущее время в миллисекундах
                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    // Сбрасываем время до начала текущего дня (00:00:00),
                    // чтобы сегодня тоже можно было выбрать
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)

                    // Возвращаем true только если дата в календаре >= текущей даты
                    return utcTimeMillis >= calendar.timeInMillis
                }

                override fun isSelectableYear(year: Int): Boolean {
                    // Также можно ограничить выбор года (не меньше текущего)
                    return year >= Calendar.getInstance().get(Calendar.YEAR)
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { viewModel.onShowDatePicker(false) },
            confirmButton = {
                TextButton(onClick = {
                    val date = datePickerState.selectedDateMillis?.let { convertMillisToDate(it) } ?: ""
                    viewModel.onDateSelected(date)
                    viewModel.onShowDatePicker(false)
                }) { Text("ОК", color = PrimaryBlue) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onShowDatePicker(false) }) { Text("Отмена") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // 2. УАҚЫТ ТАҢДАУ ТЕРЕЗЕСІ
    if (state.showTimePicker) {
        Dialog(onDismissRequest = { viewModel.onShowTimePicker(false) }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Выберите свободное время", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimaryBlue)
                    Spacer(modifier = Modifier.height(16.dp))

                    TimeSelectionGrid(
                        selectedTime = state.selectedTime,
                        timeSlots = state.availableTimeSlots.ifEmpty { listOf("09:00", "10:00", "11:00", "14:00", "15:00", "16:00") },
                        onTimeSelect = {
                            viewModel.onTimeSelected(it)
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
            text = "Клиника: ${state.clinicName}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlue
        )

        // Дата таңдау өрісі
        ClickableTextField(
            value = state.selectedDate,
            label = "Дата визита",
            placeholder = "Нажмите для выбора даты",
            onClick = { viewModel.onShowDatePicker(true) }
        )

        // Уақыт таңдау өрісі
        ClickableTextField(
            value = state.selectedTime,
            label = "Время приема",
            placeholder = if (state.selectedDate.isEmpty()) "Сначала выберите дату" else "Выберите время",
            enabled = state.selectedDate.isNotEmpty(),
            onClick = { viewModel.onShowTimePicker(true) }
        )

        // 3. ДӘРІГЕРДІ ТАҢДАУ (DROPDOWN)
        Column {
            Text("Специалист", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
            Spacer(modifier = Modifier.height(4.dp))
            ExposedDropdownMenuBox(
                expanded = state.isDoctorMenuExpanded,
                onExpandedChange = { viewModel.onDoctorMenuExpandedChange(it) },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = state.doctorName,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Выберите врача") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.isDoctorMenuExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                ExposedDropdownMenu(
                    expanded = state.isDoctorMenuExpanded,
                    onDismissRequest = { viewModel.onDoctorMenuExpandedChange(false) },
                    modifier = Modifier.background(Color.White)
                ) {
                    state.doctorList.forEach { doctor ->
                        DropdownMenuItem(
                            text = { Text(text = doctor) },
                            onClick = {
                                viewModel.onDoctorNameChange(doctor)
                                viewModel.onDoctorMenuExpandedChange(false)
                            }
                        )
                    }
                }
            }
        }

        // 4. УСЛУГА (Өзгермейтін өріс)
        ReadOnlyTextField(label = "Услуга", value = state.direction)

        // 5. ПРОДОЛЖИТЕЛЬНОСТЬ (Өзгермейтін өріс)
        ReadOnlyTextField(label = "Продолжительность", value = state.duration)

        state.error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = PrimaryBlue)
        } else {
            Button(
                onClick = { viewModel.confirmBooking() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = state.selectedDate.isNotEmpty() && state.selectedTime.isNotEmpty() && state.doctorName.isNotEmpty(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Подтвердить запись", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Көмекші компонент: Басылатын өрістер (Дата/Время)
@Composable
fun ClickableTextField(
    value: String,
    label: String,
    placeholder: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = enabled,
            readOnly = true,
            colors = OutlinedTextFieldDefaults.colors(
                disabledContainerColor = Color.White,
                disabledTextColor = Color.Black,
                disabledBorderColor = Color.LightGray,
                disabledLabelColor = PrimaryBlue
            )
        )
        // Кликтерді ұстау үшін үстіне мөлдір қабат
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(12.dp))
                .clickable(enabled = enabled) { onClick() }
        )
    }
}

// Көмекші компонент: Тек оқуға арналған өрістер (Услуга/Продолжительность)
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
    timeSlots: List<String>,
    onTimeSelect: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.heightIn(max = 300.dp)
    ) {
        items(timeSlots) { time ->
            val isSelected = time == selectedTime
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onTimeSelect(time) },
                color = if (isSelected) PrimaryBlue else Color(0xFFF1F3F5),
                shape = RoundedCornerShape(8.dp),
                border = if (isSelected) null else BorderStroke(1.dp, Color.LightGray)
            ) {
                Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = time,
                        color = if (isSelected) Color.White else Color.DarkGray,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}