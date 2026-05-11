package com.example.stomatology.app.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.stomatology.app.core.firebase.UserRoles
import com.example.stomatology.app.presentation.theme.PrimaryBlue
import com.example.stomatology.app.presentation.theme.TextDark
import com.example.stomatology.app.presentation.theme.TextGray

private val LegalTextGray = Color(0xFF4A4A4A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    var clinicMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.requestedRole) {
        if (state.requestedRole == UserRoles.DOCTOR) {
            viewModel.loadClinicsIfNeeded()
        }
    }

    val selectedClinicName = state.clinics
        .firstOrNull { clinic -> clinic.id == state.clinicId }
        ?.name
        .orEmpty()

    val subtitle = if (state.requestedRole == UserRoles.DOCTOR) {
        "Дәрігер аккаунтына өтінім"
    } else {
        "Пациент аккаунтын ашу"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AuthWaveHeader(
                title = "Тіркелу",
                subtitle = subtitle
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .imePadding()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.onRequestedRoleChange(UserRoles.PATIENT) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (state.requestedRole == UserRoles.PATIENT) {
                                PrimaryBlue.copy(alpha = 0.14f)
                            } else {
                                Color.Transparent
                            }
                        )
                    ) {
                        Text(
                            text = "Пациент",
                            fontWeight = if (state.requestedRole == UserRoles.PATIENT) {
                                FontWeight.SemiBold
                            } else {
                                FontWeight.Normal
                            },
                            color = if (state.requestedRole == UserRoles.PATIENT) {
                                PrimaryBlue
                            } else {
                                TextDark
                            }
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.onRequestedRoleChange(UserRoles.DOCTOR) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (state.requestedRole == UserRoles.DOCTOR) {
                                PrimaryBlue.copy(alpha = 0.14f)
                            } else {
                                Color.Transparent
                            }
                        )
                    ) {
                        Text(
                            text = "Дәрігер",
                            fontWeight = if (state.requestedRole == UserRoles.DOCTOR) {
                                FontWeight.SemiBold
                            } else {
                                FontWeight.Normal
                            },
                            color = if (state.requestedRole == UserRoles.DOCTOR) {
                                PrimaryBlue
                            } else {
                                TextDark
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                AuthTextField(
                    label = "Аты",
                    value = state.firstName,
                    placeholder = "Атыңыз",
                    onValueChange = viewModel::onFirstNameChange
                )

                AuthTextField(
                    label = "Тегі",
                    value = state.lastName,
                    placeholder = "Тегіңіз",
                    onValueChange = viewModel::onLastNameChange
                )

                AuthTextField(
                    label = "Телефон",
                    value = state.phone,
                    placeholder = "+7 777 123 45 67",
                    keyboardType = KeyboardType.Phone,
                    onValueChange = viewModel::onPhoneChange
                )

                AuthTextField(
                    label = "Электрондық пошта",
                    value = state.email,
                    placeholder = "example@gmail.com",
                    keyboardType = KeyboardType.Email,
                    onValueChange = viewModel::onEmailChange
                )

                if (state.requestedRole == UserRoles.DOCTOR) {
                    AuthTextField(
                        label = "Мамандық",
                        value = state.specialty,
                        placeholder = "Терапевт, хирург, ортодонт...",
                        onValueChange = viewModel::onSpecialtyChange
                    )

                    Text(
                        "Клиника",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = clinicMenuExpanded,
                        onExpandedChange = { clinicMenuExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedClinicName,
                            onValueChange = {},
                            readOnly = true,
                            placeholder = {
                                Text(
                                    if (state.isClinicsLoading) "Клиникалар жүктелуде..." else "Клиниканы таңдаңыз"
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = clinicMenuExpanded)
                            },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = authTextFieldColors()
                        )

                        DropdownMenu(
                            expanded = clinicMenuExpanded,
                            onDismissRequest = { clinicMenuExpanded = false }
                        ) {
                            state.clinics.forEach { clinic ->
                                DropdownMenuItem(
                                    text = { Text(clinic.name) },
                                    onClick = {
                                        viewModel.onClinicIdChange(clinic.id)
                                        clinicMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    "Құпия сөз",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(8.dp))

                AuthPasswordOutlinedField(
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    placeholder = { Text("Құпия сөзді енгізіңіз") }
                )

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "Аккаунт ашу арқылы сервис шарттары мен құпиялылық саясатына келісесіз",
                    fontSize = 12.sp,
                    color = LegalTextGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(22.dp))

                state.message?.let {
                    Text(
                        text = it,
                        color = Color(0xFF2E7D32),
                        fontSize = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.clearMessage() }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                state.error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 8.dp)
                    )
                }

                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = PrimaryBlue
                    )
                } else {
                    Button(
                        onClick = { viewModel.signUp() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 3.dp,
                            pressedElevation = 6.dp
                        ),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text(
                            if (state.requestedRole == UserRoles.DOCTOR) "Өтінім жіберу" else "Аккаунт ашу",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                if (state.doctorRequestSubmitted) {
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = {
                            viewModel.clearMessage()
                            onNavigateToLogin()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Кіру бетіне өту")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Аккаунтыңыз бар ма? ", color = TextGray, fontSize = 14.sp)
                    Text(
                        text = "Кіру",
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { onNavigateToLogin() }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthTextField(
    label: String,
    value: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    Text(
        label,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextDark
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        colors = authTextFieldColors()
    )

    Spacer(modifier = Modifier.height(18.dp))
}
