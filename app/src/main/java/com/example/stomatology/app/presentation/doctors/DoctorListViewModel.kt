package com.example.stomatology.app.presentation.doctors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stomatology.app.core.firebase.FirestoreCollections
import com.example.stomatology.app.core.firebase.FirestoreFields
import com.example.stomatology.app.core.firebase.UserRoles
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Дәрігер ақпаратының моделі
 */
data class DoctorItem(
    val id: String,
    val name: String,
    val specialty: String
)

/**
 * Экранның күйі (Loading, Data, Error)
 */
data class DoctorListUiState(
    val doctors: List<DoctorItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class DoctorListViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    // Ішкі өзгертілетін күй
    private val _uiState = MutableStateFlow(DoctorListUiState())
    // Сыртқы тек оқуға арналған күй
    val uiState: StateFlow<DoctorListUiState> = _uiState.asStateFlow()

    init {
        loadDoctors()
    }

    /**
     * Дәрігерлер тізімін Firebase-тен жүктеу
     */
    fun loadDoctors() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Firestore-дан рөлі "DOCTOR" болатын қолданушыларды алу
                val snapshot = firestore.collection(FirestoreCollections.USERS)
                    .whereEqualTo(FirestoreFields.ROLE, UserRoles.DOCTOR)
                    .get()
                    .await()

                val doctorsList = snapshot.documents.mapNotNull { document ->
                    val firstName = document.getString(FirestoreFields.FIRST_NAME).orEmpty()
                    val lastName = document.getString(FirestoreFields.LAST_NAME).orEmpty()

                    // Аты-жөнін құрастыру (DisplayName болмаса, аты мен жөнін қосады)
                    val displayName = document.getString(FirestoreFields.DISPLAY_NAME)
                        ?.takeIf { it.isNotBlank() }
                        ?: "$firstName $lastName".trim()

                    // Егер аты-жөні мүлдем жоқ болса, тізімге қоспаймыз
                    if (displayName.isBlank()) return@mapNotNull null

                    DoctorItem(
                        id = document.getString(FirestoreFields.UID) ?: document.id,
                        name = displayName,
                        specialty = document.getString(FirestoreFields.SPECIALTY) ?: "Стоматолог"
                    )
                }

                _uiState.update {
                    it.copy(
                        doctors = doctorsList,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Деректерді жүктеу кезінде қате шықты"
                    )
                }
            }
        }
    }
}