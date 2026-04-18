package com.example.stomatology.app.presentation.reminders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.stomatology.app.presentation.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()

    // States for toggles
    var brushMorning by remember { mutableStateOf(false) }
    var brushEvening by remember { mutableStateOf(true) }
    var mouthwash by remember { mutableStateOf(false) }
    var flossing by remember { mutableStateOf(false) }

    // States for dialog
    var showTimePicker by remember { mutableStateOf(false) }
    var activeReminderKey by remember { mutableStateOf("") } // To know which switch triggered the dialog

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(
                        color = PrimaryBlue,
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    ),
                contentAlignment = Alignment.BottomStart
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .background(Color.DarkGray.copy(alpha = 0.6f), shape = RoundedCornerShape(50))
                            .size(32.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Ежедневные\nнапоминания",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        lineHeight = 24.sp
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {

            // 1. Tooth brush section
            ReminderSection(
                title = "Чистка зубов",
                items = listOf(
                    ReminderToggle("Утром", brushMorning) {
                        brushMorning = it
                        if (it) { activeReminderKey = "Утром"; showTimePicker = true }
                    },
                    ReminderToggle("Вечером", brushEvening) {
                        brushEvening = it
                        if (it) { activeReminderKey = "Вечером"; showTimePicker = true }
                    }
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Mouthwash section
            ReminderSection(
                title = "Mouthwash",
                note = "Note: Best used at a different time to brushing. For example after lunch.",
                items = listOf(
                    ReminderToggle("Reminder", mouthwash) {
                        mouthwash = it
                        if (it) { activeReminderKey = "Mouthwash"; showTimePicker = true }
                    }
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Flossing section
            ReminderSection(
                title = "Flossing",
                note = "Note: Best to set after meals once a day.",
                items = listOf(
                    ReminderToggle("Reminder", flossing) {
                        flossing = it
                        if (it) { activeReminderKey = "Flossing"; showTimePicker = true }
                    }
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Save Button
            Button(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.End)
                    .width(120.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Save", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        // Dialog Overlay
        if (showTimePicker) {
            TimePickerDialog(
                onDismiss = { showTimePicker = false },
                onSave = { hour, minute, isAm ->
                    // Here you would save the time to a ViewModel/DataStore
                    showTimePicker = false
                }
            )
        }
    }
}

data class ReminderToggle(val label: String, val isChecked: Boolean, val onToggle: (Boolean) -> Unit)

@Composable
fun ReminderSection(title: String, note: String? = null, items: List<ReminderToggle>) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp), // Space for overlapping title
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp)) {
                if (note != null) {
                    Text(text = note, fontSize = 10.sp, color = Color(0xFF4A148C), lineHeight = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.label, color = Color(0xFF1A237E), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Switch(
                            checked = item.isChecked,
                            onCheckedChange = item.onToggle,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF4DB6AC), // Teal color from mockup
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.LightGray
                            )
                        )
                    }
                }
            }
        }

        // Overlapping Blue Pill Title
        Box(
            modifier = Modifier
                .padding(start = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(PrimaryBlue)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun TimePickerDialog(onDismiss: () -> Unit, onSave: (String, String, Boolean) -> Unit) {
    var hours by remember { mutableStateOf("08") }
    var minutes by remember { mutableStateOf("00") }
    var isAm by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Введите время", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Hours input
                    OutlinedTextField(
                        value = hours,
                        onValueChange = { if (it.length <= 2) hours = it },
                        modifier = Modifier.width(60.dp),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                    )
                    Text(" : ", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    // Minutes input
                    OutlinedTextField(
                        value = minutes,
                        onValueChange = { if (it.length <= 2) minutes = it },
                        modifier = Modifier.width(60.dp),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                    )
                    Spacer(modifier = Modifier.width(16.dp))

                    // AM/PM Toggle (Simplified as a clickable box for UI demonstration)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isAm) PrimaryBlue else Color.LightGray)
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        Text("AM", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { onSave(hours, minutes, isAm) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Сохранить", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}