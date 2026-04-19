package com.example.stomatology.app.presentation.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
fun NotificationHistoryScreen(
    onBack: () -> Unit
) {
    val todayNotifications = listOf(
        NotificationItem(
            title = "Missed",
            message = "You missed the reminder regarding brushing your teeth in the morning.",
            time = "8:30 AM",
            color = Color(0xFFE53935),
            isUnread = true
        ),
        NotificationItem(
            title = "Missed",
            message = "You missed the reminder regarding washing your dentures in the morning.",
            time = "8:00 AM",
            color = Color(0xFFEF5350)
        )
    )

    val yesterdayNotifications = listOf(
        NotificationItem(
            title = "Missed",
            message = "You missed the reminder regarding brushing your teeth in the morning.",
            time = "8:30 AM",
            color = Color(0xFFEF5350)
        ),
        NotificationItem(
            title = "Missed",
            message = "You missed the reminder regarding washing your dentures in the morning.",
            time = "8:00 AM",
            color = Color(0xFFEF5350)
        ),
        NotificationItem(
            title = "Congratulations!",
            message = "You have completed 7 days streak of mouth wash everyday.",
            time = "9:00 AM",
            color = Color(0xFF66BB6A)
        ),
        NotificationItem(
            title = "Missed",
            message = "You missed the reminder regarding washing your dentures in the night.",
            time = "10:00 PM",
            color = Color(0xFF3949AB)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
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
                        .background(
                            Color.DarkGray.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(50)
                        )
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Уведомления",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item { DateHeader("Today") }
            items(todayNotifications) { item ->
                NotificationCard(item)
            }

            item { DateHeader("Yesterday") }
            items(yesterdayNotifications) { item ->
                NotificationCard(item)
            }

            item { DateHeader("Older") }
        }
    }
}

@Composable
fun DateHeader(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE0E0E0))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color.DarkGray,
            fontWeight = FontWeight.Medium
        )
    }
}

data class NotificationItem(
    val title: String,
    val message: String,
    val time: String,
    val color: Color,
    val isUnread: Boolean = false
)

@Composable
fun NotificationCard(item: NotificationItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .width(12.dp)
                .padding(top = 16.dp)
        ) {
            if (item.isUnread) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.Red)
                )
            }
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(item.color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "!",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = item.time,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.message,
                fontSize = 12.sp,
                color = Color.Gray,
                lineHeight = 16.sp
            )
        }
    }

    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
}