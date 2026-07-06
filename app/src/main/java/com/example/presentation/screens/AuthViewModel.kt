package com.example.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.AuthUser
import com.example.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val displayName: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val authUser: AuthUser? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState(authUser = authRepository.currentUser))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.authState.collect { user ->
                _uiState.value = _uiState.value.copy(authUser = user)
            }
        }
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, errorMessage = null, infoMessage = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null, infoMessage = null)
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value, errorMessage = null, infoMessage = null)
    }

    fun onDisplayNameChange(value: String) {
        _uiState.value = _uiState.value.copy(displayName = value, errorMessage = null, infoMessage = null)
    }

    fun signIn(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Email aur password dono zaroori hain.")
            return
        }

        runAuthAction {
            authRepository.signIn(state.email, state.password)
                .onSuccess { onSuccess() }
        }
    }

    fun signUp(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Email aur password dono zaroori hain.")
            return
        }
        if (state.password.length < 6) {
            _uiState.value = state.copy(errorMessage = "Password kam se kam 6 characters ka hona chahiye.")
            return
        }
        if (state.password != state.confirmPassword) {
            _uiState.value = state.copy(errorMessage = "Password aur confirm password match nahi kar rahe.")
            return
        }

        runAuthAction {
            authRepository.signUp(
                email = state.email,
                password = state.password,
                displayName = state.displayName.ifBlank { null }
            ).onSuccess { onSuccess() }
        }
    }

    fun sendPasswordReset() {
        val state = _uiState.value
        if (state.email.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Password reset ke liye email enter karo.")
            return
        }

        runAuthAction(successMessage = "Password reset email bhej diya gaya hai.") {
            authRepository.sendPasswordReset(state.email)
        }
    }

    fun continueAsGuest(onSuccess: () -> Unit) {
        runAuthAction {
            authRepository.continueAsGuest()
                .onSuccess { onSuccess() }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, infoMessage = null)
    }

    private fun runAuthAction(
        successMessage: String? = null,
        action: suspend () -> Result<*>
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, infoMessage = null)
            val result = action()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = result.exceptionOrNull()?.message,
                infoMessage = if (result.isSuccess) successMessage else null
            )
        }
    }
}
