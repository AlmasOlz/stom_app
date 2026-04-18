package com.example.stomatology.app.presentation.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.stomatology.app.presentation.theme.PrimaryBlue

@Composable
fun DateTimePickerScreen(onConfirm: (String, String) -> Unit) {
    var isTimeTabSelected by remember { mutableStateOf(true) } // Defaulting to Time tab for demo
    var selectedDate by remember { mutableStateOf("19 Март 2024") }
    var selectedTime by remember { mutableStateOf("14:30") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 40.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
    ) {
        Text(if (isTimeTabSelected) "Выберите время" else "Выберите дату", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)

        Spacer(modifier = Modifier.height(24.dp))

        // Tabs
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TabButton("Дата", !isTimeTabSelected, Modifier.weight(1f)) { isTimeTabSelected = false }
            TabButton("Время", isTimeTabSelected, Modifier.weight(1f)) { isTimeTabSelected = true }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isTimeTabSelected) {
            TimeSelectionView(selectedTime) { selectedTime = it }
        } else {
            // Simplified Calendar Placeholder for "Дата" tab
            Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Color(0xFFFAFAFA)), contentAlignment = Alignment.Center) {
                Text("Интерфейс календаря", color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onConfirm(selectedDate, selectedTime) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text(if (isTimeTabSelected) "Записаться" else "Сохранить", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TimeSelectionView(selectedTime: String, onTimeSelect: (String) -> Unit) {
    // 24-HOUR FORMAT AS REQUESTED
    val timeSlots = listOf(
        "08:00", "09:00", "10:00", "11:00",
        "12:00", "13:30", "14:30", "15:00",
        "16:00", "17:00", "18:00", "19:00"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        // Horizontal Date Selector Placeholder
        Box(
            modifier = Modifier.fillMaxWidth().height(80.dp).border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Март 2024 - Горизонтальный скролл дат", color = Color.Gray, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Выберите свободное время", fontSize = 12.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(timeSlots) { time ->
                val isSelected = time == selectedTime
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) PrimaryBlue else Color.Transparent)
                        .border(1.dp, if (isSelected) PrimaryBlue else Color.LightGray, RoundedCornerShape(8.dp))
                        .clickable { onTimeSelect(time) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = time, // Displaying in 24h format
                        fontSize = 12.sp,
                        color = if (isSelected) Color.White else Color.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color.Transparent else Color.Transparent)
            .border(2.dp, if (isSelected) PrimaryBlue else Color.LightGray, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (isSelected) PrimaryBlue else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}