package com.example.stomatology.app.presentation.clinics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicListScreen(
    onBack: () -> Unit,
    onClinicClick: (String) -> Unit
) {
    val clinics = listOf(
        "OneDent", "Dent Lux", "Astana Stom", "Agzamov clinic",
        "Dent Love", "dr. Zhakenova", "Стоматология на Абая"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Стоматологии", fontSize = 18.sp, fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            items(clinics) { clinicName ->
                ClinicListItem(name = clinicName) {
                    onClinicClick(clinicName)
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun ClinicListItem(name: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = name, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("4.5", fontSize = 12.sp, color = Color.DarkGray)
                Icon(Icons.Default.Star, contentDescription = "Star", tint = Color(0xFFFFA000), modifier = Modifier.size(14.dp).padding(horizontal = 2.dp))
                Text("10.5k Отзывов", fontSize = 12.sp, color = Color.Gray)
            }
        }

        // Placeholder for clinic image
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE1F5FE))
        )
    }
}