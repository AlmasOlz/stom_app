package com.example.stomatology.app.presentation.education

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.stomatology.app.presentation.theme.PrimaryBlue
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun LessonScreen(
    topic: String,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Сабаққа байланысты видео мен тақырыпты таңдау
    val (displayTitle, videoId) = when (topic.lowercase()) {
        "brushing" -> "Урок по чистке зубов" to "R0M0Ry3MhME"
        "flossing" -> "Урок по зубной нити" to "3X9pTOnv_A8"
        "mouth wash" -> "Урок по полосканию" to "R0M0Ry3MhME" // ID-і қажет болса өзгертіңіз
        else -> "Обучающий урок" to "R0M0Ry3MhME"
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Шапка (Header)
            Surface(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                color = PrimaryBlue
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(40.dp).background(Color.Black.copy(0.2f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = Color.White)
                    }
                    Text(displayTitle, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Начнём...", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Следуйте видео-инструкции ниже, чтобы правильно ухаживать за полостью рта.", color = Color.DarkGray)

            // YouTube Player
            Spacer(modifier = Modifier.height(24.dp))
            AndroidView(
                modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(20.dp)),
                factory = { context ->
                    YouTubePlayerView(context).apply {
                        addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                            override fun onReady(youTubePlayer: YouTubePlayer) {
                                youTubePlayer.cueVideo(videoId, 0f)
                            }
                        })
                    }
                }
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Навигация батырмалары
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                val btnColor = ButtonDefaults.buttonColors(containerColor = Color(0xFF8CC6E9))
                Button(onClick = onBack, modifier = Modifier.weight(1f).height(56.dp), colors = btnColor, shape = RoundedCornerShape(28.dp)) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null)
                    Text(" Назад")
                }
                Button(onClick = { }, modifier = Modifier.weight(1f).height(56.dp), colors = btnColor, shape = RoundedCornerShape(28.dp)) {
                    Text("Вперед ")
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
                }
            }
        }
    }
}
