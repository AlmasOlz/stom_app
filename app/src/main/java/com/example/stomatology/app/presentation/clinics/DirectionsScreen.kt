package com.example.stomatology.app.presentation.clinics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stomatology.app.presentation.theme.PrimaryBlue

// Бағыттардың деректер моделі
data class ServiceCategory(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun DirectionsScreen(
    onNavigateToClinicList: (String) -> Unit
) {
    val categories = remember {
        listOf(
            ServiceCategory("Чекап", "Осмотр и чистка", Icons.Default.CheckCircle, Color(0xFFE3F2FD)),
            ServiceCategory("Имплантация", "Восстановление зубов", Icons.Default.AddCircle, Color(0xFFF3E5F5)),
            ServiceCategory("Ортодонтия", "Исправление прикуса", Icons.Default.Settings, Color(0xFFFFF3E0)),
            ServiceCategory("Хирургия", "Удаление и операции", Icons.Default.Warning, Color(0xFFFFEBEE)),
            ServiceCategory("Терапия", "Лечение кариеса", Icons.Default.Favorite, Color(0xFFE8F5E9)),
            ServiceCategory("Детская", "Для маленьких героев", Icons.Default.Face, Color(0xFFFFFDE7))
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)) // Жұмсақ фон
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Заголовок бөлімі
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Добрый день! 👋",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Направления",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
            }

            // Профиль немесе Хабарлама иконкасы
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                IconButton(onClick = { /* Поиск */ }) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryBlue)
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Бағыттар торы (Grid)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(categories) { category ->
                DirectionCard(category = category) {
                    onNavigateToClinicList(category.title)
                }
            }
        }
    }
}

@Composable
fun DirectionCard(
    category: ServiceCategory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Сәл көлеңке немесе Border
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Иконка блогы
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(category.color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = PrimaryBlue, // Немесе түске қарай қою түс
                    modifier = Modifier.size(24.dp)
                )
            }

            // Мәтін блогы
            Column {
                Text(
                    text = category.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = category.description,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )
            }
        }
    }
}