package com.example.stomatology.app.presentation.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.stomatology.app.presentation.theme.PrimaryBlue
import com.example.stomatology.app.presentation.theme.TextDark
import com.example.stomatology.app.presentation.theme.TextGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()

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
                    .verticalScroll(scroll)
                    .padding(horizontal = 24.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Кіру",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Text(
                    text = "Аккаунтқа кіру",
                    fontSize = 15.sp,
                    color = TextGray,
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    "Электрондық пошта",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.email,
                    onValueChange = viewModel::onEmailChange,
                    placeholder = { Text("example@gmail.com", color = TextGray.copy(alpha = 0.85f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    colors = authTextFieldColors()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    "Құпия сөз",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
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
                                contentDescription = if (passwordVisible) {
                                    "Жасыру"
                                } else {
                                    "Көрсету"
                                },
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "Құпия сөзді ұмыттыңыз ба?",
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                state.error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }

                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 8.dp),
                        color = PrimaryBlue
                    )
                } else {
                    Button(
                        onClick = { viewModel.login() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Кіру", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Тіркелмегенсіз бе? ", color = TextGray, fontSize = 15.sp)
                    Text(
                        text = "Аккаунт ашу",
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.clickable { onNavigateToRegister() }
                    )
                }
            }
        }
    }
}

/** Толқын биіктігі — тақырып мәтіні көк үстінде қалмайды. */
val AuthHeaderWaveHeight: Dp = 108.dp

@Composable
fun TopWaveBackground(
    modifier: Modifier = Modifier,
    waveColor: Color = PrimaryBlue
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height * 0.52f)
            quadraticBezierTo(
                size.width * 0.68f,
                size.height * 1.02f,
                0f,
                size.height * 0.78f
            )
            close()
        }
        drawPath(path = path, color = waveColor)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun authTextFieldColors() = TextFieldDefaults.colors(
    unfocusedContainerColor = Color(0xFFF3F5F8),
    focusedContainerColor = Color(0xFFF3F5F8),
    unfocusedIndicatorColor = Color(0xFFC9D0D9),
    focusedIndicatorColor = PrimaryBlue,
    cursorColor = PrimaryBlue,
    focusedTextColor = TextDark,
    unfocusedTextColor = TextDark,
    focusedPlaceholderColor = TextGray.copy(alpha = 0.7f),
    unfocusedPlaceholderColor = TextGray.copy(alpha = 0.7f)
)
