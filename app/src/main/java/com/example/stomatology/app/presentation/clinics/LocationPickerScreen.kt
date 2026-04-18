package com.example.stomatology.app.presentation.clinics

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stomatology.app.presentation.theme.PrimaryBlue

@Composable
fun LocationPickerScreen(
    onBack: () -> Unit,
    onLocationSelected: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Map Background (Placeholder)
        // In production, this would be a GoogleMap composable from the Maps SDK
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE0E0E0)), // Light gray map placeholder
            contentAlignment = Alignment.Center
        ) {
            // Simulated Map Pin
            Icon(
                Icons.Default.LocationOn,
                contentDescription = "Pin",
                tint = Color.Red,
                modifier = Modifier
                    .size(48.dp)
                    .offset(y = (-24).dp) // Offset to make it look like it's pointing at the center
            )
            Text("Simulated Google Map Area", color = Color.Gray, modifier = Modifier.offset(y = 30.dp))
        }

        // 2. Top Bar (Transparent/Overlaid)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .background(PrimaryBlue, shape = RoundedCornerShape(50))
                    .size(40.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        }

        // 3. Bottom Sheet (Confirmation Area)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(PrimaryBlue)
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "Выберите место",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Current Location",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Алатау, 13 ықшам ауданы",
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Алматы",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onLocationSelected("Алатау, 13 ықшам ауданы, Алматы") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Проверить", color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(16.dp)) // Safe area bottom padding
            }
        }
    }
}