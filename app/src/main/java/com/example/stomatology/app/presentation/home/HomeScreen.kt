package com.example.stomatology.app.presentation.home

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
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.stomatology.app.R
import com.example.stomatology.app.presentation.profile.UserProfileViewModel
import com.example.stomatology.app.presentation.theme.BackgroundGray
import com.example.stomatology.app.presentation.theme.PrimaryBlue
import androidx.annotation.DrawableRes

@Composable
fun HomeScreen(
    onNavigateToClinics: (String) -> Unit,
    onNavigateToAi: () -> Unit,
    onNavigateToOtherServices: () -> Unit,
    profileViewModel: UserProfileViewModel = hiltViewModel()
) {
    val profileState by profileViewModel.uiState.collectAsState()

    val userName = profileState.user.firstName
        .ifBlank { profileState.user.displayName }
        .ifBlank { "Пайдаланушы" }

    if (profileState.isLoading) {
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
        onNavigateToOtherServices = onNavigateToOtherServices
    )
}

@Composable
private fun HomeContent(
    userName: String,
    photoUrl: String,
    onNavigateToClinics: (String) -> Unit,
    onNavigateToAi: () -> Unit,
    onNavigateToOtherServices: () -> Unit
) {
    val scrollState = rememberScrollState()

    val services = remember {
        listOf(
            ServiceItem("Тіс жұлу", ServiceIcon.Drawable(R.drawable.ic_tooth_extract)) { onNavigateToClinics("Тіс жұлу") },
            ServiceItem("Протездеу", ServiceIcon.Drawable(R.drawable.ic_prosthesis)) { onNavigateToClinics("Протездеу") },
            ServiceItem("Пломба / Канал", ServiceIcon.Drawable(R.drawable.ic_root_canal)) { onNavigateToClinics("Пломба / Канал") },
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

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(PrimaryBlue)
                .padding(24.dp)
        ) {
            Text(
                text = "Өзіңізге керек қызметті таңдаңыз.",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

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
