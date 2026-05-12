package com.example.stomatology.app.presentation.doctor_dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.stomatology.app.domain.model.Appointment
import com.example.stomatology.app.domain.model.AppointmentStatus
import com.example.stomatology.app.domain.model.toUiText
import com.example.stomatology.app.presentation.components.AppBackButton
import com.example.stomatology.app.presentation.components.AppEmptyState
import com.example.stomatology.app.presentation.components.AppErrorState
import com.example.stomatology.app.presentation.components.AppLoadingState
import com.example.stomatology.app.presentation.theme.PrimaryBlue

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
        when {
            state.isLoading -> {
                AppLoadingState(
                    message = "Деректер жүктелуде...",
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF7F9FC))
                        .padding(padding)
                )
            }

            state.error != null && state.appointments.isEmpty() -> {
                AppErrorState(
                    message = state.error ?: "Жазбаларды жүктеу кезінде қате пайда болды.",
                    actionText = "Қайталап көру",
                    onAction = viewModel::retryLoadAppointments,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF7F9FC))
                        .padding(padding)
                )
            }

            state.appointments.isEmpty() -> {
                AppEmptyState(
                    title = "Қазір жазбалар жоқ",
                    subtitle = "Пациенттер жазылған кезде олар осы жерде көрсетіледі.",
                    actionText = "Қайталап көру",
                    onAction = viewModel::retryLoadAppointments,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF7F9FC))
                        .padding(padding)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF7F9FC))
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (state.error != null) {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F3))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = state.error ?: "Жазбаларды жаңарту кезінде қате пайда болды.",
                                        color = Color(0xFFD32F2F),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedButton(onClick = viewModel::retryLoadAppointments) {
                                        Text("Қайталау")
                                    }
                                }
                            }
                        }
                    }

                    items(state.appointments, key = { it.id }) { appointment ->
                        DoctorAppointmentCard(
                            appointment = appointment,
                            onOpenDetail = { onOpenAppointmentDetail(appointment.id) },
                            onAccept = { viewModel.accept(appointment.id) },
                            onReject = { viewModel.reject(appointment.id) },
                            onComplete = { viewModel.complete(appointment.id) },
                            onNoShow = { viewModel.markNoShow(appointment.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DoctorAppointmentCard(
    appointment: Appointment,
    onOpenDetail: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onComplete: () -> Unit,
    onNoShow: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = appointment.patientName.ifBlank { "Пациент" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = appointment.service,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = appointment.clinicName,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${appointment.date} • ${appointment.time}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray
            )
            Text(
                text = appointment.status.toUiText(),
                style = MaterialTheme.typography.labelLarge,
                color = PrimaryBlue,
                fontWeight = FontWeight.SemiBold
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onOpenDetail, modifier = Modifier.fillMaxWidth()) {
                    Text("Толығырақ")
                }

                when (appointment.status) {
                    AppointmentStatus.PENDING -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onAccept,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                            ) {
                                Text("Растау")
                            }
                            OutlinedButton(
                                onClick = onReject,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Бас тарту")
                            }
                        }
                    }

                    AppointmentStatus.CONFIRMED,
                    AppointmentStatus.RESCHEDULED -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onComplete,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                            ) {
                                Text("Аяқтау")
                            }
                            OutlinedButton(
                                onClick = onNoShow,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Келмеді")
                            }
                        }
                    }

                    else -> Unit
                }
            }
        }
    }
}
