package com.example.stomatology.app.presentation.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stomatology.app.presentation.theme.PrimaryBlue

@Composable
fun BookingFormScreen(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryBlue)
                .padding(top = 40.dp, start = 20.dp, end = 20.dp, bottom = 16.dp)
        ) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Записаться на прием", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color.White)
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                }
                Spacer(modifier = Modifier.height(24.dp))
                // Progress Bar
                Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(Color.White.copy(alpha = 0.3f))) {
                    Box(modifier = Modifier.fillMaxWidth(0.3f).height(4.dp).background(Color.White)) // 30% progress
                }
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {
            Text("Дата & Время", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoField("Дата", "05.06.2024", Modifier.weight(1f))
                InfoField("Начало", "12:00", Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            InfoField("Продолжительность", "3 часа", Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(24.dp))
            Text("Направление", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
            Spacer(modifier = Modifier.height(12.dp))
            InfoField("Выберите направление:", "имплантация", Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(24.dp))
            Text("Данные о клинике", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
            Spacer(modifier = Modifier.height(12.dp))
            InfoField("Название стоматологии", "OneDent", Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            InfoField("Лечащий врач", "Асель Муратова", Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Продолжить", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun InfoField(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(label, fontSize = 10.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
    }
}