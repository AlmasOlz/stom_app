package com.example.stomatology.app.presentation.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stomatology.app.core.firebase.UserRoles
import com.example.stomatology.app.presentation.theme.PrimaryBlue

@Composable
internal fun AdminSummaryRow(state: AdminDashboardUiState) {
    val doctorCount = state.users.count { user -> user.role == UserRoles.DOCTOR }
    val patientCount = state.users.count { user -> user.role == UserRoles.PATIENT }
    val adminCount = state.users.count { user -> user.role == UserRoles.ADMIN }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryCard(
                title = "Клиникалар",
                value = state.clinics.size.toString(),
                color = PrimaryBlue,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Дәрігерлер",
                value = doctorCount.toString(),
                color = Color(0xFF00897B),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryCard(
                title = "Пациенттер",
                value = patientCount.toString(),
                color = Color(0xFF5E35B1),
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Әкімшілер",
                value = adminCount.toString(),
                color = Color(0xFFEF6C00),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
