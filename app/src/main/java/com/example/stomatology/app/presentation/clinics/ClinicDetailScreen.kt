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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.stomatology.app.domain.model.Clinic
import com.example.stomatology.app.presentation.components.AppBackButton
import com.example.stomatology.app.presentation.theme.PrimaryBlue
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

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

    Scaffold(
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
                val markerPosition = if (hasLocation) {
                    remember(clinic.id, clinic.latitude, clinic.longitude) {
                        LatLng(clinic.latitude, clinic.longitude)
                    }
                } else {
                    null
                }
                val cameraPositionState = markerPosition?.let { latLng ->
                    rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(latLng, 15f)
                    }
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
                                onClick = {}
                            )
                            ClinicActionBtn(
                                icon = Icons.Default.LocationOn,
                                label = "Картада",
                                modifier = Modifier.weight(1f),
                                onClick = { openClinicInMaps(context, clinic) }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        InfoItem(Icons.Default.LocationOn, clinic.address)
                        Spacer(modifier = Modifier.height(16.dp))
                        InfoItem(Icons.Default.DateRange, "Дс-Сб: 09:00 - 20:00", status = "Ашық")

                        if (hasLocation && markerPosition != null && cameraPositionState != null) {
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
                                GoogleMap(
                                    modifier = Modifier.fillMaxSize(),
                                    cameraPositionState = cameraPositionState
                                ) {
                                    Marker(
                                        state = MarkerState(position = markerPosition),
                                        title = clinic.name,
                                        snippet = clinic.address
                                    )
                                }
                            }
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
fun InfoItem(icon: ImageVector, text: String, status: String? = null) {
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
            if (status != null) {
                Text(status, fontSize = 13.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun hasClinicLocation(clinic: Clinic): Boolean {
    return clinic.latitude in -90.0..90.0 &&
        clinic.longitude in -180.0..180.0 &&
        (clinic.latitude != 0.0 || clinic.longitude != 0.0)
}

private fun openClinicInMaps(context: Context, clinic: Clinic) {
    val query = if (hasClinicLocation(clinic)) {
        "${clinic.latitude},${clinic.longitude}(${clinic.name})"
    } else {
        clinic.address.ifBlank { clinic.name }
    }
    val geoUri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")

    val googleMapsIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
        setPackage("com.google.android.apps.maps")
    }

    val fallbackIntent = Intent(Intent.ACTION_VIEW, geoUri)

    runCatching {
        context.startActivity(googleMapsIntent)
    }.onFailure {
        context.startActivity(fallbackIntent)
    }
}
