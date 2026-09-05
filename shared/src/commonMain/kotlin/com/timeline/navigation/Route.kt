package com.timeline.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object Permission : Route
    @Serializable
    data object Auth : Route
    @Serializable
    data object Timeline : Route
    @Serializable
    data object Insights : Route
    @Serializable
    data object Settings : Route
    @Serializable
    data object CustomerCenter : Route
    @Serializable
    data class Paywall(val isDealsVariant: Boolean = false) : Route
    @Serializable
    data class FullScreenImage(val path: String) : Route
}
