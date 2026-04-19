package com.example.stomatology.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stomatology.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state

    // --- INPUTS ---
    fun onEmailChange(value: String) {
        _state.value = _state.value.copy(email = value)
    }

    fun onPasswordChange(value: String) {
        _state.value = _state.value.copy(password = value)
    }

    // --- LOGIN ---
    fun login() {
        val email = _state.value.email
        val pass = _state.value.password

        if (email.isBlank() || pass.isBlank()) {
            _state.value = _state.value.copy(error = "Fill all fields")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val result = authRepository.signInWithEmail(email, pass)

            if (result.isSuccess) {
                _state.value = AuthUiState(isSuccess = true)
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Login failed"
                )
            }
        }
    }

    // --- REGISTER ---
    fun signUp() {
        val email = _state.value.email
        val pass = _state.value.password

        if (email.isBlank() || pass.isBlank()) {
            _state.value = _state.value.copy(error = "Fill all fields")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val result = authRepository.signUpWithEmail(email, pass)

            if (result.isSuccess) {
                _state.value = AuthUiState(isSuccess = true)
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Registration failed"
                )
            }
        }
    }
}