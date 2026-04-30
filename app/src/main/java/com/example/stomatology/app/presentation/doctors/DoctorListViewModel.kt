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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class DoctorItem(
    val id: String,
    val name: String,
    val specialty: String
)

data class DoctorListUiState(
    val doctors: List<DoctorItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class DoctorListViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(DoctorListUiState())
    val uiState: StateFlow<DoctorListUiState> = _uiState

    init {
        loadDoctors()
    }

    fun loadDoctors() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val snapshot = firestore.collection(FirestoreCollections.USERS)
                    .whereEqualTo(FirestoreFields.ROLE, UserRoles.DOCTOR)
                    .get()
                    .await()

                val doctors = snapshot.documents.map { document ->
                    val firstName = document.getString(FirestoreFields.FIRST_NAME).orEmpty()
                    val lastName = document.getString(FirestoreFields.LAST_NAME).orEmpty()
                    val displayName = document.getString(FirestoreFields.DISPLAY_NAME)
                        ?: "$firstName $lastName".trim()

                    DoctorItem(
                        id = document.getString(FirestoreFields.UID) ?: document.id,
                        name = displayName,
                        specialty = document.getString(FirestoreFields.SPECIALTY).orEmpty()
                    )
                }.filter { doctor ->
                    doctor.name.isNotBlank()
                }

                _uiState.update {
                    it.copy(
                        doctors = doctors,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: ""
                    )
                }
            }
        }
    }
}
