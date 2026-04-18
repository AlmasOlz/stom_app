package com.example.stomatology.app.presentation.clinics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stomatology.app.presentation.theme.PrimaryBlue

@Composable
fun PromoDetailScreen(
    onBack: () -> Unit,
    onBookClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Top Image Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(Color(0xFFE0E0E0)) // Image Placeholder
            ) {
                // Back Button Overlay
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(top = 40.dp, start = 16.dp)
                        .background(Color.Black.copy(alpha = 0.3f), shape = RoundedCornerShape(50))
                        .size(40.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }

            // Content Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .offset(y = (-30).dp) // Pulling it up over the image slightly
            ) {
                // Blue Promo Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(PrimaryBlue)
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Здоровые улыбки без лишних тревог.\nС заботой о самых маленьких героях.",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Text Description
                Text(
                    text = "Севоран — «золотой стандарт» детской стоматологии. Мы используем оригинальный препарат Севоран, который не вызывает привыкания и полностью выводится из организма сразу после процедуры.\n\nЭто позволяет нам лечить даже самые сложные случаи за один визит, пока ваш герой видит приятные сны.",
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Book Button
                Button(
                    onClick = onBookClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(horizontal = 40.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Записаться", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(80.dp)) // Padding for the FAB
            }
        }

        // Floating Action Button (as seen in mockup)
        FloatingActionButton(
            onClick = { /* TODO: Open quick actions */ },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .size(64.dp),
            containerColor = PrimaryBlue,
            contentColor = Color.White,
            shape = RoundedCornerShape(50)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(32.dp))
        }
    }
}