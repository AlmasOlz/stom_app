package com.example.stomatology.app.presentation.reminders

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.stomatology.app.presentation.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    var brushMorning by rememberSaveable { mutableStateOf(false) }
    var brushEvening by rememberSaveable { mutableStateOf(true) }
    var mouthwash by rememberSaveable { mutableStateOf(false) }
    var flossing by rememberSaveable { mutableStateOf(false) }

    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    var activeReminderKey by rememberSaveable { mutableStateOf("") }

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
                            .background(
                                Color.DarkGray.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(50)
                            )
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
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
            ReminderSection(
                title = "Чистка зубов",
                items = listOf(
                    ReminderToggle("Утром", brushMorning) {
                        brushMorning = it
                        if (it) {
                            activeReminderKey = "Утром"
                            showTimePicker = true
                        }
                    },
                    ReminderToggle("Вечером", brushEvening) {
                        brushEvening = it
                        if (it) {
                            activeReminderKey = "Вечером"
                            showTimePicker = true
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            ReminderSection(
                title = "Mouthwash",
                note = "Note: Best used at a different time to brushing. For example after lunch.",
                items = listOf(
                    ReminderToggle("Reminder", mouthwash) {
                        mouthwash = it
                        if (it) {
                            activeReminderKey = "Mouthwash"
                            showTimePicker = true
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            ReminderSection(
                title = "Flossing",
                note = "Note: Best to set after meals once a day.",
                items = listOf(
                    ReminderToggle("Reminder", flossing) {
                        flossing = it
                        if (it) {
                            activeReminderKey = "Flossing"
                            showTimePicker = true
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (activeReminderKey.isNotBlank()) {
                Text(
                    text = "Активное напоминание: $activeReminderKey",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.End)
                    .width(120.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text(
                    text = "Save",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        if (showTimePicker) {
            TimePickerDialog(
                onDismiss = { showTimePicker = false },
                onSave = { _, _, _ ->
                    showTimePicker = false
                }
            )
        }
    }
}

data class ReminderToggle(
    val label: String,
    val isChecked: Boolean,
    val onToggle: (Boolean) -> Unit
)

@Composable
fun ReminderSection(
    title: String,
    note: String? = null,
    items: List<ReminderToggle>
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 24.dp,
                    bottom = 16.dp
                )
            ) {
                note?.let {
                    Text(
                        text = it,
                        fontSize = 10.sp,
                        color = Color(0xFF4A148C),
                        lineHeight = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.label,
                            color = Color(0xFF1A237E),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        Switch(
                            checked = item.isChecked,
                            onCheckedChange = item.onToggle,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF4DB6AC),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.LightGray
                            )
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .padding(start = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(PrimaryBlue)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Boolean) -> Unit
) {
    var hours by rememberSaveable { mutableStateOf("08") }
    var minutes by rememberSaveable { mutableStateOf("00") }
    var isAm by rememberSaveable { mutableStateOf(true) }

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
                Text(
                    text = "Введите время",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = hours,
                        onValueChange = {
                            if (it.length <= 2 && it.all { ch -> ch.isDigit() }) hours = it
                        },
                        modifier = Modifier.width(70.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                    )

                    Text(
                        text = " : ",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = minutes,
                        onValueChange = {
                            if (it.length <= 2 && it.all { ch -> ch.isDigit() }) minutes = it
                        },
                        modifier = Modifier.width(70.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = { isAm = !isAm },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAm) PrimaryBlue else Color.LightGray
                        )
                    ) {
                        Text(
                            text = if (isAm) "AM" else "PM",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { onSave(hours, minutes, isAm) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Сохранить",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}