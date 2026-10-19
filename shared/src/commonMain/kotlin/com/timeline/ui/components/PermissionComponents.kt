package com.timeline.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.timeline.presentation.INTRO_PAGE_COUNT
import com.timeline.presentation.PermissionEvent
import com.timeline.presentation.PermissionItem
import com.timeline.presentation.PermissionState
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.delay

@Composable
fun PermissionCardStackStep(
    state: PermissionState,
    onEvent: (PermissionEvent) -> Unit
) {
    val currentPermission = state.permissions.getOrNull(state.activeCardIndex)

    var isTitleVisible by remember { mutableStateOf(false) }
    var isControlsVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isTitleVisible = true
        delay(150L)
        isControlsVisible = true
    }

    val exitEasing = remember { CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f) }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val screenHeight = maxHeight
        AnimatedVisibility(
            visible = isTitleVisible,
            enter = fadeIn(onboardingTween(250)) + slideInVertically(
                initialOffsetY = { -it / 2 },
                animationSpec = onboardingTween(250)
            ),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = Dimensions.PaddingLarge,
                        vertical = Dimensions.PaddingMedium
                    )
            ) {
                Text(
                    text = AppStrings.OnboardingStackTitle,
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))
                Text(
                    text = AppStrings.OnboardingStackSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            val baseCardHeight = 440.dp
            val peekStep = 18.dp

            state.permissions.asReversed().forEachIndexed { indexFromEnd, permission ->
                val actualIndex = state.permissions.size - 1 - indexFromEnd
                val isVisible = actualIndex >= state.activeCardIndex
                val cardHeight = baseCardHeight + (peekStep * actualIndex)

                val offsetY by animateDpAsState(
                    targetValue = if (isVisible) 0.dp else 550.dp,
                    animationSpec = if (isVisible) {
                        spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    } else {
                        tween(durationMillis = 250, easing = exitEasing)
                    },
                    label = "CardOffsetY_$actualIndex"
                )

                if (offsetY < 550.dp) {
                    PermissionCard(
                        permission = permission,
                        index = actualIndex,
                        cardHeight = cardHeight,
                        screenHeight = screenHeight,
                        offsetY = { offsetY }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isControlsVisible,
            enter = fadeIn(animationSpec = onboardingTween(250)) + slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = onboardingTween(250)
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.PaddingLarge)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(Dimensions.PaddingMedium)
                ) {
                    if (currentPermission != null) {
                        OnboardingActionButton(
                            text = if (currentPermission.isGranted) AppStrings.ButtonAlreadyGranted else AppStrings.ButtonGrantAccess,
                            onClick = {
                                if (currentPermission.isGranted) {
                                    onEvent(PermissionEvent.NextStep)
                                } else {
                                    onEvent(PermissionEvent.GrantPermission(currentPermission.id))
                                }
                            }
                        )
                    }
                }
            }
        }

        if (state.activeCardIndex >= state.permissions.size && state.permissions.isNotEmpty()) {
            LaunchedEffect(Unit) {
                onEvent(PermissionEvent.NextStep)
            }
        }
    }
}

@Composable
private fun PermissionCard(
    permission: PermissionItem,
    index: Int,
    cardHeight: Dp,
    screenHeight: Dp,
    offsetY: () -> Dp
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(top = screenHeight - cardHeight + offsetY()),
        shape = RoundedCornerShape(
            topStart = 32.dp,
            topEnd = 32.dp,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        ),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.PaddingLarge),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                OnboardingIllustration(
                    icon = when (permission.id) {
                        "accessibility" -> Icons.Rounded.AccessibilityNew
                        "usage" -> Icons.Rounded.BarChart
                        else -> Icons.Rounded.Notifications
                    },
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(Dimensions.PaddingLarge))
            Text(
                text = permission.title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))
            Text(
                text = permission.description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}