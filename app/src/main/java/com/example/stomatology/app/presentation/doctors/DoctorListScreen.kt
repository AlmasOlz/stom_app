package com.example.stomatology.app.presentation.doctors

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun DoctorListScreen(onDoctorClick: (String) -> Unit) {
    val doctors = listOf(
        DoctorItem("Нуржан Сатжанов", "Ортодонт"),
        DoctorItem("Ажар Арнабек", "Терапевт"),
        DoctorItem("Динара Ахметова", "Хирург"),
        DoctorItem("Арман Маратович", "Имплантолог"),
        DoctorItem("Лейла Ашимова", "Челюстно-лицевой хирург"),
        DoctorItem("Айгерим Рауан", "Детский хирург")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 40.dp, start = 16.dp, end = 16.dp)
    ) {
        Text("Выбор врача", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)

        Spacer(modifier = Modifier.height(16.dp))

        // Promo Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(PrimaryBlue)
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth(0.6f)) {
                Text("Лечение зубов без боли и страха", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { /* TODO */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Подробнее", color = PrimaryBlue, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Наши врачи", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(doctors) { doc ->
                DoctorCard(doc) { onDoctorClick(doc.name) }
            }
        }
    }
}

data class DoctorItem(val name: String, val specialty: String)

@Composable
fun DoctorCard(doctor: DoctorItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFE0E0E0)) // Image Placeholder
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(doctor.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = Color.Black, lineHeight = 14.sp)
        Text(doctor.specialty, fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center)
    }
}