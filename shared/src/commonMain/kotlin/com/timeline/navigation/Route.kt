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
    data object Highlight : Route
    @Serializable
    data object RawHighlight : Route
    @Serializable
    data object SimplifiedHighlight : Route
    @Serializable
    data object HighlightLoading : Route
    @Serializable
    data object Insights : Route
    @Serializable
    data object Settings : Route
    @Serializable
    data object CustomerCenter : Route
    @Serializable
    data class Paywall(val isDealsVariant: Boolean = false, val source: String = "locked_feature") : Route
    @Serializable
    data class FullScreenImage(val path: String) : Route
}
