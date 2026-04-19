package com.example.stomatology.app.presentation.recovery

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stomatology.app.presentation.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoveryScreen(
    onBack: () -> Unit
) {
    var showSpecificInstructions by rememberSaveable { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Скорейшего\nвыздоровления! 💪",
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
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryBlue, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = "Ознакомьтесь с\nпослеоперационными\nинструкциями\nздесь:",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
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

            Button(
                onClick = { showSpecificInstructions = !showSpecificInstructions },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "List",
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.height(0.dp).padding(horizontal = 8.dp))

                    Column {
                        Text(
                            text = if (showSpecificInstructions) "General Instructions" else "Specific Instructions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (showSpecificInstructions) {
                                "A comprehensive list of daily Do's and Dont's"
                            } else {
                                "Step-by-step instructions to help you"
                            },
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
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFF4CAF50)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Surface(color = Color(0xFF4CAF50), shape = RoundedCornerShape(4.dp)) {
                    Text(
                        text = "Do's",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("- Eat soft cold foods for at least 2 days.", color = Color(0xFF4CAF50))
                Text("- Avoid hot, spicy, hard foods.", color = Color(0xFF4CAF50))
                Text("- Consume tea, coffee at room temperature.", color = Color(0xFF4CAF50))
                Text("- Take medicines as prescribed by your doctor.", color = Color(0xFF4CAF50))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFF44336)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Surface(color = Color(0xFFF44336), shape = RoundedCornerShape(4.dp)) {
                    Text(
                        text = "Dont's",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp
                    )
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
        "After 24 hours, gargle in that area with lukewarm water and salt at least 3-4 times a day." to null
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
                        Surface(
                            color = PrimaryBlue,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Step ${index + 1}",
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = instruction,
                            color = Color.DarkGray,
                            fontSize = 14.sp
                        )

                        if (time != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = time,
                                color = if (time.contains("Today")) Color.Red else Color(0xFF2E7D32),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    RadioButton(
                        selected = false,
                        onClick = {}
                    )
                }
            }
        }
    }
}