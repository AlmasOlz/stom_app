package com.example.stomatology.app.presentation.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stomatology.app.presentation.theme.PrimaryBlue

@Composable
fun DateTimePickerScreen(
    initialDate: String = "",
    initialTime: String = "",
    onConfirm: (String, String) -> Unit
) {
    var isTimeTabSelected by rememberSaveable { mutableStateOf(true) }
    var selectedDate by rememberSaveable { mutableStateOf(initialDate.ifBlank { "19 Март 2024" }) }
    var selectedTime by rememberSaveable { mutableStateOf(initialTime.ifBlank { "14:30" }) }

    val dateOptions = remember {
        listOf(
            "19 Март 2024",
            "20 Март 2024",
            "21 Март 2024",
            "22 Март 2024",
            "23 Март 2024"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 40.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
    ) {
        Text(
            text = if (isTimeTabSelected) "Выберите время" else "Выберите дату",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlue
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TabButton(
                text = "Дата",
                isSelected = !isTimeTabSelected,
                modifier = Modifier.weight(1f)
            ) {
                isTimeTabSelected = false
            }

            TabButton(
                text = "Время",
                isSelected = isTimeTabSelected,
                modifier = Modifier.weight(1f)
            ) {
                isTimeTabSelected = true
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isTimeTabSelected) {
            TimeSelectionView(
                selectedTime = selectedTime,
                onTimeSelect = { selectedTime = it }
            )
        } else {
            DateSelectionView(
                dates = dateOptions,
                selectedDate = selectedDate,
                onDateSelect = { selectedDate = it }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onConfirm(selectedDate, selectedTime) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text(
                text = "Сохранить",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DateSelectionView(
    dates: List<String>,
    selectedDate: String,
    onDateSelect: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Выберите доступную дату",
            fontSize = 14.sp,
            color = Color.DarkGray,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        dates.forEach { date ->
            val isSelected = date == selectedDate

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) PrimaryBlue else Color.Transparent)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) PrimaryBlue else Color.LightGray,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onDateSelect(date) }
                    .padding(14.dp)
            ) {
                Text(
                    text = date,
                    color = if (isSelected) Color.White else Color.DarkGray,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun TimeSelectionView(
    selectedTime: String,
    onTimeSelect: (String) -> Unit
) {
    val timeSlots = listOf(
        "08:00", "09:00", "10:00", "11:00",
        "12:00", "13:30", "14:30", "15:00",
        "16:00", "17:00", "18:00", "19:00"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Выберите свободное время",
            fontSize = 12.sp,
            color = Color.DarkGray,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(220.dp)
        ) {
            items(timeSlots) { time ->
                val isSelected = time == selectedTime

                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) PrimaryBlue else Color.Transparent)
                        .border(
                            1.dp,
                            if (isSelected) PrimaryBlue else Color.LightGray,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onTimeSelect(time) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = time,
                        fontSize = 12.sp,
                        color = if (isSelected) Color.White else Color.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                2.dp,
                if (isSelected) PrimaryBlue else Color.LightGray,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) PrimaryBlue else Color.Gray,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}