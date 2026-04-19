package com.example.stomatology.app.presentation.ai_analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stomatology.app.core.util.Resource
import com.example.stomatology.app.domain.model.AiAnalysisResult
import com.example.stomatology.app.domain.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AiAnalysisViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AiState>(AiState.Idle)
    val uiState: StateFlow<AiState> = _uiState

    fun analyzeImage(file: File) {
        viewModelScope.launch {
            _uiState.value = AiState.Loading

            when (val result = repository.analyzeImage(file)) {
                is Resource.Success -> {
                    _uiState.value = AiState.Success(result.data)
                }

                is Resource.Error -> {
                    _uiState.value = AiState.Error(
                        result.message.ifBlank { "Unknown error occurred" }
                    )
                }

                is Resource.Loading -> {
                    _uiState.value = AiState.Loading
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = AiState.Idle
    }
}

sealed class AiState {
    object Idle : AiState()
    object Loading : AiState()
    data class Success(val result: AiAnalysisResult) : AiState()
    data class Error(val message: String) : AiState()
}