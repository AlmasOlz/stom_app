package com.example.stomatology.app.presentation.clinics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.stomatology.app.presentation.theme.PrimaryBlue

// Клиниканың моделі
data class Clinic(
    val name: String,
    val rating: Double,
    val reviewsCount: Int,
    val address: String,
    val price: Int,
    val imageUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicSelectionScreen(
    serviceName: String = "Удаление зуба",
    onBackClick: () -> Unit
) {
    // Іздеу өрісінің мәні
    var searchQuery by remember { mutableStateOf("") }

    // Тізімге арналған мысал деректер (Mock Data)
    val clinics = remember {
        listOf(
            Clinic(
                name = "OneDent",
                rating = 4.7,
                reviewsCount = 120,
                address = "Астана, Жиембет жырау 2",
                price = 12000,
                imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQcxqLBoPcgeMd_vlvWNlfaNY15o4TPQlWS7w&s" // Тіс дәрігерінің кабинеті
            ),
            Clinic(
                name = "Astana Stom",
                rating = 4.6,
                reviewsCount = 76,
                address = "Астана, Мангилик Ел 30",
                price = 18000,
                imageUrl = "https://images.unsplash.com/photo-1588776814546-1ffcf47267a5?q=80&w=300&auto=format&fit=crop" // Стоматологиялық кресло
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)) // Ашық сұр фон
    ) {
        // 1. Жоғарғы панель (Top Bar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(top = 48.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Артқа",
                    tint = Color.Black
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = serviceName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Найдено: ${clinics.size}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))

            // 2. Іздеу өрісі (Search Bar)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Поиск клиники или адреса...", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Іздеу",
                        tint = PrimaryBlue
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = PrimaryBlue
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Клиникалар тізімі (LazyColumn)
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                val filteredClinics = clinics.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                            it.address.contains(searchQuery, ignoreCase = true)
                }

                items(filteredClinics) { clinic ->
                    ClinicCard(clinic = clinic)
                }
            }
        }
    }
}

@Composable
fun ClinicCard(clinic: Clinic) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Сурет (Coil AsyncImage)
            AsyncImage(
                model = "https://taplink.st/a/6/0/1/a/14e4be.png?195",
                contentDescription = clinic.name,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE1F5FE)), // Сурет жүктелгенше тұратын фон
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Клиника туралы ақпарат
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = clinic.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Рейтинг
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFB300), // Сары түс
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = clinic.rating.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${clinic.reviewsCount} отзывов)",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Адрес
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = clinic.address,
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Бағасы
                Text(
                    text = "от ${clinic.price} ₸",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryBlue
                )
            }
        }
    }
}