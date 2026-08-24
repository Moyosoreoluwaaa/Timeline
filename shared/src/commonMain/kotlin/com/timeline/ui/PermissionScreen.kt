package com.timeline.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.presentation.*
import com.timeline.ui.components.PermissionCard
import com.timeline.ui.components.PermissionFooter
import com.timeline.ui.components.PermissionHeader

@Composable
fun PermissionScreen(
    viewModel: PermissionViewModel,
    onNavigateToUsageStats: () -> Unit,
    onNavigateToOverlay: () -> Unit,
    onNavigateToNotification: () -> Unit,
    onNavigateToAccessibility: () -> Unit,
    onNavigateToBatteryOptimization: () -> Unit,
    onAllGranted: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PermissionEffect.NavigateToUsageStatsSettings -> onNavigateToUsageStats()
                is PermissionEffect.NavigateToOverlaySettings -> onNavigateToOverlay()
                is PermissionEffect.RequestNotificationPermission -> onNavigateToNotification()
                is PermissionEffect.NavigateToAccessibilitySettings -> onNavigateToAccessibility()
                is PermissionEffect.NavigateToBatteryOptimizationSettings -> onNavigateToBatteryOptimization()
                is PermissionEffect.AllGranted -> onAllGranted()
            }
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onEvent(PermissionEvent.CheckPermissions)
    }

    Box(
        modifier = Modifier.fillMaxSize().background(
            brush = Brush.verticalGradient(colors = listOf(Color(0xFFE67E22), Color(0xFFF1C40F), Color(0xFF000000)))
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PermissionHeader(onClose = { /* Handle close */ }, state = state)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (state.allGranted) "You're all set!" else "Almost there",
                style = MaterialTheme.typography.headlineMedium.copy(color = Color.White),
                modifier = Modifier.align(Alignment.Start)
            )

            Text(
                text = if (state.allGranted) "Everything is ready for your timeline." else "To show you real insights, we need\naccess to how you use your apps.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.7f)),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.BottomCenter) {
                if (!state.allGranted) {
                    val remainingPermissions = state.permissions.filter { !it.isGranted }
                    remainingPermissions.asReversed().forEachIndexed { index, permission ->
                        val stackIndex = remainingPermissions.size - 1 - index
                        PermissionCard(
                            permission = permission,
                            stackIndex = stackIndex,
                            totalRemaining = remainingPermissions.size,
                            onGrant = { viewModel.onEvent(PermissionEvent.GrantPermission(permission.id)) }
                        )
                    }
                }
                PermissionFooter(allGranted = state.allGranted, onStartTracking = { viewModel.onEvent(PermissionEvent.StartTracking) })
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
