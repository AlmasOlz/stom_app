package com.example.stomatology.app.presentation.admin

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stomatology.app.core.firebase.RoleRequestStatus
import com.example.stomatology.app.core.firebase.UserRoles
import com.example.stomatology.app.domain.model.Clinic
import com.example.stomatology.app.presentation.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun UsersTab(
    state: AdminDashboardUiState,
    clinics: List<Clinic>,
    onSelectUser: (AdminUserItem) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onRoleChange: (String) -> Unit,
    onSpecialtyChange: (String) -> Unit,
    onClinicIdChange: (String) -> Unit,
    onSaveUser: () -> Unit,
    onClearUser: () -> Unit
) {
    var clinicMenuExpanded by remember { mutableStateOf(false) }
    val selectedClinic = clinics.firstOrNull { clinic -> clinic.id == state.userForm.clinicId }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Пайдаланушыны өңдеу", fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = state.userForm.selectedUserId,
                onValueChange = {},
                label = { Text("UID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = false,
                supportingText = { Text("Тек оқу режимі") },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Gray,
                    disabledLabelColor = Color.Gray
                )
            )

            OutlinedTextField(
                value = state.userForm.displayName,
                onValueChange = onDisplayNameChange,
                label = { Text("Көрінетін аты") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.userForm.phone,
                onValueChange = onPhoneChange,
                label = { Text("Телефон") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RoleButton(
                    text = "Пациент",
                    selected = state.userForm.role == UserRoles.PATIENT,
                    onClick = { onRoleChange(UserRoles.PATIENT) },
                    modifier = Modifier.weight(1f)
                )
                RoleButton(
                    text = "Дәрігер",
                    selected = state.userForm.role == UserRoles.DOCTOR,
                    onClick = { onRoleChange(UserRoles.DOCTOR) },
                    modifier = Modifier.weight(1f)
                )
                RoleButton(
                    text = "Әкімші",
                    selected = state.userForm.role == UserRoles.ADMIN,
                    onClick = { onRoleChange(UserRoles.ADMIN) },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = state.userForm.specialty,
                onValueChange = onSpecialtyChange,
                label = { Text("Дәрігер мамандығы") },
                enabled = state.userForm.role == UserRoles.DOCTOR,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            ExposedDropdownMenuBox(
                expanded = clinicMenuExpanded,
                onExpandedChange = { expanded ->
                    if (state.userForm.role == UserRoles.DOCTOR) {
                        clinicMenuExpanded = expanded
                    }
                }
            ) {
                OutlinedTextField(
                    value = selectedClinic?.name.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Клиника") },
                    placeholder = { Text("Клиниканы таңдаңыз") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = clinicMenuExpanded)
                    },
                    enabled = state.userForm.role == UserRoles.DOCTOR,
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    singleLine = true
                )

                DropdownMenu(
                    expanded = clinicMenuExpanded,
                    onDismissRequest = { clinicMenuExpanded = false }
                ) {
                    clinics.forEach { clinic ->
                        DropdownMenuItem(
                            text = { Text(clinic.name) },
                            onClick = {
                                onClinicIdChange(clinic.id)
                                clinicMenuExpanded = false
                            }
                        )
                    }
                }
            }

            if (state.userForm.clinicId.isNotBlank() && selectedClinic == null) {
                Text(
                    text = "Клиника ID: ${state.userForm.clinicId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onSaveUser,
                    enabled = !state.isSavingUser,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (state.isSavingUser) "Сақталуда..." else "Сақтау")
                }

                OutlinedButton(
                    onClick = onClearUser,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Тазалау")
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = "Пайдаланушылар: ${state.users.size}",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    state.users.forEach { user ->
        UserItemCard(
            user = user,
            onSelect = { onSelectUser(user) }
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun UserItemCard(
    user: AdminUserItem,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(user.displayName, fontWeight = FontWeight.Bold)
            Text(user.email.ifBlank { "Email көрсетілмеген" }, color = Color.Gray)
            Text("Түрі: ${roleUiName(user.role)}", color = PrimaryBlue)
            if (user.requestStatus == RoleRequestStatus.PENDING && user.requestedRole == UserRoles.DOCTOR) {
                Text(
                    text = "Сұраныс: дәрігер (әкімші растауын күтуде)",
                    color = Color(0xFFD84315),
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (user.specialty.isNotBlank()) {
                Text("Мамандығы: ${user.specialty}", color = Color.DarkGray)
            }

            if (user.clinicName.isNotBlank()) {
                Text("Клиника: ${user.clinicName}", color = Color.DarkGray)
            } else if (user.clinicId.isNotBlank()) {
                Text("Клиника ID: ${user.clinicId}", color = Color.DarkGray)
            }
        }
    }
}

@Composable
private fun RoleButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) PrimaryBlue.copy(alpha = 0.15f) else Color.Transparent
        )
    ) {
        Text(text, color = if (selected) PrimaryBlue else Color.DarkGray)
    }
}

private fun roleUiName(role: String): String {
    return when (role) {
        UserRoles.ADMIN -> "Әкімші"
        UserRoles.DOCTOR -> "Дәрігер"
        else -> "Пациент"
    }
}
