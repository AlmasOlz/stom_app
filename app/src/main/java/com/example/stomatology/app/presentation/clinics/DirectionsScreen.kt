package com.example.stomatology.app.presentation.clinics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DirectionsScreen(
    onNavigateToClinicList: (String) -> Unit
) {
    val categories = listOf(
        DirectionItem("Чекап", "Полная проверка зубов", Color(0xFFB3E5FC)),
        DirectionItem("Ортодонтия", "Установка импланта", Color(0xFFFFEBEE)),
        DirectionItem("Имплантация", "Восстановление зубного ряда", Color(0xFFFFF3E0)),
        DirectionItem("Хирургия", "Лечение кариеса и каналов", Color(0xFFE3F2FD)),
        DirectionItem("Детская пластика", "Детская пластика уздечки", Color(0xFFEDE7F6)),
        DirectionItem("Терапия", "Диагностика", Color(0xFFFBE9E7))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 24.dp, start = 16.dp, end = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Направления",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Black)
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(categories) { category ->
                DirectionCard(category) {
                    onNavigateToClinicList(category.title)
                }
            }
        }
    }
}

data class DirectionItem(val title: String, val subtitle: String, val bgColor: Color)

@Composable
fun DirectionCard(item: DirectionItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(item.bgColor)
            ) {
                // Placeholder for category image (e.g., braces, implants)
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = item.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = item.subtitle, fontSize = 11.sp, color = Color.Gray, lineHeight = 14.sp)
            }
        }
    }
}