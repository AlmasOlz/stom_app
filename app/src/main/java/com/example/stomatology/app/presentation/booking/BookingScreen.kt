package com.example.stomatology.app.presentation.booking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.stomatology.app.presentation.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    clinicId: String,
    serviceName: String,
    onBookingComplete: () -> Unit,
    onOpenDateTimePicker: () -> Unit = {},
    viewModel: BookingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(clinicId) {
        viewModel.initClinic(clinicId)
        viewModel.onDirectionChange(serviceName)
    }

    LaunchedEffect(state.isBooked) {
        if (state.isBooked) {
            onBookingComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Запись на приём") }
            )
        }
    ) { padding ->
        BookingScreenContent(
            padding = padding,
            state = state,
            onDoctorNameChange = viewModel::onDoctorNameChange,
            onClinicNameChange = viewModel::onClinicNameChange,
            onDirectionChange = viewModel::onDirectionChange,
            onDurationChange = viewModel::onDurationChange,
            onOpenDateTimePicker = onOpenDateTimePicker,
            onConfirmBooking = viewModel::confirmBooking,
            scrollModifier = Modifier.verticalScroll(scrollState)
        )
    }

    if (state.showSuccessDialog) {
        BookingSuccessDialog(
            doctorName = state.doctorName,
            onReturnHome = {
                viewModel.dismissSuccessDialog()
                onBookingComplete()
            },
            onDismiss = { viewModel.dismissSuccessDialog() }
        )
    }
}

@Composable
private fun BookingScreenContent(
    padding: PaddingValues,
    state: BookingUiState,
    onDoctorNameChange: (String) -> Unit,
    onClinicNameChange: (String) -> Unit,
    onDirectionChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onOpenDateTimePicker: () -> Unit,
    onConfirmBooking: () -> Unit,
    scrollModifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .then(scrollModifier)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Клиника ID: ${state.clinicId}",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = state.selectedDate,
            onValueChange = {},
            readOnly = true,
            label = { Text("Дата") },
            placeholder = { Text("Выберите дату") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.selectedTime,
            onValueChange = {},
            readOnly = true,
            label = { Text("Время") },
            placeholder = { Text("Выберите время") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onOpenDateTimePicker,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text("Выбрать дату и время")
        }

        OutlinedTextField(
            value = state.doctorName,
            onValueChange = onDoctorNameChange,
            label = { Text("Имя врача") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.clinicName,
            onValueChange = onClinicNameChange,
            label = { Text("Название клиники") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.direction,
            onValueChange = onDirectionChange,
            label = { Text("Услуга") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true
        )

        OutlinedTextField(
            value = state.duration,
            onValueChange = onDurationChange,
            label = { Text("Продолжительность") },
            modifier = Modifier.fillMaxWidth()
        )

        state.error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = onConfirmBooking,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Подтвердить запись")
            }
        }
    }
}