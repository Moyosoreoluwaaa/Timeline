package com.timeline.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.presentation.*
import com.timeline.ui.components.PermissionCard
import com.timeline.ui.components.PermissionFooter
import com.timeline.ui.components.PermissionHeader
import com.timeline.ui.theme.AppAlpha
import com.timeline.ui.theme.AppColors
import com.timeline.ui.theme.AppWeights
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
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun PermissionScreenContent(
    state: PermissionState,
    onEvent: (PermissionEvent) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().background(
            brush = Brush.verticalGradient(colors = AppColors.BrandGradient)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = Dimensions.PaddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PermissionHeader(onClose = { /* Handle close */ }, state = state)

            Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))

            Text(
                text = if (state.allGranted) AppStrings.PermissionAllSetTitle else AppStrings.PermissionAlmostThereTitle,
                style = MaterialTheme.typography.headlineMedium.copy(color = androidx.compose.ui.graphics.Color.White),
                modifier = Modifier.align(Alignment.Start)
            )

            Text(
                text = if (state.allGranted) AppStrings.PermissionAllSetSubtitle else AppStrings.PermissionAlmostThereSubtitle,
                style = MaterialTheme.typography.bodyMedium.copy(color = androidx.compose.ui.graphics.Color.White.copy(alpha = AppAlpha.Subtitle)),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(Dimensions.SpacingHuge)) // 48dp

            Box(modifier = Modifier.fillMaxWidth().weight(AppWeights.Full), contentAlignment = Alignment.BottomCenter) {
                if (!state.allGranted) {
                    val remainingPermissions = state.permissions.filter { !it.isGranted }
                    remainingPermissions.asReversed().forEachIndexed { index, permission ->
                        val stackIndex = remainingPermissions.size - 1 - index
                        PermissionCard(
                            permission = permission,
                            stackIndex = stackIndex,
                            totalRemaining = remainingPermissions.size,
                            onGrant = { onEvent(PermissionEvent.GrantPermission(permission.id)) }
                        )
                    }
                }
                PermissionFooter(allGranted = state.allGranted, onStartTracking = { onEvent(PermissionEvent.StartTracking) })
            }
            Spacer(modifier = Modifier.height(Dimensions.IconLarge)) // 48dp
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
            onEvent = {}
        )
    }
}
