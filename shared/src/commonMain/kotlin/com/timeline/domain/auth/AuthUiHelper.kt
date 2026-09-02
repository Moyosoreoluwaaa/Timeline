package com.timeline.domain.auth

interface AuthUiHelper {
    suspend fun getGoogleIdToken(context: Any): Result<String>
}
