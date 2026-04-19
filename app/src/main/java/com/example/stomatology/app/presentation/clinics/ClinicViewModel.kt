package com.example.stomatology.app.presentation.clinics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stomatology.app.core.util.Resource
import com.example.stomatology.app.domain.model.Clinic
import com.example.stomatology.app.domain.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClinicUiState(
    val clinics: List<Clinic> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ClinicViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ClinicUiState())
    val state: StateFlow<ClinicUiState> = _state

    init {
        loadClinics()
    }

    private fun loadClinics() {
        viewModelScope.launch {
            repository.getClinics().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }

                    is Resource.Success -> {
                        _state.value = ClinicUiState(
                            clinics = result.data,
                            isLoading = false
                        )
                    }

                    is Resource.Error -> {
                        _state.value = ClinicUiState(
                            error = result.message,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }
}