package com.example.stomatology.app.presentation.clinics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.stomatology.app.presentation.components.AppBackButton
import com.example.stomatology.app.presentation.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicListScreen(
    serviceName: String,
    onBack: () -> Unit,
    onClinicClick: (String) -> Unit,
    viewModel: ClinicViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // Сүзу логикасы: қызметі бойынша + іздеу жолағы бойынша
    val filteredClinics = state.clinics.filter { clinic ->
        val matchesService = serviceName.isBlank() ||
                clinic.services.any { it.equals(serviceName, ignoreCase = true) }
        val matchesSearch = clinic.name.contains(searchQuery, ignoreCase = true) ||
                clinic.address.contains(searchQuery, ignoreCase = true)
        matchesService && matchesSearch
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (serviceName.isBlank()) "Барлық клиникалар" else serviceName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (serviceName.isNotBlank()) {
                            Text("Табылды: ${filteredClinics.size}", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                },
                navigationIcon = { AppBackButton(onClick = onBack) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8F9FA)) // Ашық сұр фон (карточкалар жақсы көрінуі үшін)
        ) {
            // 1. Іздеу жолағы (Search Bar)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Клиника немесе мекенжай іздеу...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryBlue) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = PrimaryBlue
                ),
                singleLine = true
            )

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else if (filteredClinics.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Өкінішке орай, ештеңе табылмады", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredClinics) { clinic ->
                        ClinicCardItem(
                            name = clinic.name,
                            address = clinic.address,
                            rating = clinic.rating,
                            reviews = clinic.reviews,
                            price = clinic.priceFrom,
                            imageUrl = clinic.imageUrl,
                            onClick = { onClinicClick(clinic.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClinicCardItem(
    name: String,
    address: String,
    rating: Double,
    reviews: Int,
    price: Int,
    imageUrl: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Клиника суреті (Coil)
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE1F5FE)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Рейтинг пен пікірлер
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFA000),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$rating",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "($reviews пікір)",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Мекенжай
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = address,
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Баға белгісі
                Text(
                    text = "$price ₸ бастап",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryBlue
                )
            }
        }
    }
}
