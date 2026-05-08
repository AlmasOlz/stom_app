package com.example.stomatology.app.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.stomatology.app.R
import com.example.stomatology.app.core.firebase.UserRoles
import com.example.stomatology.app.domain.model.UserProfile
import com.example.stomatology.app.presentation.theme.PrimaryBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onNotifications: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PrimaryBlue)
        }
        return
    }

    val user = state.user
    val displayName = user.displayName
        .ifBlank { "${user.firstName} ${user.lastName}".trim() }
        .ifBlank { stringResource(R.string.profile_name_fallback) }
    val email = user.email.ifBlank { stringResource(R.string.profile_email_missing) }
    val phone = user.phone.ifBlank { stringResource(R.string.profile_phone_missing) }
    val roleLabel = profileRoleLabel(user.role)
    val registeredAt = formatProfileDate(user.createdAt)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFF8F9FA))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    color = PrimaryBlue,
                    shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = stringResource(R.string.profile_notifications),
                    tint = Color.White,
                    modifier = Modifier.clickable { onNotifications() }
                )

                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.profile_edit),
                    tint = Color.White,
                    modifier = Modifier.clickable { onEditProfile() }
                )
            }

            ProfileAvatar(
                user = user,
                displayName = displayName,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 52.dp)
                    .size(104.dp),
                onClick = onEditProfile
            )
        }

        Spacer(modifier = Modifier.height(66.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = displayName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                color = PrimaryBlue.copy(alpha = 0.12f),
                shape = RoundedCornerShape(999.dp)
            ) {
                Text(
                    text = roleLabel,
                    color = PrimaryBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = email,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Text(
                text = phone,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        state.error?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                color = Color(0xFFD32F2F),
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        ProfileInfoCard(
            email = email,
            phone = phone,
            registeredAt = registeredAt
        )

        if (user.role == UserRoles.DOCTOR) {
            Spacer(modifier = Modifier.height(12.dp))
            DoctorInfoCard(
                specialty = user.specialty,
                experienceYears = user.experienceYears,
                aboutDoctor = user.aboutDoctor
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                ProfileMenuItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null
                        )
                    },
                    title = stringResource(R.string.profile_edit),
                    subtitle = stringResource(R.string.profile_edit_subtitle),
                    onClick = onEditProfile
                )

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                ProfileMenuItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null
                        )
                    },
                    title = stringResource(R.string.profile_notifications),
                    subtitle = stringResource(R.string.profile_notifications_subtitle),
                    onClick = onNotifications
                )

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                ProfileMenuItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null
                        )
                    },
                    title = stringResource(R.string.profile_settings),
                    subtitle = stringResource(R.string.profile_settings_subtitle),
                    onClick = onOpenSettings
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun ProfileAvatar(
    user: UserProfile,
    displayName: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(modifier = modifier.clickable { onClick() }) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(Color(0xFFE1F5FE)),
            contentAlignment = Alignment.Center
        ) {
            if (user.photoUrl.isNotBlank()) {
                AsyncImage(
                    model = user.photoUrl,
                    contentDescription = stringResource(R.string.profile_photo),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = profileInitials(displayName),
                    color = PrimaryBlue,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(34.dp)
                .clip(CircleShape)
                .background(Color.White)
                .padding(5.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = stringResource(R.string.profile_change_photo),
                tint = PrimaryBlue,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ProfileInfoCard(
    email: String,
    phone: String,
    registeredAt: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.profile_personal_info),
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileInfoRow(label = stringResource(R.string.profile_email), value = email)
            ProfileInfoRow(label = stringResource(R.string.profile_phone), value = phone)
            ProfileInfoRow(
                label = stringResource(R.string.profile_registered_at),
                value = registeredAt
            )
        }
    }
}

@Composable
private fun DoctorInfoCard(
    specialty: String,
    experienceYears: Int,
    aboutDoctor: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.profile_doctor_profile),
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileInfoRow(
                label = stringResource(R.string.profile_specialty),
                value = specialty.ifBlank { stringResource(R.string.profile_not_specified) }
            )
            ProfileInfoRow(
                label = stringResource(R.string.profile_experience),
                value = if (experienceYears > 0) "$experienceYears ${stringResource(R.string.profile_years)}" else stringResource(
                    R.string.profile_not_specified
                )
            )
            ProfileInfoRow(
                label = stringResource(R.string.profile_about_doctor),
                value = aboutDoctor.ifBlank { stringResource(R.string.profile_not_specified) }
            )
        }
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ProfileMenuItem(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(42.dp),
            color = PrimaryBlue.copy(alpha = 0.1f),
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                icon()
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

private fun profileInitials(displayName: String): String {
    val parts = displayName
        .split(" ")
        .filter { value -> value.isNotBlank() }

    return parts
        .take(2)
        .joinToString("") { value -> value.first().uppercaseChar().toString() }
        .ifBlank { "Қ" }
}

private fun formatProfileDate(timestamp: Long): String {
    if (timestamp <= 0L) return "—"
    return runCatching {
        val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        formatter.format(Date(timestamp))
    }.getOrDefault("—")
}

private fun profileRoleLabel(role: String): String {
    return when (role) {
        UserRoles.ADMIN -> "Әкімші"
        UserRoles.DOCTOR -> "Дәрігер"
        else -> "Қолданушы"
    }
}
