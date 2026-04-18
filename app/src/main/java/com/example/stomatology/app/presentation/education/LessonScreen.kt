package com.example.stomatology.app.presentation.education

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stomatology.app.presentation.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Урок по чистке зубов", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(8.dp)
                            .background(Color.DarkGray.copy(alpha = 0.5f), shape = RoundedCornerShape(50))
                            .size(32.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(scrollState)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Начнём...",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Начните с чистки внешней стороны зубов, то есть передней поверхности, которая видна при улыбке. Не забудьте удалить зубной налет с той части зуба, которая находится ближе к линии десны.",
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Video Player Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.LightGray)
                ) {
                    // Placeholder for the actual braces video/image
                    // Image(painter = painterResource(id = R.drawable.your_braces_image), ...)

                    // Simulated Media Controls at the bottom of the video player
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(PrimaryBlue)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("⏸ 0:08", color = Color.White, fontSize = 12.sp)
                            Slider(
                                value = 0.3f,
                                onValueChange = {},
                                modifier = Modifier.weight(1f).padding(horizontal = 8.dp).height(10.dp),
                                colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White)
                            )
                            Text("0:00 ⚙ [ ]", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // Push buttons to bottom if screen is tall

            // Navigation Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81D4FA)), // Light blue
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.height(48.dp).weight(1f)
                ) {
                    Text("◀◀ Назад", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = { /* Navigate to next lesson */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81D4FA)), // Light blue
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.height(48.dp).weight(1f)
                ) {
                    Text("Вперед ▶▶", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}