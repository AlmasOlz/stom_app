package com.example.stomatology.app.presentation.booking

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    clinicId: String,
    onBookingComplete: () -> Unit,
    viewModel: BookingViewModel = hiltViewModel()
) {
    var selectedDate by remember { mutableStateOf("2026-05-12") } // Example hardcoded, replace with DatePicker
    var selectedTime by remember { mutableStateOf("10:00 AM") }
    var doctorName by remember { mutableStateOf("Dr. Aigerim Rauan") }

    val isBooked by viewModel.isBooked.collectAsState()

    LaunchedEffect(isBooked) {
        if (isBooked) onBookingComplete()
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Book Appointment") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Text("Select Date & Time for Clinic $clinicId", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = selectedDate,
                onValueChange = { selectedDate = it },
                label = { Text("Date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = selectedTime,
                onValueChange = { selectedTime = it },
                label = { Text("Time (HH:MM AM/PM)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { viewModel.bookAppointment(clinicId, doctorName, selectedDate, selectedTime) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirm Booking")
            }
        }
    }
}