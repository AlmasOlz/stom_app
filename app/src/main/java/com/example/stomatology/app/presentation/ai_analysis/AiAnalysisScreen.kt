package com.example.stomatology.app.presentation.ai_analysis

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

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
    ) { uri ->
        uri?.let { viewModel.analyzeImage(context, it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("AI X-Ray Analysis") })
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (val state = uiState) {
                is AiState.Idle -> {
                    Text("Upload an X-Ray image to begin")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { galleryLauncher.launch("image/*") }) {
                        Text("Select Image")
                    }
                }
                is AiState.Loading -> {
                    CircularProgressIndicator()
                    Text("Analyzing image with YOLOv8...", modifier = Modifier.padding(top = 16.dp))
                }
                is AiState.Success -> {
                    Text("Analysis Complete!", style = MaterialTheme.typography.headlineSmall)
                    Text("Teeth Detected: ${state.result.teethCount}")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { galleryLauncher.launch("image/*") }) {
                        Text("Analyze Another")
                    }
                }
                is AiState.Error -> {
                    Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                    Button(onClick = { galleryLauncher.launch("image/*") }) {
                        Text("Try Again")
                    }
                }
            }
        }
    }
}