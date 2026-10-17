package com.timeline.analytics

import android.content.Context
import android.os.Bundle
import co.touchlab.kermit.Logger
import com.google.firebase.analytics.FirebaseAnalytics

class FirebaseAnalyticsImpl(
    context: Context,
    private val fa: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)
) : Analytics {

    private val logger = Logger.withTag("Analytics")
    private var isConsentEnabled: Boolean = true

    override fun setEnabled(enabled: Boolean) {
        isConsentEnabled = enabled
        try {
            fa.setAnalyticsCollectionEnabled(enabled)
            logger.d { "Analytics collection enabled set to: $enabled" }
        } catch (e: Exception) {
            logger.e(e) { "Failed to set analytics collection enabled" }
        }
    }

    override fun setUserProperty(name: String, value: String?) {
        if (!isConsentEnabled) return
        val sanitizedName = name.take(MAX_NAME_LENGTH)
        val sanitizedValue = value?.take(MAX_VALUE_LENGTH)
        try {
            fa.setUserProperty(sanitizedName, sanitizedValue)
            logger.d { "Set user property: $sanitizedName = $sanitizedValue" }
        } catch (e: Exception) {
            logger.e(e) { "Failed to set user property: $sanitizedName" }
        }
    }

    override fun log(event: String, params: Map<String, Any>) {
        if (!isConsentEnabled) return

        val sanitizedEvent = event.take(MAX_NAME_LENGTH)
        val bundle = Bundle()

        params.entries.take(MAX_PARAMS_PER_EVENT).forEach { (rawKey, value) ->
            val key = rawKey.take(MAX_NAME_LENGTH)
            if (isForbiddenKey(key)) {
                logger.w { "Blocked forbidden parameter key '$key' to protect user privacy." }
                return@forEach
            }

            when (value) {
                is String -> bundle.putString(key, value.take(MAX_VALUE_LENGTH))
                is Int -> bundle.putLong(key, value.toLong())
                is Long -> bundle.putLong(key, value)
                is Double -> bundle.putDouble(key, value)
                is Float -> bundle.putDouble(key, value.toDouble())
                is Boolean -> bundle.putString(key, value.toString())
                else -> bundle.putString(key, value.toString().take(MAX_VALUE_LENGTH))
            }
        }

        try {
            fa.logEvent(sanitizedEvent, bundle)
            logger.d { "Logged event: $sanitizedEvent with params: $params" }
        } catch (e: Exception) {
            logger.e(e) { "Failed to log event: $sanitizedEvent" }
        }
    }

    private fun isForbiddenKey(key: String): Boolean {
        val lower = key.lowercase()
        return lower in FORBIDDEN_KEYS || lower.contains("ocr") || lower.contains("screenshot")
    }

    companion object {
        private const val MAX_NAME_LENGTH = 40
        private const val MAX_VALUE_LENGTH = 100
        private const val MAX_PARAMS_PER_EVENT = 25

        private val FORBIDDEN_KEYS = setOf(
            "ocr",
            "ocr_text",
            "screenshot",
            "screenshot_data",
            "window_title",
            "title",
            "package_name",
            "package",
            "text"
        )
    }
}
