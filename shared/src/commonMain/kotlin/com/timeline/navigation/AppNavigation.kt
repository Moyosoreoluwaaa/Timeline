package com.timeline.navigation

import androidx.compose.runtime.Composable

@Composable
expect fun AppNavigation(
    onNavigateToUsageStats: () -> Unit,
    onNavigateToOverlay: () -> Unit,
    onNavigateToNotification: () -> Unit,
    onNavigateToAccessibility: () -> Unit,
    onStartService: () -> Unit
)
