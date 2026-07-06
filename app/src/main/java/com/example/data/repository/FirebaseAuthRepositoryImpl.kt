package com.example.data.repository

import com.example.domain.model.AuthUser
import com.example.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseAuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    override val currentUser: AuthUser?
        get() = firebaseAuth.currentUser?.toAuthUser()

    override val authState: Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toAuthUser())
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun signIn(email: String, password: String): Result<AuthUser> = runCatching {
        val result = suspendCoroutine { continuation ->
            firebaseAuth.signInWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener { continuation.resume(Result.success(it.user)) }
                .addOnFailureListener { continuation.resume(Result.failure(it)) }
        }.getOrThrow()

        val user = requireNotNull(result) { "Login succeeded but Firebase user was null." }
        ensureUserProfile(user)
        user.toAuthUser()
    }

    override suspend fun signUp(email: String, password: String, displayName: String?): Result<AuthUser> = runCatching {
        val result = suspendCoroutine { continuation ->
            firebaseAuth.createUserWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener { continuation.resume(Result.success(it.user)) }
                .addOnFailureListener { continuation.resume(Result.failure(it)) }
        }.getOrThrow()

        val user = requireNotNull(result) { "Signup succeeded but Firebase user was null." }
        if (!displayName.isNullOrBlank()) {
            updateDisplayName(user, displayName.trim()).getOrThrow()
        }
        sendEmailVerification()
        ensureUserProfile(firebaseAuth.currentUser ?: user)
        (firebaseAuth.currentUser ?: user).toAuthUser()
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        suspendCoroutine { continuation ->
            firebaseAuth.sendPasswordResetEmail(email.trim())
                .addOnSuccessListener { continuation.resume(Result.success(Unit)) }
                .addOnFailureListener { continuation.resume(Result.failure(it)) }
        }.getOrThrow()
    }

    override suspend fun sendEmailVerification(): Result<Unit> = runCatching {
        val user = requireNotNull(firebaseAuth.currentUser) { "No logged-in user found." }
        suspendCoroutine { continuation ->
            user.sendEmailVerification()
                .addOnSuccessListener { continuation.resume(Result.success(Unit)) }
                .addOnFailureListener { continuation.resume(Result.failure(it)) }
        }.getOrThrow()
    }

    override suspend fun continueAsGuest(): Result<AuthUser> = runCatching {
        val result = suspendCoroutine { continuation ->
            firebaseAuth.signInAnonymously()
                .addOnSuccessListener { continuation.resume(Result.success(it.user)) }
                .addOnFailureListener { continuation.resume(Result.failure(it)) }
        }.getOrThrow()

        val user = requireNotNull(result) { "Guest login succeeded but Firebase user was null." }
        ensureUserProfile(user)
        user.toAuthUser()
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        firebaseAuth.signOut()
    }

    private suspend fun updateDisplayName(user: FirebaseUser, displayName: String): Result<Unit> = runCatching {
        val request = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()

        suspendCoroutine { continuation ->
            user.updateProfile(request)
                .addOnSuccessListener { continuation.resume(Result.success(Unit)) }
                .addOnFailureListener { continuation.resume(Result.failure(it)) }
        }.getOrThrow()
    }

    private suspend fun ensureUserProfile(user: FirebaseUser) {
        val profile = mapOf(
            "uid" to user.uid,
            "email" to user.email,
            "displayName" to user.displayName,
            "isAnonymous" to user.isAnonymous,
            "isEmailVerified" to user.isEmailVerified,
            "updatedAt" to System.currentTimeMillis()
        )

        suspendCoroutine { continuation ->
            firestore.collection("users")
                .document(user.uid)
                .set(profile, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener { continuation.resume(Result.success(Unit)) }
                .addOnFailureListener { continuation.resume(Result.failure(it)) }
        }.getOrThrow()
    }

    private fun FirebaseUser.toAuthUser(): AuthUser = AuthUser(
        uid = uid,
        email = email,
        displayName = displayName,
        isEmailVerified = isEmailVerified,
        isAnonymous = isAnonymous
    )
}
