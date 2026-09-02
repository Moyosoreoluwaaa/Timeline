package com.timeline.util.auth

import android.content.Context
import com.timeline.domain.auth.AuthUiHelper
import com.timeline.domain.SecretConstants
import co.touchlab.kermit.Logger

class AndroidAuthUiHelper(
    private val logger: Logger
) : AuthUiHelper {
    
    private val helper = CredentialManagerHelper(
        webClientId = SecretConstants.GOOGLE_WEB_CLIENT_ID,
        logger = logger
    )

    override suspend fun getGoogleIdToken(context: Any): Result<String> {
        val androidContext = context as? Context ?: return Result.failure(Exception("Invalid Android Context"))
        return helper.getGoogleIdToken(androidContext)
    }
}
