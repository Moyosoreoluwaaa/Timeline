package com.timeline.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timeline.presentation.OnboardingStep
import com.timeline.presentation.PermissionEvent
import com.timeline.presentation.PermissionItem
import com.timeline.presentation.PermissionState
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings


@Composable
fun OnboardingStepContent(
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
        OnboardingStep.PlanSelection -> PlanSelectionStep(onOpenTimeline)
    }
}

@Composable
private fun PlanSelectionStep(onOpenTimeline: () -> Unit) {
    // Placeholder for new Plan Selection UI
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Choose your plan")
        Button(onClick = onOpenTimeline) { Text("Continue") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModeSelectionStep(
    onEvent: (PermissionEvent) -> Unit,
    onOpenTimeline: () -> Unit,
    onNavigateToPaywall: () -> Unit
) {
    var selectedMode by remember { mutableStateOf("Balanced") }
    var sliderValue by remember { mutableStateOf(3f) }

    val activeColor = Color(0xFFFF6D00)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Top Skip Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                TextButton(onClick = onOpenTimeline) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Skip",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Main Content Area
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(top = 56.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Settings Card Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // 1. Reasoning mode section
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = activeColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Reasoning mode",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Text(
                                text = "How detailed should the context be?",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        // Segmented Button Row for Reasoning Mode
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val modes = listOf("Concise", "Balanced", "Explanatory")
                                modes.forEach { mode ->
                                    val isSelected = selectedMode == mode
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) activeColor
                                                else Color.Transparent
                                            )
                                            .clickable {
                                                selectedMode = mode
                                                if (mode == "Concise") {
                                                    onEvent(PermissionEvent.SetReasoningMode(com.timeline.domain.reasoning.HighlightReasoningMode.CONCISE))
                                                } else if (mode == "Balanced") {
                                                    onEvent(PermissionEvent.SetReasoningMode(com.timeline.domain.reasoning.HighlightReasoningMode.BALANCED))
                                                } else if (mode == "Explanatory") {
                                                    onEvent(PermissionEvent.SetReasoningMode(com.timeline.domain.reasoning.HighlightReasoningMode.EXPLANATORY))
                                                    onNavigateToPaywall()
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = mode,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            thickness = 1.dp
                        )

                        // 2. Highlights frequency section
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Schedule,
                                        contentDescription = null,
                                        tint = activeColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Highlights frequency",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = activeColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${sliderValue.toInt()}× – ${sliderValue.toInt() * 2}× daily",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = activeColor,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Text(
                                text = "How often to generate Highlights",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Interactive Slider for Frequency
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Slider(
                                    value = sliderValue,
                                    onValueChange = {
                                        sliderValue = it
                                        onEvent(PermissionEvent.SetDigestFrequency(it.toInt()))
                                    },
                                    valueRange = 1f..6f,
                                    steps = 4,
                                    colors = SliderDefaults.colors(
                                        thumbColor = activeColor,
                                        activeTrackColor = activeColor,
                                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "1× daily",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    Text(
                                        text = "6× daily",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Title and step indicators
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Make Timeline\nwork your way",
                        style = MaterialTheme.typography.displaySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 36.sp
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(4) { index ->
                            Box(
                                modifier = Modifier
                                    .size(if (index == 3) 8.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (index == 3) MaterialTheme.colorScheme.onSurface
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                    )
                            )
                        }
                    }
                }
            }

            // Bottom Continue Button Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(24.dp)
            ) {
                Button(
                    onClick = onOpenTimeline,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
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
                    text = AppStrings.PermissionAccessibilityPrivacy,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))
                Text(
                    text = AppStrings.PermissionAccessibilityPrivacyDesc,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
                OnboardingActionButton(
                    text = AppStrings.ButtonUnderstandContinue,
                    onClick = {
                        showPrivacySheet = false
                        onEvent(PermissionEvent.GrantPermission("accessibility"))
                    }
                )
                OnboardingTextButton(
                    text = AppStrings.ButtonCancel,
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
