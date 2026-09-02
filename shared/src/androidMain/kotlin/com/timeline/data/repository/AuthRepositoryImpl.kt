package com.timeline.data.repository

import com.timeline.domain.model.User
import com.timeline.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import co.touchlab.kermit.Logger
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.ClearCredentialStateRequest

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val context: Context,
    private val logger: Logger
) : AuthRepository {

    private val tagLogger = logger.withTag("AuthRepository")
    private val credentialManager = CredentialManager.create(context)

    private val _currentUser = MutableStateFlow<User?>(getCurrentUser())
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    override suspend fun signInWithGoogle(idToken: String): Result<User> = runCatching {
        tagLogger.i { "Authenticating with Firebase using Google ID Token" }
        
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = firebaseAuth.signInWithCredential(credential).await()
        val firebaseUser = authResult.user ?: throw IllegalStateException("Firebase User is null")

        val user = User(
            uid = firebaseUser.uid,
            email = firebaseUser.email,
            displayName = firebaseUser.displayName,
            photoUrl = firebaseUser.photoUrl?.toString()
        )
        
        _currentUser.value = user
        tagLogger.i { "Successfully signed in to Firebase: ${user.uid}" }
        user
    }.onFailure { e ->
        tagLogger.e(e) { "Firebase Auth sign-in failed: ${e.localizedMessage}" }
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        tagLogger.i { "Signing out" }
        firebaseAuth.signOut()
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
        _currentUser.value = null
    }

    override fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser ?: return null
        return User(
            uid = firebaseUser.uid,
            email = firebaseUser.email,
            displayName = firebaseUser.displayName,
            photoUrl = firebaseUser.photoUrl?.toString()
        )
    }
}
