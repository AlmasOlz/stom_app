package com.example.stomatology.app.presentation.doctor_dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.stomatology.app.domain.model.Appointment
import com.example.stomatology.app.domain.model.AppointmentStatus
import com.example.stomatology.app.domain.model.toUiText
import com.example.stomatology.app.presentation.components.AppBackButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorAppointmentDetailScreen(
    appointmentId: String,
    viewModel: DoctorAppointmentViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val appointment = state.appointments.firstOrNull { it.id == appointmentId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Жазба ақпараты") },
                navigationIcon = { AppBackButton(onClick = onBack) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF7F9FC)
    ) { padding ->
        if (appointment == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF7F9FC))
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text("Жазба табылмады")
            }
        } else {
            AppointmentDetailContent(
                appointment = appointment,
                onAccept = { viewModel.accept(appointment.id) },
                onReject = { viewModel.reject(appointment.id) },
                onComplete = { viewModel.complete(appointment.id) },
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF7F9FC))
                    .padding(padding)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun AppointmentDetailContent(
    appointment: Appointment,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFinalStatus =
        appointment.status == AppointmentStatus.COMPLETED ||
            appointment.status == AppointmentStatus.REJECTED ||
            appointment.status == AppointmentStatus.CANCELLED

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = appointment.patientName.ifBlank { "Емделуші" },
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Телефон: ${appointment.patientPhone.ifBlank { "Көрсетілмеген" }}")
                Text("Клиника: ${appointment.clinicName}")
                Text("Қызмет: ${appointment.service}")
                Text("Күні: ${appointment.date}")
                Text("Уақыты: ${appointment.time}")
                Text("Күйі: ${appointment.status.toUiText()}")
            }
        }

        Button(
            onClick = onAccept,
            enabled = !isFinalStatus,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Қабылдау")
        }

        OutlinedButton(
            onClick = onReject,
            enabled = !isFinalStatus,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Бас тарту")
        }

        OutlinedButton(
            onClick = onComplete,
            enabled = !isFinalStatus,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Аяқтау")
        }
    }
}
