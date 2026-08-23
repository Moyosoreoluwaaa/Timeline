package com.timeline.presentation

data class AuthState(
    val isLoading: Boolean = false,
    val isVerifying: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false
)

sealed interface AuthEvent {
    data object SignInWithGoogle : AuthEvent
    data object SignInWithApple : AuthEvent
    data class SignInWithEmail(val email: String) : AuthEvent
}

sealed interface AuthEffect {
    data object AuthSuccess : AuthEffect
}
