package com.example.stomatology.app.presentation.ai_analysis

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.stomatology.app.presentation.theme.PrimaryBlue
import kotlin.math.cos
import kotlin.math.sin

val ColorHealthy = Color.White
val ColorCaries = Color(0xFF4DB6AC)
val ColorProsthesis = Color(0xFFFF5252)
val ColorMissing = Color(0xFF9E9E9E)
val ColorFilling = Color(0xFFFFB74D)
val ColorImplant = Color(0xFF9575CD)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAnalysisScreen(
    onBack: () -> Unit,
    viewModel: AiAnalysisViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.analyzeImage(context, it) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Стоматологическая карта", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, tint = Color.White, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFF29323C)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = uiState) {
                is AiState.Idle -> {
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) { Text("Рентген суретін таңдау", color = Color.White) }
                    Spacer(modifier = Modifier.weight(1f))
                }
                is AiState.Loading -> {
                    Spacer(modifier = Modifier.weight(1f))
                    CircularProgressIndicator(color = PrimaryBlue)
                    Text("AI суретті талдауда...", color = Color.White, modifier = Modifier.padding(top = 16.dp))
                    Spacer(modifier = Modifier.weight(1f))
                }
                is AiState.Success -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp).clip(RoundedCornerShape(24.dp))
                            .background(PrimaryBlue).padding(vertical = 24.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Your Dental Chart",
                                color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.Start).padding(start = 24.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            JawLayout(findings = state.result.findings)

                            Spacer(modifier = Modifier.height(32.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Барлығы ${state.result.teethCount} тіс табылды",
                                        fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                        LegendItem(ColorCaries, "Кариес")
                                        LegendItem(ColorProsthesis, "Коронка")
                                        LegendItem(ColorFilling, "Пломба")
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                        LegendItem(ColorImplant, "Имплант")
                                        LegendItem(ColorMissing, "Көрінбейді")
                                        LegendItem(ColorHealthy, "Сау")
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) { Text("Басқа суретті талдау", color = Color.White) }
                }
                is AiState.Error -> {
                    Spacer(modifier = Modifier.weight(1f))
                    Text("Қате: ${state.message}", color = Color.Red)
                    Button(onClick = { galleryLauncher.launch("image/*") }) { Text("Қайта көру") }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun JawLayout(findings: List<com.example.stomatology.app.domain.model.Finding>) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(400.dp)) {
        val centerX = maxWidth.value / 2
        val centerY = maxHeight.value / 2
        val radiusX = maxWidth.value * 0.35f
        val radiusY = maxHeight.value * 0.45f

        for (i in 0 until 16) {
            val angle = Math.PI - (i * (Math.PI / 15))
            val x = centerX + (radiusX * cos(angle))
            val y = centerY - (radiusY * sin(angle)) - 20f
            DrawTooth(x = x.dp, y = y.dp, number = i + 1, findings = findings)
        }

        for (i in 0 until 16) {
            val angle = Math.PI - (i * (Math.PI / 15))
            val x = centerX + (radiusX * cos(angle))
            val y = centerY + (radiusY * sin(angle)) + 20f
            DrawTooth(x = x.dp, y = y.dp, number = 32 - i, findings = findings)
        }
    }
}

// Пробелдер мен қателерден қорғалған функция
fun mapYoloClassToNumber(yoloClass: String): Int {
    val cleanClass = yoloClass.trim().uppercase()
    if (cleanClass.length < 3) return -1
    val quadrant = cleanClass.substring(0, 2)
    val toothIndex = cleanClass.substring(2).toIntOrNull() ?: return -1

    return when (quadrant) {
        "RU" -> 9 - toothIndex
        "LU" -> 8 + toothIndex
        "LL" -> 25 - toothIndex
        "RL" -> 24 + toothIndex
        else -> -1
    }
}

@Composable
fun DrawTooth(
    x: androidx.compose.ui.unit.Dp,
    y: androidx.compose.ui.unit.Dp,
    number: Int,
    findings: List<com.example.stomatology.app.domain.model.Finding>
) {
    val currentToothFinding = findings.find { finding ->
        mapYoloClassToNumber(finding.toothClass) == number
    }

    val toothColor = if (currentToothFinding == null) {
        ColorMissing // Тіс табылмаса сұр болады
    } else {
        // Барлық ауру атауларын кіші әріпке айналдырып тексереміз (мысалы "Crown-Bridge" -> "crown-bridge")
        val conditions = currentToothFinding.conditions.map { it.lowercase() }
        when {
            conditions.any { it.contains("caries") } -> ColorCaries
            conditions.any { it.contains("filling") } -> ColorFilling
            conditions.any { it.contains("implant") } -> ColorImplant
            conditions.any { it.contains("crown") || it.contains("bridge") } -> ColorProsthesis
            else -> ColorHealthy // Тіс табылған, бірақ ішінде ауру жоқ (АҚ ТҮС)
        }
    }

    Box(modifier = Modifier.offset(x = x - 12.dp, y = y - 12.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "$number", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(8.dp, 8.dp, 16.dp, 16.dp))
                    .background(toothColor)
            )
        }
    }
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, fontSize = 12.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
    }
}