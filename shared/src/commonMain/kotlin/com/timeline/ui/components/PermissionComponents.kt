package com.timeline.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timeline.presentation.OnboardingStep
import com.timeline.presentation.PermissionEvent
import com.timeline.presentation.PermissionItem
import com.timeline.presentation.PermissionState
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import timeline.shared.generated.resources.Res
import timeline.shared.generated.resources.get_started_two
import timeline.shared.generated.resources.timeline_splash_icon

@Composable
fun OnboardingStepContent(
    step: OnboardingStep,
    state: PermissionState,
    onEvent: (PermissionEvent) -> Unit
) {
    when (step) {
        OnboardingStep.Opening -> OpeningStep(onEvent)
        OnboardingStep.Welcome -> WelcomeStep(onEvent)
        OnboardingStep.PermissionCardStack -> PermissionCardStackStep(state, onEvent)
    }
}

@Composable
fun OpeningStep(onEvent: (PermissionEvent) -> Unit) {
    val privacyUrl = "https://timeline-marketing-site--moyokamaal.replit.app/privacy"

    val linkStyle = TextLinkStyles(
        style = SpanStyle(
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            textDecoration = TextDecoration.Underline
        )
    )

    val annotatedString = buildAnnotatedString {
        append("By continuing, you agree to our ")
        withLink(LinkAnnotation.Url(url = privacyUrl, styles = linkStyle)) {
            append("Terms of Service")
        }
        append(" & ")
        withLink(LinkAnnotation.Url(url = privacyUrl, styles = linkStyle)) {
            append("Privacy Policy")
        }
        append(".")
    }

    val fullTitleText = AppStrings.OnboardingWelcomeTitle
    var displayedTitleText by remember { mutableStateOf("") }
    var titleTextIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(fullTitleText) {
        while (titleTextIndex < fullTitleText.length) {
            displayedTitleText = fullTitleText.substring(0, titleTextIndex + 1)
            titleTextIndex++
            delay(50L)
        }
    }

    var isTitleVisible by remember { mutableStateOf(false) }
    var isButtonVisible by remember { mutableStateOf(false) }
    var isFooterVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isTitleVisible = true
        delay(150L)
        isButtonVisible = true
        delay(150L)
        isFooterVisible = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.get_started_two),
            contentDescription = "Get Started bg",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        OnboardingLayout(
            bottomBar = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Dimensions.PaddingMedium)
                ) {
                    AnimatedVisibility(
                        visible = isButtonVisible,
                        enter = fadeIn(animationSpec = tween(durationMillis = 500)) +
                                slideInVertically(
                                    initialOffsetY = { it / 2 },
                                    animationSpec = tween(durationMillis = 500)
                                )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { onEvent(PermissionEvent.NextStep) },
                                modifier = Modifier
                                    .fillMaxWidth(1f)
                                    .height(Dimensions.ButtonHeight),
                                shape = com.timeline.ui.theme.AppShapes.Pill,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.onSurface,
                                    contentColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Text(
                                    text = "Get Started",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = isFooterVisible,
                        enter = fadeIn(animationSpec = tween(durationMillis = 500)) +
                                slideInVertically(
                                    initialOffsetY = { it / 2 },
                                    animationSpec = tween(durationMillis = 500)
                                )
                    ) {
                        Text(
                            text = annotatedString,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            ),
                            modifier = Modifier.padding(horizontal = Dimensions.PaddingSmall)
                        )
                    }
                }
            }
        ) {
            Column {
                Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))
                Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

                Row(modifier = Modifier.fillMaxWidth(1f)) {
                    Image(
                        painter = painterResource(Res.drawable.timeline_splash_icon),
                        contentDescription = "Splash Logo",
                        modifier = Modifier.clip(RoundedCornerShape(16.dp)).size(60.dp)
                    )
                    AnimatedVisibility(
                        visible = isTitleVisible,
                        enter = fadeIn(animationSpec = tween(durationMillis = 600)) +
                                slideInVertically(
                                    initialOffsetY = { -it / 2 },
                                    animationSpec = tween(durationMillis = 600)
                                )
                    ) {
                        Text(
                            text = displayedTitleText,
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))
            }
        }
    }
}

@Composable
fun WelcomeStep(onEvent: (PermissionEvent) -> Unit) {
    var isHeaderVisible by remember { mutableStateOf(false) }
    var isTextVisible by remember { mutableStateOf(false) }
    var isFeaturesVisible by remember { mutableStateOf(false) }
    var isBottomBarVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isHeaderVisible = true
        delay(100L)
        isTextVisible = true
        delay(150L)
        isFeaturesVisible = true
        delay(150L)
        isBottomBarVisible = true
    }

    OnboardingLayout(
        bottomBar = {
            AnimatedVisibility(
                visible = isBottomBarVisible,
                enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(500)
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Dimensions.PaddingMedium)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OnboardingStepIndicator(total = 2, current = 0)
                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = { onEvent(PermissionEvent.NextStep) },
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(Dimensions.ButtonHeight),
                            shape = com.timeline.ui.theme.AppShapes.Pill,
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
    ) {
        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

        AnimatedVisibility(
            visible = isHeaderVisible,
            enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
                initialOffsetY = { -it / 2 },
                animationSpec = tween(500)
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.Timeline,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

        AnimatedVisibility(
            visible = isTextVisible,
            enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = tween(500)
            )
        ) {
            Column {
                Text(
                    text = AppStrings.OnboardingWelcomeTitle,
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))
                Text(
                    text = AppStrings.OnboardingWelcomeSubtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimensions.SpacingLarge))

        AnimatedVisibility(
            visible = isFeaturesVisible,
            enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = tween(500)
            )
        ) {
            Column {
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
    }
}

@Composable
private fun ValuePropFeature(icon: ImageVector, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.PaddingSmall),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(Dimensions.PaddingMedium))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun PermissionCardStackStep(
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        AnimatedVisibility(
            visible = isTitleVisible,
            enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
                initialOffsetY = { -it / 2 },
                animationSpec = tween(500)
            ),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(
                        horizontal = Dimensions.PaddingLarge,
                        vertical = Dimensions.PaddingMedium
                    )
            ) {
                Text(
                    text = AppStrings.OnboardingStackTitle,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(Dimensions.PaddingSmall))
                Text(
                    text = AppStrings.OnboardingStackSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

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

        AnimatedVisibility(
            visible = isControlsVisible,
            enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = tween(500)
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(Dimensions.PaddingLarge)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Dimensions.PaddingMedium)
                ) {
                    OnboardingStepIndicator(total = 2, current = 1)

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
        }

        if (state.activeCardIndex >= state.permissions.size) {
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
    offsetY: androidx.compose.ui.unit.Dp
) {
    val cardHeight = when (index) {
        0 -> 460.dp
        1 -> 520.dp
        else -> 580.dp
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight)
            .offset(y = offsetY),
        shape = RoundedCornerShape(
            topStart = 40.dp,
            topEnd = 40.dp,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        ),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = ((3 - index) * 4).dp,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
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

            Spacer(modifier = Modifier.height(160.dp))
        }
    }
}