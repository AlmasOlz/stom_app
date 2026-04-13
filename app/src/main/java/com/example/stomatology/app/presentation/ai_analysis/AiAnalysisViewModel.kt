package com.example.stomatology.app.presentation.ai_analysis

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stomatology.app.domain.model.AiAnalysisResult
import com.example.stomatology.app.domain.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject


@HiltViewModel
class AiAnalysisViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AiState>(AiState.Idle)
    val uiState: StateFlow<AiState> = _uiState

    fun analyzeImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = AiState.Loading
            try {
                // Convert content URI to temporary File
                val inputStream = context.contentResolver.openInputStream(uri)
                val tempFile = File.createTempFile("xray", ".jpg", context.cacheDir)
                val outputStream = FileOutputStream(tempFile)
                inputStream?.copyTo(outputStream)

                val result = repository.analyzeImage(tempFile)
                _uiState.value = AiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = AiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}

sealed class AiState {
    object Idle : AiState()
    object Loading : AiState()
    data class Success(val result: AiAnalysisResult) : AiState()
    data class Error(val message: String) : AiState()
}