package com.timeline.domain.reasoning

import kotlinx.serialization.Serializable

@Serializable
data class AppDailyReasoning(
    val packageName: String,
    val date: String,
    val summary: String,
    val cognitiveMode: String,
    val actionItems: List<String>,
    val entities: LocalEntities = LocalEntities()
)

@Serializable
data class LocalEntities(
    val emails: List<String> = emptyList(),
    val urls: List<String> = emptyList(),
    val dates: List<String> = emptyList()
)

object PrivacyExclusionProvider {
    private val blacklistedPackages = setOf(
        "com.android.settings",
        "com.google.android.apps.authenticator",
        "com.apple.Keynote", // Example
        "com.microsoft.teams", // Can be sensitive
        "org.thoughtcrime.securesms", // Signal
        "ch.threema.app",
        "com.google.android.apps.healthdata"
    )

    private val sensitiveKeywords = setOf(
        "banking", "finance", "password", "medical", "health", "wallet", "crypto"
    )

    fun isPackageExcluded(packageName: String): Boolean {
        if (blacklistedPackages.any { packageName.startsWith(it) }) return true
        if (sensitiveKeywords.any { packageName.lowercase().contains(it) }) return true
        return false
    }
}
