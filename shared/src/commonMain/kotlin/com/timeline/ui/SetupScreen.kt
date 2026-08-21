package com.timeline.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.timeline.presentation.SetupEvent
import com.timeline.presentation.SetupViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SetupScreen(
    viewModel: SetupViewModel,
    onNavigateToUsageStats: () -> Unit,
    onNavigateToOverlay: () -> Unit,
    onNavigateToNotification: () -> Unit,
    onNavigateToAccessibility: () -> Unit,
    onComplete: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            if (effect is com.timeline.presentation.SetupEffect.AllPermissionsGranted) {
                onComplete()
            }
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onEvent(SetupEvent.CheckPermissions)
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Permissions Required", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Timeline needs Usage Access, Overlay, Notification, and Accessibility permissions to track activity and capture screenshots.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(32.dp))

            PermissionRow(
                title = "Usage Access",
                isGranted = state.isUsageStatsGranted,
                onClick = onNavigateToUsageStats
            )

            Spacer(modifier = Modifier.height(16.dp))

            PermissionRow(
                title = "Display Over Other Apps",
                isGranted = state.isOverlayGranted,
                onClick = onNavigateToOverlay
            )

            Spacer(modifier = Modifier.height(16.dp))

            PermissionRow(
                title = "Notifications",
                isGranted = state.isNotificationGranted,
                onClick = onNavigateToNotification
            )

            Spacer(modifier = Modifier.height(16.dp))

            PermissionRow(
                title = "Accessibility Service",
                isGranted = state.isAccessibilityGranted,
                onClick = onNavigateToAccessibility
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { viewModel.onEvent(SetupEvent.StartTracking) },
                enabled = state.isUsageStatsGranted && state.isOverlayGranted && 
                        state.isNotificationGranted && state.isAccessibilityGranted,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Tracking")
            }
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (isGranted) {
            Text("✅ Granted", color = MaterialTheme.colorScheme.primary)
        } else {
            Button(onClick = onClick) {
                Text("Enable")
            }
        }
    }
}
