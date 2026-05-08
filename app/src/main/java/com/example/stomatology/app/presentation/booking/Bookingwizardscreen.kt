package com.example.stomatology.app.presentation.booking

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableIntStateOf
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
import coil.compose.AsyncImage
import com.example.stomatology.app.domain.model.AvailableSlot
import com.example.stomatology.app.domain.model.DoctorOption
import com.example.stomatology.app.presentation.theme.PrimaryBlue
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// ─────────────────────────────────────────────────────────────────────────────
//  Утилита
// ─────────────────────────────────────────────────────────────────────────────


private enum class BookingStep(val index: Int, val title: String) {
    SERVICE(0,  "Қызмет"),
    DOCTOR(1,   "Дәрігер"),
    DATETIME(2, "Күн & Уақыт"),
    CONFIRM(3,  "Растау")
}

// ─────────────────────────────────────────────────────────────────────────────
//  Root screen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingWizardScreen(
    clinicId: String,
    serviceName: String,
    onBookingComplete: () -> Unit,
    viewModel: BookingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var currentStep by remember { mutableIntStateOf(BookingStep.SERVICE.index) }

    LaunchedEffect(clinicId, serviceName) {
        viewModel.initClinic(clinicId)
        viewModel.onDirectionChange(serviceName)
    }
    LaunchedEffect(state.isBooked) {
        if (state.isBooked) onBookingComplete()
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        BookingStep.entries[currentStep].title,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (currentStep > 0) {
                        IconButton(onClick = { currentStep-- }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Артқа",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            WizardStepIndicator(currentStep = currentStep)

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { w -> w } + fadeIn() togetherWith
                                slideOutHorizontally { w -> -w } + fadeOut()
                    } else {
                        slideInHorizontally { w -> -w } + fadeIn() togetherWith
                                slideOutHorizontally { w -> w } + fadeOut()
                    }
                },
                label = "step_anim"
            ) { step ->
                when (BookingStep.entries[step]) {
                    BookingStep.SERVICE  -> StepServiceScreen(state, viewModel) {
                        currentStep = BookingStep.DOCTOR.index
                    }
                    BookingStep.DOCTOR   -> StepDoctorScreen(state, viewModel) {
                        currentStep = BookingStep.DATETIME.index
                    }
                    BookingStep.DATETIME -> StepDateTimeScreen(state, viewModel) {
                        currentStep = BookingStep.CONFIRM.index
                    }
                    BookingStep.CONFIRM  -> StepConfirmScreen(
                        state = state,
                        isLoading = state.isLoading,
                        onConfirm = { viewModel.confirmBooking() }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Step indicator
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WizardStepIndicator(currentStep: Int) {
    val steps = BookingStep.entries
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            val isDone    = index < currentStep
            val isCurrent = index == currentStep

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDone || isCurrent) PrimaryBlue else Color(0xFFE2E6EA)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text(
                        "${index + 1}",
                        color = if (isCurrent) Color.White else Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isCurrent) {
                Text(
                    step.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
            }

            if (index < steps.lastIndex) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(if (isDone) PrimaryBlue else Color(0xFFE2E6EA))
                )
            }
        }
    }
    HorizontalDivider(color = Color(0xFFE2E6EA))
}

// ─────────────────────────────────────────────────────────────────────────────
//  Step 1 – Қызмет
// ─────────────────────────────────────────────────────────────────────────────

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun StepServiceScreen(
    state: BookingUiState,
    viewModel: BookingViewModel,
    onNext: () -> Unit
) {
    val serviceOptions = remember(state.clinicServices, state.direction) {
        (state.clinicServices + state.direction)
            .map { service -> service.trim() }
            .filter { service -> service.isNotBlank() }
            .distinct()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Қандай қызмет керек?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Клиника: ${state.clinicName.ifBlank { "—" }}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = state.isServiceMenuExpanded,
            onExpandedChange = { viewModel.onServiceMenuExpandedChange(it) },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = state.direction,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Қызметті таңдаңыз") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.isServiceMenuExpanded)
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = Color.LightGray
                ),
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
            )
            DropdownMenu(
                expanded = state.isServiceMenuExpanded,
                onDismissRequest = { viewModel.onServiceMenuExpandedChange(false) },
                modifier = Modifier.background(Color.White)
            ) {
                serviceOptions.forEach { service ->
                    DropdownMenuItem(
                        text = { Text(service) },
                        onClick = { viewModel.onDirectionChange(service) }
                    )
                }
            }
        }

        if (serviceOptions.isEmpty()) {
            Text(
                "Клиника бойынша қызметтер табылмады",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        } else {
            Text(
                "Таңдалған қызметке сай дәрігерлер: ${state.doctors.size}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = state.direction.isNotBlank() && state.clinicId.isNotBlank(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) { Text("Келесі", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Step 2 – Дәрігер
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepDoctorScreen(
    state: BookingUiState,
    viewModel: BookingViewModel,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Дәрігерді таңдаңыз", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        ExposedDropdownMenuBox(
            expanded = state.isDoctorMenuExpanded,
            onExpandedChange = { viewModel.onDoctorMenuExpandedChange(it) },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = state.doctorName,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Дәрігерді таңдаңыз") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.isDoctorMenuExpanded) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = Color.LightGray
                ),
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
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
                                Text(doctor.name, fontWeight = FontWeight.Medium)
                                if (doctor.specialty.isNotBlank())
                                    Text(doctor.specialty, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                                if (doctor.experienceYears > 0)
                                    Text("Тәжірибе: ${doctor.experienceYears} жыл", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        },
                        onClick = { viewModel.onDoctorSelected(doctor) }
                    )
                }
            }
        }

        if (state.doctors.isEmpty()) {
            Text("Бұл клиникада дәрігер жоқ", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }

        state.doctors.firstOrNull { it.uid == state.doctorId }?.let { WizardDoctorCard(it) }

        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = state.doctorId.isNotBlank(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) { Text("Келесі", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Step 3 – Күн & Уақыт
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepDateTimeScreen(
    state: BookingUiState,
    viewModel: BookingViewModel,
    onNext: () -> Unit
) {
    if (state.showDatePicker) {
        val dpState = rememberDatePickerState(
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0);       set(Calendar.MILLISECOND, 0)
                    }
                    return utcTimeMillis >= cal.timeInMillis
                }
                override fun isSelectableYear(year: Int) =
                    year >= Calendar.getInstance().get(Calendar.YEAR)
            }
        )
        DatePickerDialog(
            onDismissRequest = { viewModel.onShowDatePicker(false) },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { viewModel.onDateSelected(convertMillisToDate(it)) }
                    viewModel.onShowDatePicker(false)
                }) { Text("OK", color = PrimaryBlue) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onShowDatePicker(false) }) { Text("Бас тарту") }
            }
        ) { DatePicker(state = dpState) }
    }

    if (state.showTimePicker) {
        Dialog(onDismissRequest = { viewModel.onShowTimePicker(false) }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Бос уақытты таңдаңыз", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimaryBlue)
                    Spacer(modifier = Modifier.height(16.dp))
                    WizardTimeGrid(
                        selectedTime = state.selectedTime,
                        slots = state.availableSlots,
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
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Күн мен уақытты таңдаңыз", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        WizardClickableField(
            value = state.selectedDate,
            enabled = true,
            label = "Қабылдау күні",
            placeholder = "Күнді таңдаңыз",
            onClick = { viewModel.onShowDatePicker(true) }
        )

        WizardClickableField(
            value = state.selectedTime,
            label = "Қабылдау уақыты",
            placeholder = if (state.selectedDate.isEmpty()) "Алдымен күнді таңдаңыз" else "Уақытты таңдаңыз",
            enabled = state.selectedDate.isNotEmpty() && state.availableSlots.isNotEmpty(),
            onClick = { viewModel.onShowTimePicker(true) }
        )

        if (state.selectedDate.isNotBlank() && state.availableSlots.isEmpty()) {
            Text("Бұл күні бос уақыт жоқ немесе демалыс күні", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }

        WizardReadOnlyField(label = "Ұзақтығы", value = state.duration)

        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = state.selectedDate.isNotBlank() && state.selectedTime.isNotBlank(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) { Text("Келесі", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Step 4 – Растау
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StepConfirmScreen(
    state: BookingUiState,
    isLoading: Boolean,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Жазылуды растаңыз", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F9FF)),
            elevation = CardDefaults.cardElevation(0.dp),
            border = BorderStroke(1.dp, Color(0xFFD0E4FF))
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ConfirmRow("Клиника",   state.clinicName)
                HorizontalDivider(color = Color(0xFFD0E4FF), thickness = 0.5.dp)
                ConfirmRow("Қызмет",   state.direction)
                HorizontalDivider(color = Color(0xFFD0E4FF), thickness = 0.5.dp)
                ConfirmRow("Дәрігер",  state.doctorName)
                HorizontalDivider(color = Color(0xFFD0E4FF), thickness = 0.5.dp)
                ConfirmRow("Күні",     state.selectedDate)
                HorizontalDivider(color = Color(0xFFD0E4FF), thickness = 0.5.dp)
                ConfirmRow("Уақыты",   state.selectedTime)
                HorizontalDivider(color = Color(0xFFD0E4FF), thickness = 0.5.dp)
                ConfirmRow("Ұзақтығы", state.duration)
            }
        }

        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }

        Spacer(modifier = Modifier.weight(1f))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = PrimaryBlue)
        } else {
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) { Text("Жазылуды растау", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun ConfirmRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray, fontSize = 13.sp)
        Text(
            value.ifBlank { "—" },
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Private helper composables  (Wizard-ке тән, BookingScreen.kt-мен қайшылық жоқ)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WizardClickableField(
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
            .clickable(enabled = enabled, onClick = onClick)
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
private fun WizardReadOnlyField(label: String, value: String) {
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
private fun WizardTimeGrid(
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
        items(slots, key = { it.time }) { slot ->
            val isSelected = slot.time == selectedTime
            val bg = when {
                !slot.isEnabled -> Color(0xFFE2E6EA)
                isSelected      -> PrimaryBlue
                else            -> Color(0xFFF1F3F5)
            }
            val textColor = when {
                !slot.isEnabled -> Color(0xFF8A8F98)
                isSelected      -> Color.White
                else            -> Color.DarkGray
            }
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = slot.isEnabled) { onTimeSelect(slot.time) },
                color = bg,
                shape = RoundedCornerShape(8.dp),
                border = if (isSelected) null else BorderStroke(1.dp, Color.LightGray)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(slot.time, color = textColor, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                    if (!slot.isEnabled) {
                        Text("Толы", color = textColor, style = MaterialTheme.typography.labelSmall)
                    } else if (slot.capacity > 1) {
                        Text("${slot.bookedCount}/${slot.capacity}", color = textColor, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun WizardDoctorCard(doctor: DoctorOption) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFFE8F4FA)),
                contentAlignment = Alignment.Center
            ) {
                if (doctor.photoUrl.isNotBlank()) {
                    AsyncImage(
                        model = doctor.photoUrl,
                        contentDescription = "Doctor avatar",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(doctor.name.take(1).uppercase(), color = PrimaryBlue, fontWeight = FontWeight.Bold)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(doctor.name, fontWeight = FontWeight.Bold)
                Text(doctor.specialty.ifBlank { "Стоматолог" }, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                if (doctor.experienceYears > 0)
                    Text("Тәжірибе: ${doctor.experienceYears} жыл", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                if (doctor.aboutDoctor.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(doctor.aboutDoctor, color = Color.DarkGray, style = MaterialTheme.typography.bodySmall, maxLines = 3)
                }
            }
        }
    }
}