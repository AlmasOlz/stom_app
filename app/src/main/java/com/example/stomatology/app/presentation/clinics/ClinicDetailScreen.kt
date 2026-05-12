package com.example.stomatology.app.presentation.clinics

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.stomatology.app.BuildConfig
import com.example.stomatology.app.R
import com.example.stomatology.app.domain.model.Clinic
import com.example.stomatology.app.presentation.components.AppBackButton
import com.example.stomatology.app.presentation.theme.PrimaryBlue
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ClinicDetailScreen(
    clinicId: String,
    serviceName: String,
    onBack: () -> Unit,
    onBookClick: (String, String) -> Unit,
    viewModel: ClinicViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val clinic = state.clinics.find { it.id == clinicId }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Клиника туралы", fontWeight = FontWeight.Bold) },
                navigationIcon = { AppBackButton(onClick = onBack) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            clinic?.let {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 12.dp,
                    color = Color.White
                ) {
                    Button(
                        onClick = { onBookClick(it.id, serviceName) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text(
                            "Қабылдауға жазылу",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }

            clinic == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Клиника табылмады")
                }
            }

            else -> {
                val hasLocation = hasClinicLocation(clinic)
                val canRenderMap = hasLocation && canRenderMapboxEmbeddedMap()
                val mapboxMapUrl = if (canRenderMap) {
                    remember(clinic.id, clinic.latitude, clinic.longitude) {
                        buildMapboxStaticMapUrl(clinic)
                    }
                } else {
                    null
                }

                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .background(Color(0xFFF1F3F5))
                    ) {
                        AsyncImage(
                            model = clinic.imageUrl.ifBlank {
                                "https://static.tildacdn.com/tild3032-6633-4366-a532-643335356139/6fgnff.png"
                            },
                            contentDescription = "Фото клиники ${clinic.name}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = clinic.name,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Стоматология орталығы", color = Color.Gray, fontSize = 14.sp)
                            }

                            Surface(
                                color = Color(0xFFFFF8E1),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFFA000),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${clinic.rating}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ClinicActionBtn(
                                icon = Icons.Default.Call,
                                label = "Қоңырау шалу",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    val opened = openDialer(context, clinic.phone)
                                    if (!opened) {
                                        scope.launch { snackbarHostState.showSnackbar("Қоңырау шалу мүмкін болмады") }
                                    }
                                }
                            )
                            ClinicActionBtn(
                                icon = Icons.Default.LocationOn,
                                label = "Картада",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    val opened = openClinicInMaps(context, clinic)
                                    if (!opened) {
                                        scope.launch { snackbarHostState.showSnackbar("Картаны ашу мүмкін болмады") }
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        InfoItem(Icons.Default.LocationOn, clinic.address)
                        Spacer(modifier = Modifier.height(16.dp))
                        InfoItem(
                            icon = Icons.Default.DateRange,
                            text = stringResource(id = R.string.clinic_working_hours_default)
                        )

                        if (hasLocation && canRenderMap && !mapboxMapUrl.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Text("Орналасуы", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.6f))
                            ) {
                                AsyncImage(
                                    model = mapboxMapUrl,
                                    contentDescription = "Картада ${clinic.name}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else if (hasLocation) {
                            Spacer(modifier = Modifier.height(20.dp))
                            MapUnavailableCard(
                                onOpenMap = {
                                    val opened = openClinicInMaps(context, clinic)
                                    if (!opened) {
                                        scope.launch { snackbarHostState.showSnackbar("Картаны ашу мүмкін болмады") }
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = PrimaryBlue.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Таңдалған қызмет", fontSize = 12.sp, color = Color.Gray)
                                    Text(serviceName, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                                }
                                Text(
                                    "${clinic.priceFrom} ₸",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text("Клиника туралы", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = clinic.description,
                            fontSize = 15.sp,
                            color = Color.DarkGray,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text("Барлық қызметтер", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            clinic.services.forEach { service ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF1F3F5),
                                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = service,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        fontSize = 14.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ClinicActionBtn(
    icon: ImageVector,
    label: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryBlue,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, color = Color.Black, fontSize = 14.sp)
    }
}

@Composable
fun InfoItem(
    icon: ImageVector,
    text: String,
    status: String? = null,
    statusColor: Color = Color(0xFF4CAF50)
) {
    val isWorkingHoursLine = remember(text) { parseWorkingHours(text) != null }
    val isOpenNow = remember(text) { isClinicOpenNow(text) }
    val resolvedStatus = if (isWorkingHoursLine) {
        if (isOpenNow) {
            stringResource(id = R.string.clinic_status_open)
        } else {
            stringResource(id = R.string.clinic_status_closed)
        }
    } else {
        status
    }
    val resolvedStatusColor = if (isWorkingHoursLine) {
        if (isOpenNow) Color(0xFF2E7D32) else Color(0xFFC62828)
    } else {
        statusColor
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(PrimaryBlue.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = PrimaryBlue)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text, fontSize = 15.sp, color = Color.Black)
            resolvedStatus?.let { value ->
                Text(value, fontSize = 13.sp, color = resolvedStatusColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MapUnavailableCard(onOpenMap: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.6f)),
        color = Color(0xFFF8F9FB)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Карта қолжетімсіз",
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Text(
                text = "Mapbox картасы уақытша ашылмады. Орналасуды сыртқы картадан ашыңыз.",
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            OutlinedButton(onClick = onOpenMap) {
                Text("Картада ашу")
            }
        }
    }
}

private fun isClinicOpenNow(workingHoursText: String, now: LocalDateTime = LocalDateTime.now()): Boolean {
    val schedule = parseWorkingHours(workingHoursText) ?: return false
    if (now.dayOfWeek !in schedule.workingDays) {
        return false
    }

    val currentTime = now.toLocalTime()
    return !currentTime.isBefore(schedule.openTime) && currentTime.isBefore(schedule.closeTime)
}

private data class ClinicSchedule(
    val workingDays: Set<DayOfWeek>,
    val openTime: LocalTime,
    val closeTime: LocalTime
)

private fun parseWorkingHours(raw: String): ClinicSchedule? {
    val pattern = Regex(
        pattern = "^\\s*([\\p{L}]{1,3})\\s*-\\s*([\\p{L}]{1,3})\\s*:\\s*(\\d{1,2}:\\d{2})\\s*-\\s*(\\d{1,2}:\\d{2})\\s*$",
        option = RegexOption.IGNORE_CASE
    )
    val match = pattern.find(raw) ?: return null

    val startDay = dayAbbreviationToDayOfWeek(match.groupValues[1]) ?: return null
    val endDay = dayAbbreviationToDayOfWeek(match.groupValues[2]) ?: return null
    val openTime = runCatching { LocalTime.parse(match.groupValues[3], DateTimeFormatter.ofPattern("H:mm")) }.getOrNull()
        ?: return null
    val closeTime = runCatching { LocalTime.parse(match.groupValues[4], DateTimeFormatter.ofPattern("H:mm")) }.getOrNull()
        ?: return null

    if (closeTime <= openTime) {
        return null
    }

    return ClinicSchedule(
        workingDays = expandDayRange(startDay, endDay),
        openTime = openTime,
        closeTime = closeTime
    )
}

private fun expandDayRange(start: DayOfWeek, end: DayOfWeek): Set<DayOfWeek> {
    val days = linkedSetOf<DayOfWeek>()
    var day = start
    while (true) {
        days += day
        if (day == end) break
        day = day.plus(1)
    }
    return days
}

private fun dayAbbreviationToDayOfWeek(value: String): DayOfWeek? {
    return when (value.lowercase(Locale.ROOT)) {
        "дс", "пн" -> DayOfWeek.MONDAY
        "сс", "вт" -> DayOfWeek.TUESDAY
        "ср" -> DayOfWeek.WEDNESDAY
        "бс", "чт" -> DayOfWeek.THURSDAY
        "жм", "пт" -> DayOfWeek.FRIDAY
        "сб" -> DayOfWeek.SATURDAY
        "жс", "вс" -> DayOfWeek.SUNDAY
        else -> null
    }
}

private fun canRenderMapboxEmbeddedMap(): Boolean {
    return BuildConfig.MAPBOX_ACCESS_TOKEN.isNotBlank()
}

private fun hasClinicLocation(clinic: Clinic): Boolean {
    return clinic.latitude in -90.0..90.0 &&
        clinic.longitude in -180.0..180.0 &&
        (clinic.latitude != 0.0 || clinic.longitude != 0.0)
}

private fun buildMapboxStaticMapUrl(clinic: Clinic): String {
    val token = BuildConfig.MAPBOX_ACCESS_TOKEN
    if (token.isBlank()) return ""

    val latitude = clinic.latitude
    val longitude = clinic.longitude
    val marker = "pin-s+00AEEF($longitude,$latitude)"
    val camera = "$longitude,$latitude,15,0"

    return "https://api.mapbox.com/styles/v1/mapbox/streets-v12/static/$marker/$camera/1200x400?access_token=$token"
}

private fun openClinicInMaps(context: Context, clinic: Clinic): Boolean {
    val query = if (hasClinicLocation(clinic)) {
        "${clinic.latitude},${clinic.longitude}(${clinic.name})"
    } else {
        clinic.address.ifBlank { clinic.name }
    }
    if (query.isBlank()) return false
    val geoUri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")

    val googleMapsIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
        setPackage("com.google.android.apps.maps")
    }

    val fallbackIntent = Intent(Intent.ACTION_VIEW, geoUri)

    return runCatching {
        context.startActivity(googleMapsIntent)
        true
    }.onFailure {
        runCatching { context.startActivity(fallbackIntent) }.getOrElse { return false }
    }.getOrDefault(true)
}

private fun openDialer(context: Context, phone: String): Boolean {
    val value = phone.trim()
    if (value.isBlank()) return false
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(value)}"))
    return runCatching {
        context.startActivity(intent)
        true
    }.getOrDefault(false)
}
