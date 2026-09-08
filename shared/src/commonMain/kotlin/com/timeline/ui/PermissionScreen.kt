package com.timeline.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.presentation.*
import com.timeline.ui.BackHandler
import com.timeline.ui.components.*
import com.timeline.ui.theme.Dimensions
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
    onAllGranted: () -> Unit,
    onNavigateToPaywall: () -> Unit
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
                is PermissionEffect.NavigateToPaywall -> onNavigateToPaywall()
            }
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onEvent(PermissionEvent.CheckPermissions)
    }

    BackHandler(enabled = state.currentStep != OnboardingStep.Welcome) {
        viewModel.onEvent(PermissionEvent.PreviousStep)
    }

    AnimatedContent(
        targetState = state.currentStep,
        transitionSpec = {
            if (targetState.ordinal > initialState.ordinal) {
                (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
            } else {
                (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
            }
        },
        label = "OnboardingStep"
    ) { step ->
        OnboardingStepContent(
            step = step,
            state = state,
            onEvent = viewModel::onEvent,
            onOpenTimeline = {
                viewModel.onEvent(PermissionEvent.StartTracking)
            },
            onNavigateToPaywall = {
                viewModel.selectProPlan()
            }
        )
    }
}

@Composable
private fun OnboardingStepContent(
    step: OnboardingStep,
    state: PermissionState,
    onEvent: (PermissionEvent) -> Unit,
    onOpenTimeline: () -> Unit,
    onNavigateToPaywall: () -> Unit
) {
    when (step) {
        OnboardingStep.Welcome -> UnifiedWelcomeStep(onEvent)
        OnboardingStep.PermissionCardStack -> PermissionCardStackStep(state, onEvent)
        OnboardingStep.ModeSelection -> ModeSelectionStep(onEvent, onOpenTimeline, onNavigateToPaywall)
    }
}

@Composable
private fun ModeSelectionStep(
    onEvent: (PermissionEvent) -> Unit,
    onOpenTimeline: () -> Unit,
    onNavigateToPaywall: () -> Unit
) {
    OnboardingLayout(
        topBar = {
            Column {
                Text(text = AppStrings.OnboardingModeTitle, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                Text(text = AppStrings.OnboardingModeSubtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        },
        bottomBar = {
            OnboardingStepIndicator(total = 3, current = 2, modifier = Modifier.padding(bottom = Dimensions.PaddingMedium))
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Dimensions.PaddingMedium)
        ) {
            // Pro Mode (Behind, Taller)
            ModeCard(
                title = AppStrings.OnboardingProModeTitle,
                desc = AppStrings.OnboardingProModeDesc,
                isPro = true,
                onClick = onNavigateToPaywall,
                buttonText = AppStrings.OnboardingProModeButton
            )
            
            // Basic Mode (In front)
            ModeCard(
                title = AppStrings.OnboardingBasicModeTitle,
                desc = AppStrings.OnboardingBasicModeDesc,
                onClick = onOpenTimeline,
                buttonText = AppStrings.OnboardingBasicModeButton
            )
        }
    }
}

@Composable
private fun ModeCard(
    title: String,
    desc: String,
    onClick: () -> Unit,
    buttonText: String,
    isPro: Boolean = false
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isPro) 320.dp else 280.dp),
        shape = RoundedCornerShape(24.dp),
        color = if (isPro) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(Dimensions.PaddingLarge)) {
            if (isPro) {
                Badge(containerColor = MaterialTheme.colorScheme.primary) { Text(AppStrings.OnboardingProModeBadge) }
                Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))
            }
            Text(text = title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))
            Text(text = desc, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.weight(1f))
            OnboardingActionButton(text = buttonText, onClick = onClick)
        }
    }
}

@Composable
private fun UnifiedWelcomeStep(onEvent: (PermissionEvent) -> Unit) {
    OnboardingLayout(
        bottomBar = {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OnboardingStepIndicator(total = 3, current = 0)
                Spacer(modifier = Modifier.weight(1f))
                OnboardingActionButton(text = AppStrings.ButtonNext, onClick = { onEvent(PermissionEvent.NextStep) }, modifier = Modifier.width(140.dp))
            }
        }
    ) {
        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))
        Icon(imageVector = Icons.Rounded.Timeline, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
        Text(text = AppStrings.OnboardingWelcomeTitle, style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))
        Text(text = AppStrings.OnboardingWelcomeSubtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        
        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))
        
        ValuePropFeature(
            icon = Icons.Rounded.AutoAwesome,
            title = AppStrings.OnboardingValueProp1Title,
            desc = AppStrings.OnboardingValueProp1Desc
        )
        Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
        ValuePropFeature(
            icon = Icons.Rounded.Favorite,
            title = AppStrings.OnboardingValueProp2Title,
            desc = AppStrings.OnboardingValueProp2Desc
        )
    }
}

@Composable
private fun ValuePropFeature(icon: ImageVector, title: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = Dimensions.PaddingSmall),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(Dimensions.PaddingMedium))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PermissionCardStackStep(
    state: PermissionState,
    onEvent: (PermissionEvent) -> Unit
) {
    var showPrivacySheet by remember { mutableStateOf(false) }
    val currentPermission = state.permissions.getOrNull(state.activeCardIndex)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // ... (Header and Stack logic)
        
        // Header Area
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = Dimensions.PaddingLarge, vertical = Dimensions.PaddingMedium)
                .align(Alignment.TopStart)
        ) {
            Text(
                text = AppStrings.OnboardingStackTitle,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = AppStrings.OnboardingStackSubtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        // 2. Interactive Card Stack (Align Bottom)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            state.permissions.asReversed().forEachIndexed { indexFromEnd, permission ->
                val actualIndex = state.permissions.size - 1 - indexFromEnd
                val isVisible = actualIndex >= state.activeCardIndex
                
                val offsetY by animateDpAsState(
                    targetValue = if (isVisible) 0.dp else 1200.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )

                if (offsetY < 1200.dp) {
                    PermissionCard(
                        permission = permission,
                        index = actualIndex,
                        offsetY = offsetY
                    )
                }
            }
        }

        // 3. Overlay Control Area (Indicator + Buttons)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(Dimensions.PaddingLarge)
                .align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimensions.PaddingMedium)
            ) {
                OnboardingStepIndicator(total = 3, current = 1)

                if (currentPermission != null) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.PaddingSmall)
                    ) {
                        OnboardingActionButton(
                            text = if (currentPermission.isGranted) AppStrings.ButtonAlreadyGranted else AppStrings.ButtonGrantAccess,
                            onClick = {
                                if (currentPermission.isGranted) {
                                    onEvent(PermissionEvent.NextStep)
                                } else if (currentPermission.id == "accessibility") {
                                    showPrivacySheet = true
                                } else {
                                    onEvent(PermissionEvent.GrantPermission(currentPermission.id))
                                }
                            }
                        )
                        OnboardingTextButton(
                            text = AppStrings.ButtonSkipForNow,
                            onClick = { onEvent(PermissionEvent.NextStep) }
                        )
                    }
                }
            }
        }

        // Handle completion of all cards
        if (state.activeCardIndex >= state.permissions.size) {
            LaunchedEffect(Unit) {
                onEvent(PermissionEvent.NextStep)
            }
        }
    }

    // Privacy Bottom Sheet for Accessibility
    if (showPrivacySheet) {
        ModalBottomSheet(
            onDismissRequest = { showPrivacySheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.PaddingLarge)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Rounded.PrivacyTip,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
                Text(
                    text = "Accessibility & Privacy",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))
                Text(
                    text = "Timeline uses Accessibility to detect app switches and capture visual context for your daily highlights. Data processing happens on-device and through secure cloud services to improve app functionality and deliver the best highlights.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
                OnboardingActionButton(
                    text = "I Understand & Accept",
                    onClick = {
                        showPrivacySheet = false
                        onEvent(PermissionEvent.GrantPermission("accessibility"))
                    }
                )
                OnboardingTextButton(
                    text = "Cancel",
                    onClick = { showPrivacySheet = false }
                )
            }
        }
    }
}

@Composable
private fun PermissionCard(
    permission: PermissionItem,
    index: Int,
    offsetY: androidx.compose.ui.unit.Dp
) {
    // Back cards (higher indices like 2) are tallest, front cards (index 0) are shortest
    // This allows back cards to peek from the top when all are aligned at the bottom
    val cardHeight = when (index) {
        0 -> 460.dp // Frontmost
        1 -> 520.dp // Middle
        else -> 580.dp // Backmost
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight)
            .offset(y = offsetY),
        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = ((3 - index) * 4).dp, // Higher elevation for front (index 0) cards
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.PaddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
            OnboardingIllustration(
                icon = when (permission.id) {
                    "accessibility" -> Icons.Rounded.AccessibilityNew
                    "usage" -> Icons.Rounded.BarChart
                    else -> Icons.Rounded.Notifications
                },
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
            Text(
                text = permission.title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))
            Text(
                text = permission.description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            // Leave space for the overlay buttons at the bottom
            Spacer(modifier = Modifier.height(160.dp))
        }
    }
}


