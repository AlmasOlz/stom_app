package com.example.stomatology.app.presentation.clinics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage // Coil кітапханасы
import com.example.stomatology.app.presentation.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ClinicDetailScreen(
    clinicId: String,
    serviceName: String,
    onBack: () -> Unit,
    onBookClick: (String, String) -> Unit,
    viewModel: ClinicViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val clinic = state.clinics.find { it.id == clinicId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("О клинике", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            clinic?.let {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 12.dp,
                    color = Color.White
                ) {
                    Button(
                        onClick = { onBookClick(it.id, serviceName) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Записаться на прием", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }
            clinic == null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Клиника не найдена")
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // 1. ИНТЕРНЕТТЕН СУРЕТ ЖҮКТЕУ (Header Image)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .background(Color(0xFFF1F3F5))
                    ) {
                        AsyncImage(
                            model = "https://static.tildacdn.com/tild3032-6633-4366-a532-643335356139/6fgnff.png", // Firebase-тегі немесе API-дағы сілтеме
                            contentDescription = "Фото клиники ${clinic.name}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = clinic.name,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Стоматологический центр", color = Color.Gray, fontSize = 14.sp)
                            }

                            Surface(
                                color = Color(0xFFFFF8E1),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFFA000),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${clinic.rating}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // 2. Байланыс батырмалары (Түзетілді)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ClinicActionBtn(Icons.Default.Call, "Позвонить", Modifier.weight(1f))
                            ClinicActionBtn(Icons.Default.LocationOn, "На карте", Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        InfoItem(Icons.Default.LocationOn, clinic.address)
                        Spacer(modifier = Modifier.height(16.dp))
                        InfoItem(Icons.Default.DateRange, "Пн-Сб: 09:00 - 20:00", status = "Открыто")

                        Spacer(modifier = Modifier.height(24.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = PrimaryBlue.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Выбранная услуга", fontSize = 12.sp, color = Color.Gray)
                                    Text(serviceName, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                                }
                                Text("${clinic.priceFrom} ₸", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text("О клинике", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = clinic.description,
                            fontSize = 15.sp,
                            color = Color.DarkGray,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text("Все услуги", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            clinic.services.forEach { service ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF1F3F5),
                                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = service,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        fontSize = 14.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ClinicActionBtn(icon: ImageVector, label: String, modifier: Modifier) {
    OutlinedButton(
        onClick = { /* Логика */ },
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Icon(
            imageVector = icon, // Түзетілді: Жұлдызшаның орнына келетін иконка
            contentDescription = null,
            tint = PrimaryBlue,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, color = Color.Black, fontSize = 14.sp)
    }
}

@Composable
fun InfoItem(icon: ImageVector, text: String, status: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(PrimaryBlue.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = PrimaryBlue)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text, fontSize = 15.sp, color = Color.Black)
            if (status != null) {
                Text(status, fontSize = 13.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
            }
        }
    }
}