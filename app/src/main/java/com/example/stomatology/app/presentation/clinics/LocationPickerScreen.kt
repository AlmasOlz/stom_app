package com.example.stomatology.app.presentation.clinics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stomatology.app.presentation.components.AppBackButton
import com.example.stomatology.app.presentation.theme.PrimaryBlue

@Composable
fun LocationPickerScreen(
    onBack: () -> Unit,
    onLocationSelected: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {

        // 1. КАРТА АЙМАҒЫ (Mock Map)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF1F3F5)), // Ашық сұр түс (карта стилі)
            contentAlignment = Alignment.Center
        ) {
            // Орталықтағы маркер (Пин)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Pin",
                    tint = Color.Red,
                    modifier = Modifier.size(54.dp)
                )
                // Маркердің көлеңкесі
                Box(
                    modifier = Modifier
                        .size(10.dp, 4.dp)
                        .background(Color.Black.copy(alpha = 0.2f), CircleShape)
                )
            }
        }

        // 2. ЖОҒАРҒЫ БАТЫРМАЛАР (Back & Search)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AppBackButton(onClick = onBack)
        }

        // 3. ОҢ ЖАҚТАҒЫ ПРАКТИКАЛЫҚ БАТЫРМАЛАР
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp, bottom = 100.dp), // Панельден жоғары тұруы керек
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SmallFloatingActionButton(
                onClick = { /* Центрировать карту */ },
                containerColor = Color.White,
                contentColor = PrimaryBlue,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "My Location")
            }
        }

        // 4. ТӨМЕНГІ МЕКЕНЖАЙ ПАНЕЛІ (Bottom Sheet Style)
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Подтвердите адрес",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(PrimaryBlue.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Алатау, 13 ықшам ауданы",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        Text(
                            text = "Алматы, Казахстан",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { onLocationSelected("Алатау, 13 ықшам ауданы, Алматы") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Выбрать это место",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
