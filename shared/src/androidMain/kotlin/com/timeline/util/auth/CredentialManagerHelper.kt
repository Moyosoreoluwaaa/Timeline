package com.timeline.util.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import co.touchlab.kermit.Logger
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

class CredentialManagerHelper(
    private val webClientId: String,
    private val logger: Logger
) {
    private val tagLogger = logger.withTag("CredentialManagerHelper")

    suspend fun getGoogleIdToken(context: Context): Result<String> = runCatching {
        tagLogger.d { "Initiating Google ID Token request with Web Client ID (prefix): ${webClientId.take(10)}..." }
        
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        tagLogger.i { "Requesting credentials from system bottom sheet" }
        val result = credentialManager.getCredential(
            context = context,
            request = request
        )

        val credential = result.credential
        tagLogger.d { "System returned credential type: ${credential.type}" }
        
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        
        tagLogger.i { "Obtained Google ID Token successfully (length: ${googleIdTokenCredential.idToken.length})" }
        googleIdTokenCredential.idToken
    }.onFailure { e ->
        val errorMessage = when (e) {
            is GetCredentialException -> "Credential Manager error (${e.type}): ${e.message}"
            else -> "Unexpected auth error: ${e.message}"
        }
        tagLogger.e(e) { errorMessage }
    }
}
