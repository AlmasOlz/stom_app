package com.example.stomatology.app.presentation.ai_analysis

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.stomatology.app.domain.model.Finding
import com.example.stomatology.app.domain.model.ToothDetectionState
import com.example.stomatology.app.presentation.theme.PrimaryBlue
import java.io.File
import java.io.FileOutputStream
import kotlin.math.cos
import kotlin.math.sin

// ─── Tooth Colour Palette ─────────────────────────────────────────────────────

val ColorUnknown = Color.White
val ColorHealthy = Color(0xFFF5F5F5)
val ColorMissing = Color.Black

val ColorAbscess = Color(0xFFE53935)
val ColorCaries = Color(0xFF4DB6AC)
val ColorFilling = Color(0xFFFFB74D)
val ColorImplant = Color(0xFF9575CD)
val ColorCrown = Color(0xFFFF5252)
val ColorOther = Color(0xFF64B5F6)

// ─── Wisdom teeth set ─────────────────────────────────────────────────────────

private val WISDOM_TEETH = setOf("RU8", "LU8", "LL8", "RL8")

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAnalysisScreen(
    onBack: () -> Unit,
    viewModel: AiAnalysisViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var selectedFinding by remember { mutableStateOf<Finding?>(null) }
    var selectedDisplayNumber by remember { mutableStateOf<Int?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            runCatching { uriToTempFile(context, it) }
                .onSuccess { file ->
                    selectedFinding = null
                    selectedDisplayNumber = null
                    viewModel.analyzeImage(file)
                }
                .onFailure {
                    selectedFinding = null
                    selectedDisplayNumber = null
                    viewModel.resetState()
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Стоматологическая карта",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            tint = Color.White,
                            contentDescription = "Back"
                        )
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
                .background(Color(0xFF29323C))
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = uiState) {
                is AiState.Idle -> {
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Рентген суретін таңдау", color = Color.White)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }

                is AiState.Loading -> {
                    Spacer(modifier = Modifier.weight(1f))
                    CircularProgressIndicator(color = PrimaryBlue)
                    Text(
                        "AI суретті талдауда...",
                        color = Color.White,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }

                is AiState.Success -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(PrimaryBlue)
                            .padding(vertical = 24.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                            Text(
                                text = "Тіс картасы",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.Start)
                                    .padding(start = 24.dp)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            JawLayout(
                                findings = state.result.findings,
                                onToothClick = { displayNumber, finding ->
                                    selectedDisplayNumber = displayNumber
                                    selectedFinding = finding
                                }
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            selectedDisplayNumber?.let { toothNumber ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Тіс №$toothNumber",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color.Black
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        val toothCode = selectedFinding?.toothClass ?: displayNumberToYoloClass(toothNumber)
                                        val toothStatus = getToothStatusText(
                                            displayNumber = toothNumber,
                                            finding = selectedFinding
                                        )

                                        Text(
                                            text = "Класс: $toothCode",
                                            color = Color.DarkGray,
                                            fontSize = 14.sp
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "Күйі: $toothStatus",
                                            color = Color.DarkGray,
                                            fontSize = 14.sp
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        val readableConditions = selectedFinding
                                            ?.conditions
                                            ?.map { conditionToReadableText(it) }
                                            ?.takeIf { it.isNotEmpty() }
                                            ?.joinToString(", ")
                                            ?: "Жоқ"

                                        Text(
                                            text = "Анықталғаны: $readableConditions",
                                            color = Color.DarkGray,
                                            fontSize = 14.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Барлығы ${state.result.teethCount} тіс анықталды",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.Black
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        LegendItem(ColorAbscess, "Абсцесс")
                                        LegendItem(ColorCaries, "Кариес")
                                        LegendItem(ColorCrown, "Коронка")
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        LegendItem(ColorFilling, "Пломба")
                                        LegendItem(ColorImplant, "Имплант")
                                        LegendItem(ColorMissing, "Жоқ")
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        LegendItem(ColorHealthy, "Сау", hasBorder = true)
                                        LegendItem(ColorUnknown, "Көрінбейді", hasBorder = true)
                                        LegendItem(ColorOther, "Басқа")
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Басқа суретті талдау", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                is AiState.Error -> {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = "Қате: ${state.message}", color = Color.Red)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { galleryLauncher.launch("image/*") }) {
                        Text("Қайта көру")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun uriToTempFile(context: Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)
        ?: throw IllegalArgumentException("Cannot open selected image")
    val tempFile = File.createTempFile("xray_", ".jpg", context.cacheDir)
    inputStream.use { input ->
        FileOutputStream(tempFile).use { output ->
            input.copyTo(output)
        }
    }
    return tempFile
}

// ─── Jaw Layout ───────────────────────────────────────────────────────────────

@Composable
fun JawLayout(
    findings: List<Finding>,
    onToothClick: (Int, Finding?) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
    ) {
        val centerX = maxWidth.value / 2f
        val centerY = maxHeight.value / 2f
        val radiusX = maxWidth.value * 0.35f
        val radiusY = maxHeight.value * 0.45f

        for (i in 0 until 16) {
            val angle = Math.PI - (i * (Math.PI / 15.0))
            val x = centerX + (radiusX * cos(angle))
            val y = centerY - (radiusY * sin(angle)) - 20f

            DrawTooth(
                x = x.dp,
                y = y.dp,
                displayNumber = i + 1,
                findings = findings,
                onClick = onToothClick
            )
        }

        for (i in 0 until 16) {
            val angle = Math.PI - (i * (Math.PI / 15.0))
            val x = centerX + (radiusX * cos(angle))
            val y = centerY + (radiusY * sin(angle)) + 20f

            DrawTooth(
                x = x.dp,
                y = y.dp,
                displayNumber = 32 - i,
                findings = findings,
                onClick = onToothClick
            )
        }
    }
}

/**
 * Display  1-16  → upper jaw, right-to-left  → RU8..RU1, LU1..LU8
 * Display 17-32  → lower jaw, right-to-left  → LL8..LL1, RL1..RL8
 */
fun mapYoloClassToDisplayNumber(yoloClass: String): Int {
    val clean = yoloClass.trim().uppercase()
    if (clean.length < 3) return -1

    val quadrant = clean.substring(0, 2)
    val toothIdx = clean.substring(2).toIntOrNull() ?: return -1

    return when (quadrant) {
        "RU" -> 9 - toothIdx
        "LU" -> 8 + toothIdx
        "LL" -> 25 - toothIdx
        "RL" -> 24 + toothIdx
        else -> -1
    }
}

fun displayNumberToYoloClass(displayNumber: Int): String {
    return when (displayNumber) {
        in 1..8 -> "RU${9 - displayNumber}"
        in 9..16 -> "LU${displayNumber - 8}"
        in 17..24 -> "LL${25 - displayNumber}"
        in 25..32 -> "RL${displayNumber - 24}"
        else -> "Белгісіз"
    }
}

fun isWisdomToothCode(toothCode: String): Boolean = toothCode in WISDOM_TEETH

fun isWisdomDisplayNumber(displayNumber: Int): Boolean {
    val toothCode = displayNumberToYoloClass(displayNumber)
    return isWisdomToothCode(toothCode)
}

// ─── Single Tooth ─────────────────────────────────────────────────────────────

@Composable
fun DrawTooth(
    x: Dp,
    y: Dp,
    displayNumber: Int,
    findings: List<Finding>,
    onClick: (Int, Finding?) -> Unit
) {
    val expectedToothCode = displayNumberToYoloClass(displayNumber)
    val finding = findings.find { it.toothClass == expectedToothCode }
    val toothColor = resolveToothColor(displayNumber, expectedToothCode, finding)

    Box(
        modifier = Modifier.offset(x = x - 12.dp, y = y - 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$displayNumber",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(8.dp, 8.dp, 16.dp, 16.dp))
                    .background(toothColor)
                    .clickable {
                        onClick(displayNumber, finding)
                    }
            )
        }
    }
}

/**
 * UI rule:
 * - If backend returns MISSING -> black
 * - If backend returns UNKNOWN -> white
 * - If backend returns DETECTED -> by conditions
 * - If tooth not found in findings at all:
 *      wisdom teeth -> white
 *      all others   -> black
 */
fun resolveToothColor(
    displayNumber: Int,
    toothCode: String,
    finding: Finding?
): Color {
    if (finding == null) {
        return ColorUnknown   // ← МАҢЫЗДЫ
    }

    return when (finding.state) {
        ToothDetectionState.MISSING -> ColorMissing
        ToothDetectionState.UNKNOWN -> {
            if (isWisdomDisplayNumber(displayNumber)) ColorUnknown else ColorMissing
        }

        ToothDetectionState.DETECTED -> {
            val conds = finding.conditions.map { it.lowercase() }
            when {
                conds.isEmpty() -> ColorHealthy
                conds.any { it.contains("abscess") } -> ColorAbscess
                conds.any { it.contains("caries") } -> ColorCaries
                conds.any { it.contains("filling") } -> ColorFilling
                conds.any { it.contains("implant") } -> ColorImplant
                conds.any { it.contains("crown") || it.contains("bridge") } -> ColorCrown
                else -> ColorOther
            }
        }
    }
}

fun getToothStatusText(
    displayNumber: Int,
    finding: Finding?
): String {
    val toothCode = finding?.toothClass ?: displayNumberToYoloClass(displayNumber)

    if (finding == null) {
        return if (isWisdomToothCode(toothCode)) "Көрінбейді" else "Жоқ"
    }

    return when (finding.state) {
        ToothDetectionState.MISSING -> "Жоқ"
        ToothDetectionState.UNKNOWN -> {
            if (isWisdomDisplayNumber(displayNumber)) "Көрінбейді" else "Жоқ"
        }

        ToothDetectionState.DETECTED -> "Табылды"
    }
}

// ─── Legend ───────────────────────────────────────────────────────────────────

@Composable
fun LegendItem(
    color: Color,
    text: String,
    hasBorder: Boolean = false
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (hasBorder) {
                        Modifier.background(
                            color = Color.LightGray,
                            shape = CircleShape
                        )
                    } else {
                        Modifier
                    }
                )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color.DarkGray,
            fontWeight = FontWeight.Medium
        )
    }
}

// ─── Condition text helper ────────────────────────────────────────────────────

fun conditionToReadableText(condition: String): String {
    return when (condition.lowercase()) {
        "abscess" -> "Абсцесс"
        "caries" -> "Кариес"
        "filling" -> "Пломба"
        "implant" -> "Имплант"
        "crown" -> "Коронка"
        "root_canal_treatment" -> "Түбір өзегі емделген"
        "post" -> "Штифт"
        "periapical_lesion" -> "Периапикальды өзгеріс"
        "residual_root" -> "Қалған түбір"
        "impacted_tooth" -> "Ретенцияланған тіс"
        "endocrown" -> "Эндокоронка"
        "veneer" -> "Винир"
        else -> condition
    }
}