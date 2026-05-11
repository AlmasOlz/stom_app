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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stomatology.app.R
import com.example.stomatology.app.presentation.components.AppBackButton
import com.example.stomatology.app.presentation.theme.PrimaryBlue

private val RecoveryBackground = Color(0xFFF8F9FA)
private val RecoveryTitleColor = Color(0xFF2B3A67)
private val RecoveryPositiveColor = Color(0xFF4CAF50)
private val RecoveryWarningColor = Color(0xFFF44336)
private val RecoveryTomorrowColor = Color(0xFF2E7D32)

data class RecoveryStep(
    val instruction: String,
    val time: String?,
    val isToday: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoveryScreen(
    onBack: () -> Unit
) {
    var showSpecificInstructions by rememberSaveable { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.recovery_title),
                        fontWeight = FontWeight.Bold,
                        color = RecoveryTitleColor,
                        lineHeight = 24.sp
                    )
                },
                navigationIcon = {
                    AppBackButton(onClick = onBack)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RecoveryBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(RecoveryBackground)
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
                    text = stringResource(R.string.recovery_hero),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(
                    if (showSpecificInstructions) {
                        R.string.recovery_specific_title
                    } else {
                        R.string.recovery_general_title
                    }
                ),
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
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = stringResource(
                                if (showSpecificInstructions) {
                                    R.string.recovery_show_general
                                } else {
                                    R.string.recovery_show_specific
                                }
                            ),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = stringResource(
                                if (showSpecificInstructions) {
                                    R.string.recovery_general_subtitle
                                } else {
                                    R.string.recovery_specific_subtitle
                                }
                            ),
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
    val doItems = stringArrayResource(R.array.recovery_do_items).toList()
    val dontItems = stringArrayResource(R.array.recovery_dont_items).toList()

    Column {
        InstructionCard(
            title = stringResource(R.string.recovery_do_title),
            items = doItems,
            color = RecoveryPositiveColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        InstructionCard(
            title = stringResource(R.string.recovery_dont_title),
            items = dontItems,
            color = RecoveryWarningColor
        )
    }
}

@Composable
private fun InstructionCard(
    title: String,
    items: List<String>,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, color),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(color = color, shape = RoundedCornerShape(4.dp)) {
                Text(
                    text = title,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            items.forEach { item ->
                Text(
                    text = "- $item",
                    color = color
                )
            }
        }
    }
}

@Composable
fun SpecificInstructionsList() {
    val stepTexts = stringArrayResource(R.array.recovery_specific_steps)
    val todayTime = stringResource(R.string.recovery_specific_time_today)
    val tomorrowTime = stringResource(R.string.recovery_specific_time_tomorrow)
    val steps = stepTexts.mapIndexed { index, instruction ->
        RecoveryStep(
            instruction = instruction,
            time = when (index) {
                0 -> todayTime
                1 -> tomorrowTime
                else -> null
            },
            isToday = index == 0
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        steps.forEachIndexed { index, step ->
            RecoveryStepCard(
                number = index + 1,
                step = step
            )
        }
    }
}

@Composable
private fun RecoveryStepCard(
    number: Int,
    step: RecoveryStep
) {
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
                        text = stringResource(R.string.recovery_step_label, number),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = step.instruction,
                    color = Color.DarkGray,
                    fontSize = 14.sp
                )

                step.time?.let { time ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = time,
                        color = if (step.isToday) RecoveryWarningColor else RecoveryTomorrowColor,
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
