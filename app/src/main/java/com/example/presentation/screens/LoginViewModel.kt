package com.example.presentation.screens

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val errorMessage: String? = null
)

class LoginViewModel(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LoginUiState(isAuthenticated = auth.currentUser != null)
    )
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(value: String) {
        _uiState.value = _uiState.value.copy(email = value.trim(), errorMessage = null)
    }

    fun onPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    fun signIn() {
        val state = _uiState.value
        if (!validate(state.email, state.password)) return

        _uiState.value = state.copy(isLoading = true, errorMessage = null)
        auth.signInWithEmailAndPassword(state.email, state.password)
            .addOnCompleteListener { task ->
                _uiState.value = if (task.isSuccessful) {
                    _uiState.value.copy(isLoading = false, isAuthenticated = true)
                } else {
                    _uiState.value.copy(
                        isLoading = false,
                        errorMessage = task.exception?.localizedMessage ?: "Login failed. Please try again."
                    )
                }
            }
    }

    fun createAccount() {
        val state = _uiState.value
        if (!validate(state.email, state.password)) return

        _uiState.value = state.copy(isLoading = true, errorMessage = null)
        auth.createUserWithEmailAndPassword(state.email, state.password)
            .addOnCompleteListener { task ->
                _uiState.value = if (task.isSuccessful) {
                    _uiState.value.copy(isLoading = false, isAuthenticated = true)
                } else {
                    _uiState.value.copy(
                        isLoading = false,
                        errorMessage = task.exception?.localizedMessage ?: "Account creation failed. Please try again."
                    )
                }
            }
    }

    fun signInWithGoogle(idToken: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                _uiState.value = if (task.isSuccessful) {
                    _uiState.value.copy(isLoading = false, isAuthenticated = true)
                } else {
                    _uiState.value.copy(
                        isLoading = false,
                        errorMessage = task.exception?.localizedMessage ?: "Google login failed. Please try again."
                    )
                }
            }
    }

    fun onGoogleSignInFailed(message: String) {
        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = message)
    }

    private fun validate(email: String, password: String): Boolean {
        val message = when {
            email.isBlank() -> "Email is required."
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Enter a valid email address."
            password.length < 6 -> "Password must be at least 6 characters."
            else -> null
        }

        if (message != null) {
            _uiState.value = _uiState.value.copy(errorMessage = message)
            return false
        }
        return true
    }
}
