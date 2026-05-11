package com.example.stomatology.app.presentation.auth

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.stomatology.app.core.firebase.UserRoles
import com.example.stomatology.app.presentation.theme.PrimaryBlue
import com.example.stomatology.app.presentation.theme.TextDark
import com.example.stomatology.app.presentation.theme.TextGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    var clinicMenuExpanded by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.requestedRole) {
        if (state.requestedRole == UserRoles.DOCTOR) {
            viewModel.loadClinicsIfNeeded()
        }
    }

    val selectedClinicName = state.clinics
        .firstOrNull { clinic -> clinic.id == state.clinicId }
        ?.name
        .orEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AuthHeaderWaveHeight)
            ) {
                TopWaveBackground(Modifier.fillMaxSize())
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Тіркелу",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Text(
                    text = if (state.requestedRole == UserRoles.DOCTOR) {
                        "Дәрігер аккаунтына өтінім"
                    } else {
                        "Пациент аккаунтын ашу"
                    },
                    fontSize = 15.sp,
                    color = TextGray,
                    modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.onRequestedRoleChange(UserRoles.PATIENT) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (state.requestedRole == UserRoles.PATIENT) {
                                PrimaryBlue.copy(alpha = 0.12f)
                            } else {
                                Color(0xFFF3F5F8)
                            }
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (state.requestedRole == UserRoles.PATIENT) {
                                PrimaryBlue
                            } else {
                                Color(0xFFC9D0D9)
                            }
                        )
                    ) {
                        Text(
                            text = "Пациент",
                            fontWeight = FontWeight.SemiBold,
                            color = if (state.requestedRole == UserRoles.PATIENT) PrimaryBlue else TextGray
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.onRequestedRoleChange(UserRoles.DOCTOR) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (state.requestedRole == UserRoles.DOCTOR) {
                                PrimaryBlue.copy(alpha = 0.12f)
                            } else {
                                Color(0xFFF3F5F8)
                            }
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (state.requestedRole == UserRoles.DOCTOR) {
                                PrimaryBlue
                            } else {
                                Color(0xFFC9D0D9)
                            }
                        )
                    ) {
                        Text(
                            text = "Дәрігер",
                            fontWeight = FontWeight.SemiBold,
                            color = if (state.requestedRole == UserRoles.DOCTOR) PrimaryBlue else TextGray
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

                Text("Клиника", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
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
                                if (state.isClinicsLoading) "Клиникалар жүктелуде..." else "Клиниканы таңдаңыз",
                                color = TextGray.copy(alpha = 0.85f)
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

            Text("Құпия сөз", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                placeholder = { Text("Құпия сөзді енгізіңіз", color = TextGray.copy(alpha = 0.85f)) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) {
                                Icons.Outlined.Visibility
                            } else {
                                Icons.Outlined.VisibilityOff
                            },
                            contentDescription = if (passwordVisible) "Жасыру" else "Көрсету",
                            tint = TextGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                colors = authTextFieldColors()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Аккаунт ашу арқылы сервис шарттары мен құпиялылық саясатына келісесіз",
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = TextGray,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

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
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
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
                Text("Аккаунтыңыз бар ма? ", color = TextGray, fontSize = 15.sp)
                Text(
                    text = "Кіру",
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
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
    Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextGray.copy(alpha = 0.85f)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        colors = authTextFieldColors()
    )

    Spacer(modifier = Modifier.height(18.dp))
}
