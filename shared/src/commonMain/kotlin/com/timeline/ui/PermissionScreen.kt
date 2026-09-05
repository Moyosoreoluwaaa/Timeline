package com.timeline.ui

import androidx.compose.animation.*
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
            onEvent = viewModel::onEvent,
            onOpenTimeline = {
                viewModel.onEvent(PermissionEvent.StartTracking)
            }
        )
    }
}

@Composable
private fun OnboardingStepContent(
    step: OnboardingStep,
    onEvent: (PermissionEvent) -> Unit,
    onOpenTimeline: () -> Unit
) {
    when (step) {
        OnboardingStep.Welcome -> WelcomeStep(onEvent)
        OnboardingStep.ValueProp -> ValuePropStep(onEvent)
        OnboardingStep.PermissionOverview -> PermissionOverviewStep(onEvent)
        OnboardingStep.AccessibilityIntro -> AccessibilityIntroStep(onEvent)
        OnboardingStep.AccessibilityGrant -> AccessibilityGrantStep(onEvent)
        OnboardingStep.AccessibilitySuccess -> AccessibilitySuccessStep(onEvent)
        OnboardingStep.AccessibilityFailure -> AccessibilityFailureStep(onEvent)
        OnboardingStep.UsageIntro -> UsageIntroStep(onEvent)
        OnboardingStep.UsageGrant -> UsageGrantStep(onEvent)
        OnboardingStep.UsageSuccess -> UsageSuccessStep(onEvent)
        OnboardingStep.UsageFailure -> UsageFailureStep(onEvent)
        OnboardingStep.NotificationsIntro -> NotificationsIntroStep(onEvent)
        OnboardingStep.NotificationsGrant -> NotificationsGrantStep(onEvent)
        OnboardingStep.AllSet -> AllSetStep(onOpenTimeline)
    }
}

@Composable
private fun WelcomeStep(onEvent: (PermissionEvent) -> Unit) {
    OnboardingLayout(
        bottomBar = {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OnboardingStepIndicator(total = 3, current = 0)
                Spacer(modifier = Modifier.weight(1f))
                OnboardingActionButton(text = AppStrings.ButtonNext, onClick = { onEvent(PermissionEvent.NextStep) }, modifier = Modifier.width(120.dp))
            }
        }
    ) {
        Spacer(modifier = Modifier.height(Dimensions.SpacingMega))
        Icon(imageVector = Icons.Rounded.Timeline, contentDescription = null, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(Dimensions.SpacingGiant))
        Text(text = AppStrings.OnboardingWelcomeTitle, style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
        Text(text = AppStrings.OnboardingWelcomeSubtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@Composable
private fun ValuePropStep(onEvent: (PermissionEvent) -> Unit) {
    OnboardingLayout(
        bottomBar = {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OnboardingStepIndicator(total = 3, current = 1)
                Spacer(modifier = Modifier.weight(1f))
                OnboardingActionButton(text = AppStrings.ButtonNext, onClick = { onEvent(PermissionEvent.NextStep) }, modifier = Modifier.width(120.dp))
            }
        }
    ) {
        Spacer(modifier = Modifier.height(Dimensions.SpacingMega))
        Icon(imageVector = Icons.Rounded.HourglassEmpty, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(Dimensions.SpacingGiant))
        Text(text = AppStrings.OnboardingValuePropTitle, style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
        Text(text = AppStrings.OnboardingValuePropSubtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@Composable
private fun PermissionOverviewStep(onEvent: (PermissionEvent) -> Unit) {
    OnboardingLayout(
        bottomBar = {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OnboardingStepIndicator(total = 3, current = 2)
                Spacer(modifier = Modifier.weight(1f))
                OnboardingActionButton(text = AppStrings.ButtonNext, onClick = { onEvent(PermissionEvent.NextStep) }, modifier = Modifier.width(120.dp))
            }
        }
    ) {
        Spacer(modifier = Modifier.height(Dimensions.SpacingGiant))
        Text(text = AppStrings.OnboardingOverviewTitle, style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
        Text(text = AppStrings.OnboardingOverviewSubtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column {
                OnboardingPermissionFeature(icon = Icons.Rounded.AccessibilityNew, title = AppStrings.OnboardingAccessibilityGrantTitle)
                HorizontalDivider(modifier = Modifier.padding(horizontal = Dimensions.PaddingMedium), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                OnboardingPermissionFeature(icon = Icons.Rounded.BarChart, title = AppStrings.OnboardingUsageGrantTitle)
                HorizontalDivider(modifier = Modifier.padding(horizontal = Dimensions.PaddingMedium), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                OnboardingPermissionFeature(icon = Icons.Rounded.Notifications, title = AppStrings.OnboardingNotificationsGrantTitle)
            }
        }
    }
}

@Composable
private fun AccessibilityIntroStep(onEvent: (PermissionEvent) -> Unit) {
    OnboardingLayout(
        bottomBar = {
            Column {
                OnboardingActionButton(text = AppStrings.ButtonUnderstandContinue, onClick = { onEvent(PermissionEvent.NextStep) })
                OnboardingTextButton(text = AppStrings.ButtonNotNow, onClick = { onEvent(PermissionEvent.NextStep) })
            }
        }
    ) {
        Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            OnboardingIllustration(icon = Icons.Rounded.AccessibilityNew, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
        Text(text = AppStrings.OnboardingAccessibilityIntroTitle, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
        
        OnboardingIntroFeature(icon = Icons.Rounded.Visibility, text = AppStrings.OnboardingAccessibilityIntro1)
        OnboardingIntroFeature(icon = Icons.Rounded.ChatBubbleOutline, text = AppStrings.OnboardingAccessibilityIntro2)
        OnboardingIntroFeature(icon = Icons.Rounded.History, text = AppStrings.OnboardingAccessibilityIntro3)
        
        Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ) {
            Row(modifier = Modifier.padding(Dimensions.PaddingMedium), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.width(Dimensions.PaddingMedium))
                Text(text = AppStrings.OnboardingAccessibilityIntroFooter, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
private fun AccessibilityGrantStep(onEvent: (PermissionEvent) -> Unit) {
    OnboardingLayout(
        topBar = { OnboardingPermissionIndicator(current = 1) },
        bottomBar = {
            OnboardingActionButton(text = AppStrings.ButtonGrantAccess, onClick = { onEvent(PermissionEvent.GrantPermission("accessibility")) })
        }
    ) {
        Spacer(modifier = Modifier.height(Dimensions.SpacingGiant))
        Text(text = AppStrings.OnboardingAccessibilityGrantTitle, style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ) {
            Column(modifier = Modifier.padding(Dimensions.PaddingLarge), horizontalAlignment = Alignment.CenterHorizontally) {
                OnboardingIllustration(icon = Icons.Rounded.AccessibilityNew, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
                Text(text = AppStrings.OnboardingAccessibilityGrantSubtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun AccessibilitySuccessStep(onEvent: (PermissionEvent) -> Unit) {
    OnboardingLayout(
        bottomBar = {
            OnboardingActionButton(text = AppStrings.ButtonContinue, onClick = { onEvent(PermissionEvent.NextStep) })
        }
    ) {
        Spacer(modifier = Modifier.height(Dimensions.SpacingGiant))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Surface(modifier = Modifier.size(80.dp), shape = CircleShape, color = Color(0xFF4CAF50)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))
        Text(text = AppStrings.OnboardingAccessibilitySuccessTitle, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
        Text(text = AppStrings.OnboardingAccessibilitySuccessSubtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun AccessibilityFailureStep(onEvent: (PermissionEvent) -> Unit) {
    OnboardingLayout(
        bottomBar = {
            Column {
                OnboardingActionButton(text = AppStrings.ButtonTryAgain, onClick = { onEvent(PermissionEvent.RetryPermission) })
                OnboardingTextButton(text = AppStrings.ButtonNotNow, onClick = { onEvent(PermissionEvent.NextStep) })
            }
        }
    ) {
        Spacer(modifier = Modifier.height(Dimensions.SpacingGiant))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Surface(modifier = Modifier.size(80.dp), shape = CircleShape, color = MaterialTheme.colorScheme.errorContainer) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Rounded.PriorityHigh, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))
        Text(text = AppStrings.OnboardingAccessibilityFailureTitle, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
        Text(text = AppStrings.OnboardingAccessibilityFailureSubtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun UsageIntroStep(onEvent: (PermissionEvent) -> Unit) {
    OnboardingLayout(
        bottomBar = {
            Column {
                OnboardingActionButton(text = AppStrings.ButtonContinueSettings, onClick = { onEvent(PermissionEvent.NextStep) })
                OnboardingTextButton(text = AppStrings.ButtonNotNow, onClick = { onEvent(PermissionEvent.NextStep) })
            }
        }
    ) {
        Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            OnboardingIllustration(icon = Icons.Rounded.BarChart, color = Color(0xFF4CAF50))
        }
        Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
        Text(text = AppStrings.OnboardingUsageIntroTitle, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
        Text(text = AppStrings.OnboardingUsageIntroSubtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
        
        OnboardingIntroFeature(icon = Icons.Rounded.Visibility, text = AppStrings.OnboardingUsageIntro1)
        OnboardingIntroFeature(icon = Icons.Rounded.AutoGraph, text = AppStrings.OnboardingUsageIntro2)
        OnboardingIntroFeature(icon = Icons.Rounded.Lock, text = AppStrings.OnboardingUsageIntro3)
        OnboardingIntroFeature(icon = Icons.Rounded.VisibilityOff, text = AppStrings.OnboardingUsageIntro4)
    }
}

@Composable
private fun UsageGrantStep(onEvent: (PermissionEvent) -> Unit) {
    OnboardingLayout(
        topBar = { OnboardingPermissionIndicator(current = 2) },
        bottomBar = {
            OnboardingActionButton(text = AppStrings.ButtonOpenSettings, onClick = { onEvent(PermissionEvent.GrantPermission("usage")) })
        }
    ) {
        Spacer(modifier = Modifier.height(Dimensions.SpacingGiant))
        Text(text = AppStrings.OnboardingUsageGrantTitle, style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ) {
            Column(modifier = Modifier.padding(Dimensions.PaddingLarge), horizontalAlignment = Alignment.CenterHorizontally) {
                OnboardingIllustration(icon = Icons.Rounded.BarChart, color = Color(0xFF4CAF50))
                Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
                Text(text = AppStrings.OnboardingUsageGrantSubtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun UsageSuccessStep(onEvent: (PermissionEvent) -> Unit) {
    OnboardingLayout(
        bottomBar = {
            OnboardingActionButton(text = AppStrings.ButtonContinue, onClick = { onEvent(PermissionEvent.NextStep) })
        }
    ) {
        Spacer(modifier = Modifier.height(Dimensions.SpacingGiant))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Surface(modifier = Modifier.size(80.dp), shape = CircleShape, color = Color(0xFF4CAF50)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))
        Text(text = AppStrings.OnboardingUsageSuccessTitle, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
        Text(text = AppStrings.OnboardingUsageSuccessSubtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun UsageFailureStep(onEvent: (PermissionEvent) -> Unit) {
    OnboardingLayout(
        bottomBar = {
            Column {
                OnboardingActionButton(text = AppStrings.ButtonTryAgain, onClick = { onEvent(PermissionEvent.RetryPermission) })
                OnboardingTextButton(text = AppStrings.ButtonNotNow, onClick = { onEvent(PermissionEvent.NextStep) })
            }
        }
    ) {
        Spacer(modifier = Modifier.height(Dimensions.SpacingGiant))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Surface(modifier = Modifier.size(80.dp), shape = CircleShape, color = MaterialTheme.colorScheme.errorContainer) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Rounded.PriorityHigh, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))
        Text(text = AppStrings.OnboardingUsageFailureTitle, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
        Text(text = AppStrings.OnboardingUsageFailureSubtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun NotificationsIntroStep(onEvent: (PermissionEvent) -> Unit) {
    OnboardingLayout(
        bottomBar = {
            OnboardingActionButton(text = AppStrings.ButtonEnable, onClick = { onEvent(PermissionEvent.NextStep) })
        }
    ) {
        Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            OnboardingIllustration(icon = Icons.Rounded.Notifications, color = Color(0xFFFF9800))
        }
        Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
        Text(text = AppStrings.OnboardingNotificationsIntroTitle, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
        Text(text = AppStrings.OnboardingNotificationsIntroSubtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
        
        OnboardingIntroFeature(icon = Icons.Rounded.ChatBubbleOutline, text = AppStrings.OnboardingNotificationsIntro1)
        OnboardingIntroFeature(icon = Icons.Rounded.CloudUpload, text = AppStrings.OnboardingNotificationsIntro2)
        OnboardingIntroFeature(icon = Icons.Rounded.Lock, text = AppStrings.OnboardingNotificationsIntro3)
    }
}

@Composable
private fun NotificationsGrantStep(onEvent: (PermissionEvent) -> Unit) {
    OnboardingLayout(
        topBar = { OnboardingPermissionIndicator(current = 3) },
        bottomBar = {
            OnboardingActionButton(text = AppStrings.ButtonEnable, onClick = { onEvent(PermissionEvent.GrantPermission("notifications")) })
        }
    ) {
        Spacer(modifier = Modifier.height(Dimensions.SpacingGiant))
        Text(text = AppStrings.OnboardingNotificationsGrantTitle, style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ) {
            Column(modifier = Modifier.padding(Dimensions.PaddingLarge), horizontalAlignment = Alignment.CenterHorizontally) {
                OnboardingIllustration(icon = Icons.Rounded.Notifications, color = Color(0xFFFF9800))
                Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
                Text(text = AppStrings.OnboardingNotificationsGrantSubtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun AllSetStep(onOpenTimeline: () -> Unit) {
    OnboardingLayout(
        bottomBar = {
            OnboardingActionButton(text = AppStrings.ButtonOpenTimeline, onClick = onOpenTimeline)
        }
    ) {
        Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(contentAlignment = Alignment.Center) {
                Surface(modifier = Modifier.size(100.dp), shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.primary) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Rounded.Timeline, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(48.dp))
                    }
                }
                // Small success icons around the main one
                Icon(imageVector = Icons.Rounded.Favorite, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(24.dp).align(Alignment.TopEnd).offset(x = 12.dp, y = (-12).dp))
                Icon(imageVector = Icons.Rounded.BarChart, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp).align(Alignment.BottomStart).offset(x = (-12).dp, y = 12.dp))
            }
        }
        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))
        Text(text = AppStrings.OnboardingAllSetTitle, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(Dimensions.PaddingMedium))
        Text(text = AppStrings.OnboardingAllSetSubtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))
        
        OnboardingPermissionFeature(icon = Icons.Rounded.Lock, title = AppStrings.OnboardingAllSet1)
        OnboardingPermissionFeature(icon = Icons.Rounded.Smartphone, title = AppStrings.OnboardingAllSet2)
        OnboardingPermissionFeature(icon = Icons.Rounded.ToggleOn, title = AppStrings.OnboardingAllSet3)
    }
}
