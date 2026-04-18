package com.example.stomatology.app.presentation.doctors

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stomatology.app.presentation.theme.PrimaryBlue

@Composable
fun DoctorProfileScreen(doctorName: String, onBookClick: () -> Unit) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Cover Photo Placeholder
        Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(Color(0xFFCFD8DC)))

        // Doctor Avatar overlapping cover
        Box(
            modifier = Modifier
                .offset(y = (-50).dp)
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        )

        Column(
            modifier = Modifier.offset(y = (-30).dp).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(doctorName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
            Text("Хирург", fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(32.dp))

            // Stats Grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem("5000", "Успешно проведенных\nопераций", Modifier.weight(1f))
                StatItem("1500", "Операций по\nкостной пластике", Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem("12+", "лет практики", Modifier.weight(1f))
                StatItem("20+", "Сертификатов\nмеждународного образца", Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(color = Color.LightGray)
            Spacer(modifier = Modifier.height(16.dp))

            Text("Довольные пациенты", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(16.dp))

            // Patient Photos Placeholder
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f).height(100.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFE0E0E0)))
                Box(modifier = Modifier.weight(1f).height(100.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFE0E0E0)))
                Box(modifier = Modifier.weight(1f).height(100.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFE0E0E0)))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onBookClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Записаться", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StatItem(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        Text(label, fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center, lineHeight = 14.sp)
    }
}