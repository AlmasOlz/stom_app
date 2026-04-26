package com.example.stomatology.app.presentation.clinics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stomatology.app.core.util.Resource
import com.example.stomatology.app.domain.model.Clinic
import com.example.stomatology.app.domain.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// 1. UI State-ті толықтыру
data class ClinicUiState(
    val clinics: List<Clinic> = emptyList(),
    val filteredClinics: List<Clinic> = emptyList(), // Сүзілген тізім
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false, // Pull-to-refresh үшін
    val error: String? = null,
    val searchQuery: String = "" // Іздеу мәтіні
)

@HiltViewModel
class ClinicViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ClinicUiState())
    val state: StateFlow<ClinicUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadClinics()
    }

    // 2. Клиникаларды жүктеу (Refresh мүмкіндігімен)
    fun loadClinics(isRefreshing: Boolean = false) {
        viewModelScope.launch {
            repository.getClinics().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(
                            isLoading = !isRefreshing,
                            isRefreshing = isRefreshing
                        ) }
                    }
                    is Resource.Success -> {
                        val clinics = result.data ?: emptyList()
                        _state.update { it.copy(
                            clinics = clinics,
                            filteredClinics = clinics,
                            isLoading = false,
                            isRefreshing = false
                        ) }
                    }
                    is Resource.Error -> {
                        _state.update { it.copy(
                            error = result.message,
                            isLoading = false,
                            isRefreshing = false
                        ) }
                    }
                }
            }
        }
    }

    // 3. Іздеу логикасы (Debounce-пен: пайдаланушы жазуды тоқтатқанда ғана іздейді)
    fun onSearchQueryChange(newQuery: String) {
        _state.update { it.copy(searchQuery = newQuery) }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300L) // Ресурсты үнемдеу үшін кідіріс
            val filtered = if (newQuery.isBlank()) {
                _state.value.clinics
            } else {
                _state.value.clinics.filter {
                    it.name.contains(newQuery, ignoreCase = true) ||
                            it.address.contains(newQuery, ignoreCase = true)
                }
            }
            _state.update { it.copy(filteredClinics = filtered) }
        }
    }

    // 4. Таңдаулыларға қосу (егер Repository-де осындай функция болса)
    fun toggleFavorite(clinicId: String) {
        viewModelScope.launch {
            // Мұнда Repository арқылы локальді базаға (Room) сақтауға болады
            // repository.toggleFavorite(clinicId)
        }
    }
}