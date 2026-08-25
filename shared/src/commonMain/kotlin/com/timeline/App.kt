package com.timeline

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.timeline.navigation.AppNavigation
import com.timeline.ui.theme.TimelineTheme

@Composable
fun App(
    onNavigateToUsageStats: () -> Unit,
    onNavigateToOverlay: () -> Unit,
    onNavigateToNotification: () -> Unit,
    onNavigateToAccessibility: () -> Unit,
    onNavigateToBatteryOptimization: () -> Unit,
    onStartService: () -> Unit
) {
    TimelineTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            AppNavigation(
                onNavigateToUsageStats = onNavigateToUsageStats,
                onNavigateToOverlay = onNavigateToOverlay,
                onNavigateToNotification = onNavigateToNotification,
                onNavigateToAccessibility = onNavigateToAccessibility,
                onNavigateToBatteryOptimization = onNavigateToBatteryOptimization,
                onStartService = onStartService
            )
        }
    }
}
