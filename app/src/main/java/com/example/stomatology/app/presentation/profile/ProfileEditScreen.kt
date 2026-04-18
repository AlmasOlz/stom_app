package com.example.stomatology.app.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stomatology.app.presentation.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()

    var fullName by remember { mutableStateOf("Мадиханова Алтынай") }
    var nickname by remember { mutableStateOf("maddi") }
    var email by remember { mutableStateOf("youremail@domain.com") }
    var phone by remember { mutableStateOf("123-456-7890") }
    var country by remember { mutableStateOf("United States") }
    var gender by remember { mutableStateOf("Женский") }
    var address by remember { mutableStateOf("45 New Avenue, New York") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редактировать профиль", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProfileTextField("Full name", fullName) { fullName = it }
            ProfileTextField("Nick name", nickname) { nickname = it }
            ProfileTextField("Email", email) { email = it }

            // Phone with simulated flag
            ProfileTextField("Phone number", phone, leadingIcon = "🇺🇸 ") { phone = it }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    ProfileTextField("Страна", country) { country = it }
                }
                Box(modifier = Modifier.weight(1f)) {
                    ProfileTextField("Пол", gender) { gender = it }
                }
            }

            ProfileTextField("Address", address) { address = it }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { /* TODO: Save Profile to Backend */ onBack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("СОХРАНИТЬ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTextField(
    label: String,
    value: String,
    leadingIcon: String? = null,
    onValueChange: (String) -> Unit
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.Gray, fontSize = 12.sp) },
        leadingIcon = leadingIcon?.let { { Text(it, fontSize = 20.sp, modifier = Modifier.padding(start = 8.dp)) } },
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = Color(0xFFE8F4F8), // Light bluish-gray matching mockup
            focusedContainerColor = Color(0xFFE8F4F8),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}