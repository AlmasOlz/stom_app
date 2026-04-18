package com.example.stomatology.app.presentation.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stomatology.app.presentation.theme.PrimaryBlue

@Composable
fun NotificationHistoryScreen(onBack: () -> Unit) {
    val todayNotifications = listOf(
        NotificationItem("Missed", "You missed the reminder regarding brushing your teeth in the morning.", "8:30 AM", Color(0xFFE53935), isUnread = true),
        NotificationItem("Missed", "You missed the reminder regarding washing your dentures in the morning.", "8:00 AM", Color(0xFFEF5350))
    )

    val yesterdayNotifications = listOf(
        NotificationItem("Missed", "You missed the reminder regarding brushing your teeth in the morning.", "8:30 AM", Color(0xFFEF5350)),
        NotificationItem("Missed", "You missed the reminder regarding washing your dentures in the morning.", "8:00 AM", Color(0xFFEF5350)),
        NotificationItem("Congratulations!", "You have completed 7 days streak of mouth wash everyday", "9:00 AM", Color(0xFF66BB6A)),
        NotificationItem("Missed", "You missed the reminder regarding washing your dentures in the night.", "10:00 PM", Color(0xFF3949AB))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top Bar Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(
                    color = PrimaryBlue,
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                ),
            contentAlignment = Alignment.BottomStart
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .background(Color.DarkGray.copy(alpha = 0.5f), shape = RoundedCornerShape(50))
                        .size(32.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("Уведомления", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        }

        // List Area
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item { DateHeader("Today") }
            items(todayNotifications) { NotificationCard(it) }

            item { DateHeader("Yesterday") }
            items(yesterdayNotifications) { NotificationCard(it) }

            item { DateHeader("Older") }
        }
    }
}

@Composable
fun DateHeader(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE0E0E0)) // Gray header background
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text = text, fontSize = 14.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
    }
}

data class NotificationItem(val title: String, val message: String, val time: String, val color: Color, val isUnread: Boolean = false)

@Composable
fun NotificationCard(item: NotificationItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Unread Dot
        Box(modifier = Modifier.width(12.dp).padding(top = 16.dp)) {
            if (item.isUnread) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.Red))
            }
        }

        // Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(item.color),
            contentAlignment = Alignment.Center
        ) {
            // Simplified icon logic for mockup mapping
            Text("!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Content
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = item.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(text = item.time, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = item.message, fontSize = 12.sp, color = Color.Gray, lineHeight = 16.sp)
        }
    }
    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
}