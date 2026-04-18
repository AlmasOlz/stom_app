package com.example.stomatology.app.presentation.clinics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stomatology.app.domain.model.Clinic

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicListScreen(
    onBack: () -> Unit,
    onClinicClick: (String) -> Unit
) {
    // Mocked data for UI demonstration. In production, this comes from ViewModel/Firestore
    val clinics = listOf(
        Clinic("1", "OneDent", 4.8, 10500, "Astana, Kabanbay Batyr 11", listOf("Implant", "Surgery")),
        Clinic("2", "Dent Lux", 4.5, 8200, "Astana, Mangilik El 23", listOf("Whitening", "Therapy"))
    )

    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Clinics") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search clinics, services...") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(clinics.filter { it.name.contains(searchQuery, ignoreCase = true) }) { clinic ->
                    ClinicItem(clinic) { onClinicClick(clinic.id) }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun ClinicItem(clinic: Clinic, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(clinic.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(clinic.address, color = Color.Gray, fontSize = 14.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFFC107))
                Text("${clinic.rating}", fontWeight = FontWeight.Medium)
            }
        }
    }
}