package com.example.stomatology.app.presentation.doctor_dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.stomatology.app.domain.model.toUiText
import com.example.stomatology.app.presentation.components.AppBackButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorAppointmentListScreen(
    viewModel: DoctorAppointmentViewModel,
    onBack: () -> Unit,
    onOpenAppointmentDetail: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Дәрігер жазбалары") },
                navigationIcon = { AppBackButton(onClick = onBack) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF7F9FC)
    ) { padding ->
        if (state.appointments.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF7F9FC))
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text("Қазір жазбалар жоқ")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF7F9FC))
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.appointments) { appointment ->
                    Card(
                        onClick = { onOpenAppointmentDetail(appointment.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = appointment.patientName.ifBlank { "Емделуші" },
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${appointment.service} • ${appointment.date} • ${appointment.time}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                            Text(
                                text = appointment.status.toUiText(),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}
