package com.example.stomatology.app.presentation.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stomatology.app.presentation.theme.PrimaryBlue

// Хабарлама түріне қарай деректер моделі
data class NotificationItem(
    val title: String,
    val description: String,
    val time: String,
    val type: NotificationType
)

enum class NotificationType { MISSED, SUCCESS, INFO }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationHistoryScreen(onBack: () -> Unit) {
    val notifications = listOf(
        NotificationItem(
            title = "Пропущено",
            description = "Вы пропустили утреннюю чистку зубов. Не забывайте о гигиене!",
            time = "8:30 AM",
            type = NotificationType.MISSED
        ),
        NotificationItem(
            title = "Пропущено",
            description = "Вы пропустили очистку зубных протезов. Рекомендуется делать это дважды в день.",
            time = "8:00 AM",
            type = NotificationType.MISSED
        ),
        NotificationItem(
            title = "Поздравляем!",
            description = "Отлично! Вы используете ополаскиватель для рта уже 7 дней подряд.",
            time = "9:00 AM",
            type = NotificationType.SUCCESS
        )
    )
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Уведомления", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = PrimaryBlue)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Today", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp))
            }
            items(notifications) { notification ->
                NotificationCard(notification)
            }
        }
    }
}

@Composable
fun NotificationCard(item: NotificationItem) {
    // Қателерді түзету үшін стандартты иконкаларды қолданамыз
    val icon: ImageVector
    val iconColor: Color
    val backgroundColor: Color

    when (item.type) {
        NotificationType.MISSED -> {
            icon = Icons.Default.Warning // 'Error' орнына 'Warning'
            iconColor = Color(0xFFE53935)
            backgroundColor = Color(0xFFFFEBEE)
        }
        NotificationType.SUCCESS -> {
            icon = Icons.Default.CheckCircle
            iconColor = Color(0xFF43A047)
            backgroundColor = Color(0xFFE8F5E9)
        }
        NotificationType.INFO -> {
            icon = Icons.Default.Info // 'NotificationsActive' орнына 'Info'
            iconColor = PrimaryBlue
            backgroundColor = Color(0xFFE3F2FD)
        }
    }

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
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(backgroundColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = item.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                    Text(text = item.time, fontSize = 12.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    lineHeight = 18.sp
                )
            }
        }
    }
}