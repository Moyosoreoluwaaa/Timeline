package com.timeline.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.presentation.*
import com.timeline.ui.components.PermissionHeader
import com.timeline.ui.theme.AppAlpha
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings
import androidx.compose.ui.tooling.preview.Preview
import com.timeline.ui.theme.TimelineTheme

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

    PermissionScreenContent(
        state = state,
        onEvent = viewModel::onEvent,
        onLastAnimationFinished = {
            viewModel.onEvent(PermissionEvent.StartTracking)
        }
    )
}

@Composable
private fun PermissionScreenContent(
    state: PermissionState,
    onEvent: (PermissionEvent) -> Unit,
    onLastAnimationFinished: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PermissionHeader(state = state)

            Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimensions.PaddingLarge)
            ) {
                Text(
                    text = if (state.allGranted) AppStrings.PermissionAllSetTitle else AppStrings.PermissionAlmostThereTitle,
                    style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.onBackground),
                )

                Text(
                    text = if (state.allGranted) AppStrings.PermissionAllSetSubtitle else AppStrings.PermissionAlmostThereSubtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground.copy(alpha = AppAlpha.Subtitle)),
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.SpacingHuge))

            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
                color = MaterialTheme.colorScheme.surfaceContainerLowest
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = Dimensions.PaddingLarge)
                ) {
                    PermissionStack(
                        state = state,
                        onEvent = onEvent,
                        onLastAnimationFinished = onLastAnimationFinished
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionStack(
    state: PermissionState,
    onEvent: (PermissionEvent) -> Unit,
    onLastAnimationFinished: () -> Unit
) {
    var hiddenIds by remember { mutableStateOf(state.permissions.filter { it.isGranted }.map { it.id }.toSet()) }

    val visiblePermissions = state.permissions.filter { !hiddenIds.contains(it.id) }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        visiblePermissions.forEachIndexed { index, permission ->
            val stackIndex = index
            key(permission.id) {
                com.timeline.ui.components.PermissionCard(
                    permission = permission,
                    stackIndex = stackIndex,
                    totalRemaining = visiblePermissions.size,
                    onGrant = { onEvent(PermissionEvent.GrantPermission(permission.id)) },
                    onAnimationFinished = {
                        hiddenIds = hiddenIds + permission.id
                        if (state.allGranted && (hiddenIds.size >= state.permissions.size)) {
                            onLastAnimationFinished()
                        }
                    }
                )
            }
        }
    }
}

@Preview
@Composable
private fun PermissionScreenPreview() {
    val sampleState = PermissionState(
        permissions = listOf(
            PermissionItem(
                id = "usage",
                title = "Usage Stats",
                description = "Needed to track app usage",
                isGranted = false
            ),
            PermissionItem(
                id = "overlay",
                title = "Display Over Other Apps",
                description = "Needed to show time limits",
                isGranted = false
            ),
            PermissionItem(
                id = "notifications",
                title = "Notifications",
                description = "Needed for alerts",
                isGranted = false
            )
        ),
        allGranted = false
    )
    TimelineTheme {
        PermissionScreenContent(
            state = sampleState,
            onEvent = {},
            onLastAnimationFinished = {}
        )
    }
}
