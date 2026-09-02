package com.timeline.domain.usecase

import com.timeline.domain.model.User
import com.timeline.domain.repository.AuthRepository

class SignInWithGoogleUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<User> {
        return authRepository.signInWithGoogle(idToken)
    }
}
