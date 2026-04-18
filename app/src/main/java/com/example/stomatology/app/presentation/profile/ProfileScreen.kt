package com.example.stomatology.app.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stomatology.app.presentation.theme.PrimaryBlue

@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Top Blue Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    color = PrimaryBlue,
                    shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(Icons.Default.Notifications, contentDescription = "Alerts", tint = Color.White)
                Icon(Icons.Default.Edit, contentDescription = "History", tint = Color.White)
            }

            // Avatar Box overlapping the bottom
            Box(
                modifier = Modifier
                    .offset(y = 50.dp)
                    .size(100.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )

                // Edit Button
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))

        // User Info
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Алтынай Мадиханова",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "youremail@domain.com | +01 234 567 89",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Settings Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                ProfileMenuItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    title = "Редактировать профиль",
                    trailing = {}
                )
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                ProfileMenuItem(
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Notifications") },
                    title = "Уведомления",
                    trailing = { Text("ON", color = Color(0xFFFFA500), fontWeight = FontWeight.Bold) }
                )
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                ProfileMenuItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Language") },
                    title = "Язык",
                    trailing = { Text("English", color = Color(0xFFFFA500), fontWeight = FontWeight.Bold) }
                )
            }
        }
    }
}

@Composable
fun ProfileMenuItem(icon: @Composable () -> Unit, title: String, trailing: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, modifier = Modifier.weight(1f), fontSize = 16.sp, color = Color.Black)
        trailing()
    }
}