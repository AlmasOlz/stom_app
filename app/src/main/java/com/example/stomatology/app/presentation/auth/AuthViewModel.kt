package com.example.stomatology.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stomatology.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state

    fun onFirstNameChange(value: String) {
        _state.value = _state.value.copy(firstName = value)
    }

    fun onLastNameChange(value: String) {
        _state.value = _state.value.copy(lastName = value)
    }

    fun onPhoneChange(value: String) {
        _state.value = _state.value.copy(phone = value)
    }

    fun onEmailChange(value: String) {
        _state.value = _state.value.copy(email = value)
    }

    fun onPasswordChange(value: String) {
        _state.value = _state.value.copy(password = value)
    }

    fun clearSuccess() {
        _state.value = _state.value.copy(isSuccess = false)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun login() {
        val email = _state.value.email.trim()
        val pass = _state.value.password.trim()

        if (email.isBlank() || pass.isBlank()) {
            _state.value = _state.value.copy(error = "Заполните все поля")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val result = authRepository.signInWithEmail(email, pass)

            if (result.isSuccess) {
                val uid = authRepository.getCurrentUserId()
                if (uid == null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "User id not found after login"
                    )
                    return@launch
                }

                val roleResult = authRepository.getUserRole(uid)

                if (roleResult.isSuccess) {
                    _state.value = AuthUiState(
                        email = email,
                        password = "",
                        isLoading = false,
                        isSuccess = true,
                        role = roleResult.getOrNull()
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = roleResult.exceptionOrNull()?.message ?: "Failed to load user role"
                    )
                }
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Login failed"
                )
            }
        }
    }

    fun signUp() {
        val firstName = _state.value.firstName.trim()
        val lastName = _state.value.lastName.trim()
        val phone = _state.value.phone.trim()
        val email = _state.value.email.trim()
        val pass = _state.value.password.trim()

        if (
            firstName.isBlank() ||
            lastName.isBlank() ||
            phone.isBlank() ||
            email.isBlank() ||
            pass.isBlank()
        ) {
            _state.value = _state.value.copy(error = "Заполните все поля")
            return
        }

        if (pass.length < 6) {
            _state.value = _state.value.copy(error = "Пароль должен быть минимум 6 символов")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val result = authRepository.signUpWithEmail(
                email = email,
                pass = pass,
                firstName = firstName,
                lastName = lastName,
                phone = phone
            )

            if (result.isSuccess) {
                _state.value = AuthUiState(
                    email = email,
                    password = "",
                    isLoading = false,
                    isSuccess = true,
                    role = "patient"
                )
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Registration failed"
                )
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _state.value = AuthUiState()
    }
}