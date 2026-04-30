package com.example.stomatology.app.presentation.profile

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.example.stomatology.app.R
import com.example.stomatology.app.presentation.components.AppBackButton
import com.example.stomatology.app.presentation.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onBack: () -> Unit,
    onSignOut: () -> Unit
) {
    val activity = LocalContext.current as? Activity
    val currentLanguage = AppCompatDelegate.getApplicationLocales()
        .toLanguageTags()
        .takeIf { it.isNotBlank() }
        ?.substringBefore(",")
        ?.lowercase()
        ?.let { tag -> if (tag.startsWith("ru")) "ru" else "kk" }
        ?: "kk"
    var selectedLanguage by rememberSaveable { mutableStateOf(currentLanguage) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.settings_title), fontWeight = FontWeight.Bold) },
                navigationIcon = { AppBackButton(onClick = onBack) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_language),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        selectedLanguage = "kk"
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("kk"))
                        activity?.recreate()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selectedLanguage == "kk") PrimaryBlue.copy(alpha = 0.12f) else Color.Transparent
                    )
                ) {
                    Text(stringResource(R.string.settings_kz), color = if (selectedLanguage == "kk") PrimaryBlue else Color.DarkGray)
                }

                OutlinedButton(
                    onClick = {
                        selectedLanguage = "ru"
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ru"))
                        activity?.recreate()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selectedLanguage == "ru") PrimaryBlue.copy(alpha = 0.12f) else Color.Transparent
                    )
                ) {
                    Text(stringResource(R.string.settings_ru), color = if (selectedLanguage == "ru") PrimaryBlue else Color.DarkGray)
                }
            }

            Text(
                text = if (selectedLanguage == "kk")
                    stringResource(R.string.settings_selected_kz)
                else
                    stringResource(R.string.settings_selected_ru),
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text(stringResource(R.string.settings_signout))
            }
        }
    }
}
