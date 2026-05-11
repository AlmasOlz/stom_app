package com.example.stomatology.app.presentation.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stomatology.app.domain.model.Clinic
import com.example.stomatology.app.presentation.theme.PrimaryBlue

@Composable
internal fun ClinicsTab(
    state: AdminDashboardUiState,
    onNameChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onServicesChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onImageUrlChange: (String) -> Unit,
    onLatitudeChange: (String) -> Unit,
    onLongitudeChange: (String) -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit,
    onEdit: (Clinic) -> Unit,
    onDelete: (String) -> Unit
) {
    val form = state.clinicForm

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = if (form.id.isBlank()) "Жаңа клиника" else "Клиниканы өңдеу",
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = form.name,
                onValueChange = onNameChange,
                label = { Text("Атауы") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = form.address,
                onValueChange = onAddressChange,
                label = { Text("Мекенжай") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = form.servicesInput,
                onValueChange = onServicesChange,
                label = { Text("Қызметтер (үтір арқылы)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = form.priceFrom,
                onValueChange = onPriceChange,
                label = { Text("Бастапқы баға (₸)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = form.description,
                onValueChange = onDescriptionChange,
                label = { Text("Сипаттама") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = form.imageUrl,
                onValueChange = onImageUrlChange,
                label = { Text("Сурет сілтемесі") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = form.latitude,
                    onValueChange = onLatitudeChange,
                    label = { Text("Ендік") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = form.longitude,
                    onValueChange = onLongitudeChange,
                    label = { Text("Бойлық") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onSave,
                    enabled = !state.isSavingClinic,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (state.isSavingClinic) "Сақталуда..." else "Сақтау")
                }

                OutlinedButton(
                    onClick = onClear,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Тазалау")
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = "Тіркелген клиникалар: ${state.clinics.size}",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    state.clinics.forEach { clinic ->
        ClinicItemCard(
            clinic = clinic,
            onEdit = { onEdit(clinic) },
            onDelete = { onDelete(clinic.id) }
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ClinicItemCard(
    clinic: Clinic,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(clinic.name, fontWeight = FontWeight.Bold)
            Text(clinic.address.ifBlank { "Мекенжай көрсетілмеген" }, color = Color.Gray)
            Text(
                text = "Бастапқы баға: ${clinic.priceFrom} ₸",
                color = PrimaryBlue,
                fontWeight = FontWeight.SemiBold
            )
            if (clinic.services.isNotEmpty()) {
                Text(
                    text = clinic.services.joinToString(", "),
                    color = Color.DarkGray
                )
            }
            if (clinic.latitude != 0.0 || clinic.longitude != 0.0) {
                Text(
                    text = "Координаталар: ${clinic.latitude}, ${clinic.longitude}",
                    color = Color.Gray
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                    Text("Өңдеу")
                }
                OutlinedButton(onClick = onDelete, modifier = Modifier.weight(1f)) {
                    Text("Жою")
                }
            }
        }
    }
}
