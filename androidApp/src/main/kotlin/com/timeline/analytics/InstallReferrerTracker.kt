package com.timeline.analytics

import android.content.Context
import android.net.Uri
import co.touchlab.kermit.Logger
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails

object InstallReferrerTracker {
    private val logger = Logger.withTag("InstallReferrer")

    fun trackInstallReferrer(context: Context, analytics: Analytics) {
        val referrerClient = InstallReferrerClient.newBuilder(context).build()

        referrerClient.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        try {
                            val response: ReferrerDetails = referrerClient.installReferrer
                            val referrerUrl = response.installReferrer
                            val source = parseInstallSource(referrerUrl)
                            analytics.setUserProperty(AnalyticsUserProperties.INSTALL_SOURCE, source)
                            logger.d { "Install referrer parsed: $source (from raw: $referrerUrl)" }
                        } catch (e: Exception) {
                            logger.e(e) { "Failed to read install referrer" }
                            analytics.setUserProperty(AnalyticsUserProperties.INSTALL_SOURCE, "organic")
                        } finally {
                            try {
                                referrerClient.endConnection()
                            } catch (_: Exception) {}
                        }
                    }
                    else -> {
                        logger.d { "Install referrer response code: $responseCode - defaulting to organic" }
                        analytics.setUserProperty(AnalyticsUserProperties.INSTALL_SOURCE, "organic")
                    }
                }
            }

            override fun onInstallReferrerServiceDisconnected() {
                logger.d { "Install referrer service disconnected" }
            }
        })
    }

    internal fun parseInstallSource(referrerUrl: String?): String {
        if (referrerUrl.isNullOrBlank()) return "organic"

        val query = if (referrerUrl.contains("?")) {
            referrerUrl.substringAfter("?")
        } else {
            referrerUrl
        }

        val params = query.split("&").mapNotNull {
            val parts = it.split("=")
            if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
        }.toMap()

        val utmSource = params["utm_source"]?.lowercase() ?: ""

        return when {
            utmSource.contains("instagram") -> "instagram"
            utmSource.contains("tiktok") -> "tiktok"
            utmSource.contains("youtube") -> "youtube"
            utmSource.contains("reddit") -> "reddit"
            utmSource.contains("twitter") || utmSource.contains("x") -> "x"
            utmSource.isNotBlank() -> utmSource.take(40)
            referrerUrl.lowercase().contains("instagram") -> "instagram"
            referrerUrl.lowercase().contains("tiktok") -> "tiktok"
            referrerUrl.lowercase().contains("youtube") -> "youtube"
            referrerUrl.lowercase().contains("reddit") -> "reddit"
            referrerUrl.lowercase().contains("twitter") -> "x"
            else -> "organic"
        }
    }
}
