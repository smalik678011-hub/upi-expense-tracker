package com.example.core.session

import com.example.domain.model.AuthUser
import com.example.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

/**
 * Central session gateway for presentation and sync layers.
 * Keeps auth access behind domain contracts so Firebase can be changed later.
 */
class SessionManager(
    private val authRepository: AuthRepository
) {
    val authState: Flow<AuthUser?> = authRepository.authState

    val currentUser: AuthUser?
        get() = authRepository.currentUser

    val isSignedIn: Boolean
        get() = currentUser != null

    val isGuest: Boolean
        get() = currentUser?.isAnonymous == true

    suspend fun signOut(): Result<Unit> = authRepository.signOut()
}
