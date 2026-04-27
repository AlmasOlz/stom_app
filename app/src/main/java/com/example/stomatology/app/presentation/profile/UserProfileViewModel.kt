package com.example.stomatology.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stomatology.app.domain.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class UserProfileUiState(
    val user: UserProfile = UserProfile(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val uid = auth.currentUser?.uid
                    ?: throw IllegalStateException("Пользователь не авторизован")

                val snapshot = firestore.collection("users")
                    .document(uid)
                    .get()
                    .await()

                val user = snapshot.toObject(UserProfile::class.java)
                    ?: UserProfile(
                        uid = uid,
                        email = auth.currentUser?.email.orEmpty()
                    )

                _uiState.update {
                    it.copy(
                        user = user,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Ошибка загрузки профиля"
                    )
                }
            }
        }
    }
}