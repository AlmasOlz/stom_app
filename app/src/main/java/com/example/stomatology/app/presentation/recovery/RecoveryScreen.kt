package com.example.stomatology.app.presentation.recovery

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stomatology.app.presentation.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoveryScreen(onBack: () -> Unit) {
    var showSpecificInstructions by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Скорейшего\nвыздоровления! 💪",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2B3A67),
                        lineHeight = 24.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(8.dp)
                            .background(Color.DarkGray, shape = RoundedCornerShape(50))
                            .size(32.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F9FA))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Hero Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PrimaryBlue)
                    .padding(20.dp)
            ) {
                Text(
                    text = "Ознакомьтесь с\nпослеоперационн\nыми инструкциями\nздесь:",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                // Note: Add Doctor Image placeholder alignment here in production
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (showSpecificInstructions) "Specific Instructions:" else "General Instructions:",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (showSpecificInstructions) {
                SpecificInstructionsList()
            } else {
                GeneralInstructionsList()
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Toggle Button at bottom
            Button(
                onClick = { showSpecificInstructions = !showSpecificInstructions },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.List, contentDescription = "List", modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if (showSpecificInstructions) "General Instructions" else "Specific Instructions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = if (showSpecificInstructions) "A comprehensive list of daily Do's and Dont's" else "Step-by-step instructions to help you",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GeneralInstructionsList() {
    Column {
        // Do's
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFF4CAF50)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Surface(color = Color(0xFF4CAF50), shape = RoundedCornerShape(4.dp)) {
                    Text("Do's", color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("- Eat soft cold foods for atleast 2 days.", color = Color(0xFF4CAF50))
                Text("- Avoid hot, spicy, hard foods.", color = Color(0xFF4CAF50))
                Text("- Consume tea, coffee at room temperature.", color = Color(0xFF4CAF50))
                Text("- Take medicines as prescribed by your doctor.", color = Color(0xFF4CAF50))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dont's
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFF44336)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Surface(color = Color(0xFFF44336), shape = RoundedCornerShape(4.dp)) {
                    Text("Dont's", color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("- Do not smoke/drink alcohol for 48 hours post extraction.", color = Color(0xFFF44336))
                Text("- Do not spit outside for 2 days and do not use straw for first 24 hours.", color = Color(0xFFF44336))
            }
        }
    }
}

@Composable
fun SpecificInstructionsList() {
    val steps = listOf(
        "Bite firmly on the gauze placed in your mouth for at least 45-60 minutes and then gently remove the pack." to "Today 8:00 AM",
        "After going home apply ice pack on the area in 15-20 minute intervals till nighttime." to "Tomorrow 9:00 AM",
        "After removing the pack take one dosage of medicines prescribed." to null,
        "After 24 hours, gargle in that area with luke warm water and salt atleast 3-4 times a day." to null
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        steps.forEachIndexed { index, (instruction, time) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, PrimaryBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(color = PrimaryBlue, shape = RoundedCornerShape(12.dp)) {
                            Text("Step ${index + 1}", color = Color.White, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(instruction, color = Color.DarkGray, fontSize = 14.sp)
                        if (time != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(time, color = if (time.contains("Today")) Color.Red else Color.Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    RadioButton(selected = false, onClick = { /* TODO */ })
                }
            }
        }
    }
}