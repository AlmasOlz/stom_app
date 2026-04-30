package com.example.stomatology.app.presentation.education

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@Composable
fun InstructionsScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    val instructionSteps = stringArrayResource(R.array.recovery_specific_steps).toList()
    val timedSteps = listOf(
        stringResource(R.string.recovery_specific_time_today),
        stringResource(R.string.recovery_specific_time_tomorrow)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Шапка (Скорейшего выздоровления!)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AppBackButton(onClick = onBack)
                Text(
                    text = stringResource(R.string.recovery_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp
                )
                // Профиль суретінің орны
                Box(modifier = Modifier.size(45.dp).background(Color.LightGray, CircleShape))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Көгілдір баннер
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = PrimaryBlue.copy(alpha = 0.7f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.recovery_hero),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(text = stringResource(R.string.recovery_specific_title), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))

            // 3. Қадамдар тізімі (Instructions)
            instructionSteps.forEachIndexed { index, instruction ->
                InstructionStep(
                    step = stringResource(R.string.recovery_step_label, index + 1),
                    desc = instruction,
                    time = timedSteps.getOrNull(index),
                    timeColor = if (index == 0) Color.Red else Color(0xFF4CAF50)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. General Instructions батырмасы
            Button(
                onClick = { /* Жалпы нұсқаулықтарға өту */ },
                modifier = Modifier.fillMaxWidth().height(80.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue.copy(alpha = 0.8f))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.List, null, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(stringResource(R.string.recovery_general_title), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(stringResource(R.string.recovery_general_subtitle), fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun InstructionStep(step: String, desc: String, time: String?, timeColor: Color = Color.Gray) {
    var isChecked by remember { mutableStateOf(false) }

    Box(modifier = Modifier.padding(bottom = 16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = desc, fontSize = 14.sp, color = Color.DarkGray)
                    if (time != null) {
                        Text(text = time, fontSize = 12.sp, color = timeColor, fontWeight = FontWeight.Bold)
                    }
                }
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = { isChecked = it },
                    colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue)
                )
            }
        }
        // "Step X" белгісі
        Surface(
            color = PrimaryBlue.copy(alpha = 0.9f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(start = 12.dp)
        ) {
            Text(
                text = step,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}
