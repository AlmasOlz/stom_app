package com.example.stomatology.app.presentation.tracking

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.stomatology.app.R
import com.example.stomatology.app.presentation.profile.UserProfileViewModel
import com.example.stomatology.app.presentation.theme.PrimaryBlue
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.launch

data class VideoTopic(
    val title: String,
    val route: String,
    val icon: ImageVector
)

data class DayProgress(
    val label: String,
    val isCompleted: Boolean
)

@Composable
fun TrackingScreen(
    onBack: () -> Unit,
    onNavigateToReminders: () -> Unit = {},
    onNavigateToInstructions: () -> Unit = {},
    onNavigateToLesson: (String) -> Unit = {},
    profileViewModel: UserProfileViewModel = hiltViewModel()
) {
    val profileState by profileViewModel.uiState.collectAsState()
    val fallbackUserName = stringResource(R.string.tracking_user_fallback)

    val userName = profileState.user.firstName
        .ifBlank { profileState.user.displayName }
        .ifBlank { fallbackUserName }
    val userPhotoUrl = profileState.user.photoUrl.trim()

    if (profileState.isLoading) {
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

    TrackingContent(
        userName = userName,
        userPhotoUrl = userPhotoUrl,
        onNavigateToReminders = onNavigateToReminders,
        onNavigateToInstructions = onNavigateToInstructions,
        onNavigateToLesson = onNavigateToLesson
    )
}

@Composable
private fun TrackingContent(
    userName: String,
    userPhotoUrl: String,
    onNavigateToReminders: () -> Unit,
    onNavigateToInstructions: () -> Unit,
    onNavigateToLesson: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    val weeklyProgress = remember { buildCurrentWeekProgress() }
    val currentStep = weeklyProgress.count { it.isCompleted }.coerceAtLeast(1)
    val totalSteps = weeklyProgress.size

    val videoTopics = listOf(
        VideoTopic(
            title = stringResource(R.string.tracking_video_brushing),
            route = "brushing",
            icon = Icons.Default.PlayArrow
        ),
        VideoTopic(
            title = stringResource(R.string.tracking_video_flossing),
            route = "flossing",
            icon = Icons.Default.PlayArrow
        ),
        VideoTopic(
            title = stringResource(R.string.tracking_video_mouthwash),
            route = "mouthwash",
            icon = Icons.Default.PlayArrow
        )
    )
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    androidx.compose.material3.Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color(0xFFF8F9FA)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.tracking_greeting, userName),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE1F5FE)),
                contentAlignment = Alignment.Center
            ) {
                if (userPhotoUrl.isNotBlank()) {
                    AsyncImage(
                        model = userPhotoUrl,
                        contentDescription = stringResource(R.string.profile_photo),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    val fallbackLetter = userName.trim()
                        .firstOrNull()
                        ?.uppercaseChar()
                        ?.toString()
                        .orEmpty()

                    if (fallbackLetter.isNotBlank()) {
                        Text(
                            text = fallbackLetter,
                            color = PrimaryBlue,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.tracking_current_progress),
                    color = PrimaryBlue,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = stringResource(R.string.tracking_treatment_tooth_extraction),
                    color = Color.DarkGray,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = stringResource(R.string.tracking_step_format, currentStep, totalSteps),
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.tracking_weekly_progress),
                    color = PrimaryBlue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weeklyProgress.forEach { day ->
                        DayProgressItem(day)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.tracking_navigation_title),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ActionCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.tracking_tips),
                icon = Icons.Default.Info,
                onClick = onNavigateToInstructions
            )

            ActionCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.tracking_daily_reminders),
                icon = Icons.Default.Notifications,
                onClick = onNavigateToReminders
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.tracking_videos_title),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(end = 8.dp)
        ) {
            items(videoTopics) { topic ->
                VideoCard(
                    topic = topic,
                    onClick = {
                        if (topic.route.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Бұл сабақ әзірге қолжетімсіз")
                            }
                        } else {
                            onNavigateToLesson(topic.route)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
    }
}

@Composable
private fun DayProgressItem(day: DayProgress) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (day.isCompleted) PrimaryBlue
                    else Color.LightGray.copy(alpha = 0.5f)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (day.isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = day.label,
            fontSize = 12.sp,
            color = if (day.isCompleted) PrimaryBlue else Color.Gray
        )
    }
}

@Composable
fun ActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun VideoCard(
    topic: VideoTopic,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(100.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = topic.icon,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(32.dp)
                )

                Text(
                    text = topic.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun buildCurrentWeekProgress(
    locale: Locale = Locale.getDefault(),
    today: Calendar = Calendar.getInstance()
): List<DayProgress> {
    val calendar = today.clone() as Calendar
    val offsetFromMonday = (7 + calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY) % 7
    val currentDayIndex = offsetFromMonday
    val shortWeekdays = DateFormatSymbols(locale).shortWeekdays

    calendar.add(Calendar.DAY_OF_MONTH, -offsetFromMonday)

    return (0..6).map { index ->
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val label = shortWeekdays[dayOfWeek]
            .replace(".", "")
            .take(2)
            .uppercase(locale)

        calendar.add(Calendar.DAY_OF_MONTH, 1)

        DayProgress(
            label = label,
            isCompleted = index <= currentDayIndex
        )
    }
}
