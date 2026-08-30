package com.timeline.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.presentation.*
import com.timeline.ui.components.PermissionActiveCard
import com.timeline.ui.components.PermissionHeaderText
import com.timeline.ui.components.PermissionPeekRow
import com.timeline.ui.components.PermissionTopIndicator
import com.timeline.ui.theme.Dimensions
import com.timeline.ui.theme.TimelineTheme
import com.timeline.util.AppStrings
import kotlinx.coroutines.delay

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
        onAllPermissionsSettled = {
            viewModel.onEvent(PermissionEvent.StartTracking)
        }
    )
}

@Composable
private fun PermissionScreenContent(
    state: PermissionState,
    onEvent: (PermissionEvent) -> Unit,
    onAllPermissionsSettled: () -> Unit
) {
    // Once every permission is granted, give the last card's exit transition
    // a moment to finish before handing off — avoids cutting the animation.
    LaunchedEffect(state.allGranted) {
        if (state.allGranted && state.permissions.isNotEmpty()) {
            delay(Dimensions.PermissionSettledDelayMs)
            onAllPermissionsSettled()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.statusBarsPadding())
            Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))

            PermissionTopIndicator(
                total = state.permissions.size,
                filledCount = state.permissions.count { it.isGranted }
            )

            Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

            PermissionHeaderText(
                title = if (state.allGranted) AppStrings.PermissionAllSetTitle else AppStrings.PermissionAlmostThereTitle,
                subtitle = if (state.allGranted) AppStrings.PermissionAllSetSubtitle else AppStrings.PermissionAlmostThereSubtitle
            )

            Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))

            PermissionSheet(
                state = state,
                onEvent = onEvent,
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
        }
    }
}

/**
 * Full-screen-spanning, bottom-sheet-styled panel: rounded top corners + a
 * standard Material drag handle, a horizontally scrolling row of queued
 * permissions peeking above, and the active permission expanded to fill the
 * rest of the height. This is a persistent panel (not a dismissible
 * ModalBottomSheet) — the rounded-corner + drag-handle treatment is purely
 * visual, matching "bottom sheet style" without the swipe-to-dismiss behavior
 * that would let someone skip permissions entirely.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PermissionSheet(
    state: PermissionState,
    onEvent: (PermissionEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val ungranted = state.permissions.filter { !it.isGranted }
    var selectedPermissionId by remember { mutableStateOf<String?>(null) }

    // Auto-select first ungranted if none selected or selected is now granted
    val activeId = selectedPermissionId ?: ungranted.firstOrNull()?.id
    val active = ungranted.find { it.id == activeId }

    // If the currently selected permission gets granted, move to the next one
    LaunchedEffect(ungranted.map { it.id }) {
        if (selectedPermissionId != null && ungranted.none { it.id == selectedPermissionId }) {
            selectedPermissionId = ungranted.firstOrNull()?.id
        }
    }

    Surface(
        modifier = modifier.clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                BottomSheetDefaults.DragHandle()
            }

            if (ungranted.size > 1) {
                PermissionPeekRow(
                    permissions = ungranted,
                    selectedId = activeId,
                    onPermissionSelected = { selectedPermissionId = it }
                )
                Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
            }

            AnimatedContent(
                targetState = active,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                transitionSpec = {
                    (slideInVertically { height -> height / 4 } + fadeIn()) togetherWith
                            (slideOutVertically { height -> -height / 4 } + fadeOut())
                },
                label = "ActivePermission"
            ) { permission ->
                if (permission != null) {
                    PermissionActiveCard(
                        permission = permission,
                        onGrant = { onEvent(PermissionEvent.GrantPermission(permission.id)) },
                        onNotNow = {
                            // Cycle to next ungranted permission
                            val currentIndex = ungranted.indexOfFirst { it.id == permission.id }
                            if (currentIndex != -1 && ungranted.size > 1) {
                                val nextIndex = (currentIndex + 1) % ungranted.size
                                selectedPermissionId = ungranted[nextIndex].id
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun PermissionScreenPreview() {
    val sampleState = PermissionState(
        permissions = listOf(
            PermissionItem(id = "accessibility", title = "Accessibility", description = "Helps us deliver weather updates and alerts even when the app is closed.", isGranted = false),
            PermissionItem(id = "overlay", title = "Display over other apps", description = "Allows weather alerts and widgets to appear on top of other apps.", isGranted = false),
            PermissionItem(id = "notifications", title = "Notifications", description = "Get real-time weather alerts, daily forecasts, and important updates.", isGranted = false),
            PermissionItem(id = "battery", title = "Ignore battery optimization", description = "Helps us keep weather alerts reliable and on time in the background.", isGranted = false)
        ),
        allGranted = false
    )
    TimelineTheme {
        PermissionScreenContent(
            state = sampleState,
            onEvent = {},
            onAllPermissionsSettled = {}
        )
    }
}