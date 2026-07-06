package com.example.domain.model

/**
 * Lightweight authenticated-user model used by the app layer.
 * Firebase-specific objects should stay inside data repositories.
 */
data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val isEmailVerified: Boolean,
    val isAnonymous: Boolean
)
