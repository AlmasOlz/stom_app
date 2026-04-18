package com.example.stomatology.app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stomatology.app.presentation.theme.BackgroundGray
import com.example.stomatology.app.presentation.theme.PrimaryBlue

@Composable
fun HomeScreen(
    onNavigateToClinics: () -> Unit,
    onNavigateToAi: () -> Unit,
    onNavigateToOtherServices: () -> Unit // НОВЫЙ КОЛЛБЕК
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .verticalScroll(scrollState)
            .padding(top = 16.dp, bottom = 16.dp)
    ) {
        // Хедер "Привет Алтын"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Привет Алтын 👋",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            // Аватарка-заглушка
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Голубой Баннер
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(PrimaryBlue)
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "Пожалуйста,\nвыберите область, в\nкоторой вам\nнеобходима помощь.",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Секция "У меня есть" (Сетка услуг)
        Text(
            text = "У меня есть",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        val services = listOf(
            ServiceItem("Удаление зуба", Icons.Default.Build, onNavigateToClinics),
            ServiceItem("Протезирование", Icons.Default.Face, onNavigateToClinics),
            ServiceItem("Пломба / Канал", Icons.Default.CheckCircle, onNavigateToClinics),
            ServiceItem("Имплант", Icons.Default.Star, onNavigateToClinics),
            ServiceItem("Сломанный зуб", Icons.Default.Favorite, onNavigateToAi),
            ServiceItem("Брекеты", Icons.Default.Face, onNavigateToClinics)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(horizontal = 8.dp),
            userScrollEnabled = false
        ) {
            items(services) { service ->
                ServiceCard(service)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ОБНОВЛЕННАЯ КНОПКА ПОИСКА ДОПОЛНИТЕЛЬНЫХ УСЛУГ
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { onNavigateToOtherServices() }, // ИСПОЛЬЗУЕМ НОВЫЙ КОЛЛБЕК
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Поиск других процедур", fontSize = 16.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

data class ServiceItem(val title: String, val icon: ImageVector, val onClick: () -> Unit)

@Composable
fun ServiceCard(service: ServiceItem) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { service.onClick() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = service.icon,
            contentDescription = service.title,
            tint = PrimaryBlue,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = service.title,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = Color.Black,
            maxLines = 2
        )
    }
}