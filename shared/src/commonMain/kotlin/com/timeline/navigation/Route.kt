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
    data object Settings : Route
}
