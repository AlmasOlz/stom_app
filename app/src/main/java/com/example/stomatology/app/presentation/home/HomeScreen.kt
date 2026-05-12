package com.example.stomatology.app.presentation.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.stomatology.app.R
import com.example.stomatology.app.domain.model.Clinic
import com.example.stomatology.app.presentation.profile.UserProfileViewModel
import com.example.stomatology.app.presentation.theme.BackgroundGray
import com.example.stomatology.app.presentation.theme.PrimaryBlue
import androidx.annotation.DrawableRes
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

@Composable
fun HomeScreen(
    onNavigateToClinics: (String) -> Unit,
    onNavigateToAi: () -> Unit,
    onNavigateToOtherServices: () -> Unit,
    onQuickRebook: (String, String) -> Unit,
    onOpenClinic: (String, String) -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel(),
    profileViewModel: UserProfileViewModel = hiltViewModel()
) {
    val profileState by profileViewModel.uiState.collectAsState()
    val homeState by homeViewModel.uiState.collectAsState()
    val context = LocalContext.current

    val userName = profileState.user.firstName
        .ifBlank { profileState.user.displayName }
        .ifBlank { "Пайдаланушы" }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val hasPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (hasPermission) {
            context.fetchCurrentLocation { latLng ->
                homeViewModel.onLocationUpdated(latLng.latitude, latLng.longitude)
            }
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    if (profileState.isLoading || homeState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGray),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PrimaryBlue)
        }
        return
    }

    HomeContent(
        userName = userName,
        photoUrl = profileState.user.photoUrl,
        onNavigateToClinics = onNavigateToClinics,
        onNavigateToAi = onNavigateToAi,
        onNavigateToOtherServices = onNavigateToOtherServices,
        onQuickRebook = onQuickRebook,
        onOpenClinic = onOpenClinic,
        uiState = homeState
    )
}

@Composable
private fun HomeContent(
    userName: String,
    photoUrl: String,
    onNavigateToClinics: (String) -> Unit,
    onNavigateToAi: () -> Unit,
    onNavigateToOtherServices: () -> Unit,
    onQuickRebook: (String, String) -> Unit,
    onOpenClinic: (String, String) -> Unit,
    uiState: HomeUiState
) {
    val scrollState = rememberScrollState()

    val services = remember {
        listOf(
            ServiceItem("Тіс жұлу", ServiceIcon.Drawable(R.drawable.ic_tooth_extract)) { onNavigateToClinics("Тіс жұлу") },
            ServiceItem("Протездеу", ServiceIcon.Drawable(R.drawable.ic_prosthesis)) { onNavigateToClinics("Протездеу") },
            ServiceItem("Пломба / Канал емі", ServiceIcon.Drawable(R.drawable.ic_root_canal)) { onNavigateToClinics("Пломба / Канал") },
            ServiceItem("Имплант", ServiceIcon.Drawable(R.drawable.ic_implant)) { onNavigateToClinics("Имплант") },
            ServiceItem("AI талдау", ServiceIcon.Vector(Icons.Default.AutoAwesome)) { onNavigateToAi() },
            ServiceItem("Брекет", ServiceIcon.Drawable(R.drawable.ic_braces)) { onNavigateToClinics("Брекет") }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .verticalScroll(scrollState)
            .padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Сәлем, $userName",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            HomeAvatar(
                userName = userName,
                photoUrl = photoUrl
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        QuickRebookHeroCard(
            quickRebook = uiState.quickRebook,
            onQuickRebook = onQuickRebook
        )

        Spacer(modifier = Modifier.height(20.dp))

        NearbyClinicsSection(
            clinics = uiState.nearbyClinics,
            onOpenClinic = onOpenClinic
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Қызметтер",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(modifier = Modifier.padding(horizontal = 8.dp)) {
            services.chunked(3).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { service ->
                        ServiceCard(
                            service = service,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    repeat(3 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { onNavigateToOtherServices() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Іздеу",
                    tint = PrimaryBlue
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Басқа қызметтерді іздеу",
                    fontSize = 16.sp,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun QuickRebookHeroCard(
    quickRebook: com.example.stomatology.app.domain.model.Appointment?,
    onQuickRebook: (String, String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryBlue)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Жылдам қайта жазылу",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            if (quickRebook == null) {
                Text("Алдыңғы жазба табылмады", color = Color(0xFFEAF2FF), fontSize = 13.sp)
            } else {
                Text(
                    "Соңғы жазба: ${quickRebook.doctorName} • ${quickRebook.service}",
                    color = Color(0xFFEAF2FF),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { onQuickRebook(quickRebook.clinicId, quickRebook.service) },
                    border = BorderStroke(1.dp, Color.White),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = PrimaryBlue
                    )
                ) {
                    Text(
                        text = "Қайта жазылу",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun NearbyClinicsSection(
    clinics: List<Clinic>,
    onOpenClinic: (String, String) -> Unit
) {
    Text(
        text = "Жақын маңдағы клиникалар",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
    Spacer(modifier = Modifier.height(10.dp))

    if (clinics.isEmpty()) {
        Text(
            text = "Клиникалар әлі жүктелмеген немесе геолокацияға рұқсат берілмеген",
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        return
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        items(clinics.take(8)) { clinic ->
            Card(
                modifier = Modifier
                    .width(220.dp)
                    .clickable { onOpenClinic(clinic.id, clinic.services.firstOrNull().orEmpty()) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(clinic.name, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(clinic.address, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
                    Text("${clinic.rating} ★", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun Context.fetchCurrentLocation(onSuccess: (LatLng) -> Unit) {
    val hasFine = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val hasCoarse = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    if (!hasFine && !hasCoarse) return

    val client = LocationServices.getFusedLocationProviderClient(this)
    client.lastLocation
        .addOnSuccessListener { last: Location? ->
            if (last != null) {
                onSuccess(LatLng(last.latitude, last.longitude))
            } else {
                val cts = CancellationTokenSource()
                client.getCurrentLocation(
                    if (hasFine) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    cts.token
                ).addOnSuccessListener { current: Location? ->
                    if (current != null) {
                        onSuccess(LatLng(current.latitude, current.longitude))
                    } else {
                        fetchLastKnownLocationFallback()?.let(onSuccess)
                    }
                }.addOnFailureListener {
                    fetchLastKnownLocationFallback()?.let(onSuccess)
                }
            }
        }
        .addOnFailureListener {
            fetchLastKnownLocationFallback()?.let(onSuccess)
        }
}

@SuppressLint("MissingPermission")
private fun Context.fetchLastKnownLocationFallback(): LatLng? {
    val locationManager = getSystemService(LocationManager::class.java) ?: return null
    val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
    val lastKnown: Location = providers
        .mapNotNull { provider -> runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull() }
        .maxByOrNull { it.time }
        ?: return null
    return LatLng(lastKnown.latitude, lastKnown.longitude)
}

@Composable
private fun HomeAvatar(
    userName: String,
    photoUrl: String
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        if (photoUrl.isNotBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "User avatar",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = userName.firstOrNull()?.uppercase() ?: "U",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

data class ServiceItem(
    val title: String,
    val icon: ServiceIcon,
    val onClick: () -> Unit
)

sealed interface ServiceIcon {
    data class Vector(val imageVector: ImageVector) : ServiceIcon
    data class Drawable(@DrawableRes val resId: Int) : ServiceIcon
}

@Composable
fun ServiceCard(
    service: ServiceItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(0.95f)
            .clickable { service.onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            when (val icon = service.icon) {
                is ServiceIcon.Vector -> Icon(
                    imageVector = icon.imageVector,
                    contentDescription = service.title,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(42.dp)
                )

                is ServiceIcon.Drawable -> Image(
                    painter = painterResource(id = icon.resId),
                    contentDescription = service.title,
                    modifier = Modifier.size(42.dp)
                )
            }

            Text(
                text = service.title,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                color = Color.Black,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
