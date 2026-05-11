package com.example.stomatology.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stomatology.app.core.util.Resource
import com.example.stomatology.app.domain.model.Appointment
import com.example.stomatology.app.domain.model.Clinic
import com.example.stomatology.app.domain.repository.AppRepository
import com.example.stomatology.app.domain.repository.AppointmentRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

enum class HomeSortOption {
    PRICE_ASC,
    RATING_DESC
}

data class HomeClinicFilters(
    val sortOption: HomeSortOption = HomeSortOption.RATING_DESC,
    val minRating: Double = 0.0,
    val district: String = "",
    val openNowOnly: Boolean = false
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val clinics: List<Clinic> = emptyList(),
    val filteredClinics: List<Clinic> = emptyList(),
    val nearbyClinics: List<Clinic> = emptyList(),
    val recentAppointments: List<Appointment> = emptyList(),
    val quickRebook: Appointment? = null,
    val filters: HomeClinicFilters = HomeClinicFilters(),
    val userLat: Double? = null,
    val userLon: Double? = null,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val appointmentRepository: AppointmentRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeClinics()
        observePatientAppointmentsWhenSignedIn()
    }

    fun onLocationUpdated(lat: Double, lon: Double) {
        _uiState.update { state ->
            state.copy(userLat = lat, userLon = lon)
        }
        recomputeDerivedState()
    }

    fun onSortChanged(option: HomeSortOption) {
        _uiState.update { state ->
            state.copy(filters = state.filters.copy(sortOption = option))
        }
        recomputeDerivedState()
    }

    fun onMinRatingChanged(minRating: Double) {
        _uiState.update { state ->
            state.copy(filters = state.filters.copy(minRating = minRating))
        }
        recomputeDerivedState()
    }

    fun onDistrictChanged(district: String) {
        _uiState.update { state ->
            state.copy(filters = state.filters.copy(district = district))
        }
        recomputeDerivedState()
    }

    fun onOpenNowOnlyChanged(enabled: Boolean) {
        _uiState.update { state ->
            state.copy(filters = state.filters.copy(openNowOnly = enabled))
        }
        recomputeDerivedState()
    }

    private fun observeClinics() {
        viewModelScope.launch {
            appRepository.getClinics().collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }

                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                clinics = result.data,
                                error = null
                            )
                        }
                        recomputeDerivedState()
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    private fun observePatientAppointmentsWhenSignedIn() {
        viewModelScope.launch {
            auth.authUidChanges().distinctUntilChanged().collectLatest { uid ->
                if (uid.isNullOrBlank()) {
                    _uiState.update {
                        it.copy(recentAppointments = emptyList(), quickRebook = null)
                    }
                    return@collectLatest
                }
                appointmentRepository.getAppointmentsForPatient(uid).collectLatest { appointments ->
                    val recent = appointments.take(5)
                    _uiState.update {
                        it.copy(
                            recentAppointments = recent,
                            quickRebook = recent.firstOrNull()
                        )
                    }
                }
            }
        }
    }

    private fun recomputeDerivedState() {
        val state = _uiState.value
        val openNow = isClinicOpenNow()

        val filtered = state.clinics
            .asSequence()
            .filter { clinic ->
                state.filters.minRating <= 0.0 || clinic.rating >= state.filters.minRating
            }
            .filter { clinic ->
                state.filters.district.isBlank() ||
                    clinic.address.contains(state.filters.district, ignoreCase = true)
            }
            .filter { _ ->
                !state.filters.openNowOnly || openNow
            }
            .let { clinics ->
                when (state.filters.sortOption) {
                    HomeSortOption.PRICE_ASC -> clinics.sortedBy { it.priceFrom }
                    HomeSortOption.RATING_DESC -> clinics.sortedByDescending { it.rating }
                }
            }
            .toList()

        val nearby = if (state.userLat != null && state.userLon != null) {
            filtered
                .filter { hasCoordinates(it) }
                .sortedBy { clinic ->
                    distanceKm(
                        state.userLat,
                        state.userLon,
                        clinic.latitude,
                        clinic.longitude
                    )
                }
                .take(5)
        } else {
            filtered
                .filter { hasCoordinates(it) }
                .take(5)
        }

        _uiState.update {
            it.copy(
                filteredClinics = filtered,
                nearbyClinics = nearby
            )
        }
    }

    private fun hasCoordinates(clinic: Clinic): Boolean {
        return clinic.latitude in -90.0..90.0 &&
            clinic.longitude in -180.0..180.0 &&
            (clinic.latitude != 0.0 || clinic.longitude != 0.0)
    }

    private fun isClinicOpenNow(now: LocalDateTime = LocalDateTime.now()): Boolean {
        // App-wide heuristic: Mon-Sat 09:00-20:00.
        val day = now.dayOfWeek.value // 1=Mon ... 7=Sun
        val hour = now.hour
        return day in 1..6 && hour in 9..19
    }

    private fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
            kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
            kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return earthRadiusKm * c
    }
}

private fun FirebaseAuth.authUidChanges() = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        trySend(firebaseAuth.currentUser?.uid)
    }
    addAuthStateListener(listener)
    trySend(currentUser?.uid)
    awaitClose { removeAuthStateListener(listener) }
}
