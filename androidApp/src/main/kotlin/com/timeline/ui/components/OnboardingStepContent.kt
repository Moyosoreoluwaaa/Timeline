package com.timeline.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timeline.R
import com.timeline.presentation.INTRO_PAGE_COUNT
import com.timeline.presentation.OnboardingStep
import com.timeline.presentation.PermissionEvent
import com.timeline.presentation.PermissionState
import com.timeline.ui.theme.AppAlpha
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun OnboardingStepContent(
    step: OnboardingStep,
    state: PermissionState,
    onEvent: (PermissionEvent) -> Unit
) {
    when (step) {
        OnboardingStep.Opening -> OpeningStep(onEvent)
        OnboardingStep.Intro -> IntroCarouselStep(
            page = state.introPage,
            totalSteps = INTRO_PAGE_COUNT + 1 + state.permissions.size,
            focusAreas = state.focusAreas,
            onEvent = onEvent,
            variant = FanVariant.CenteredFan
        )
        OnboardingStep.ThreePermissions -> ThreePermissionsStep(onEvent)
        OnboardingStep.PermissionCardStack -> PermissionCardStackStep(state, onEvent)
        OnboardingStep.Done -> DoneStep()
    }
}

@Composable
fun ThreePermissionsStep(onEvent: (PermissionEvent) -> Unit) {
    val palette = introPalette
    val scrollState = rememberScrollState()

    // Animation: three dots or segments around the logo light up one by one as the rows appear,
    // and the logo glows when all three are lit.
    var rowsVisible by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        delay(200L.milliseconds)
        rowsVisible = 1
        delay(350L.milliseconds)
        rowsVisible = 2
        delay(350L.milliseconds)
        rowsVisible = 3
    }

    val glowAlpha by animateFloatAsState(
        targetValue = if (rowsVisible >= 3) 0.55f else 0f,
        animationSpec = onboardingTween(400),
        label = "LogoGlowAlpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 12.dp)
                // Bottom padding to ensure content scrolls above the overlay button area
                .padding(bottom = 120.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Logo with animated dots around it and radial glow when all 3 lit
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .drawBehind {
                            if (glowAlpha > 0.01f) {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            palette.accent.copy(alpha = glowAlpha),
                                            Color.Transparent
                                        ),
                                        center = Offset(size.width / 2f, size.height / 2f),
                                        radius = size.width * 0.75f
                                    )
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Logo
                    Image(
                        painter = painterResource(R.drawable.timeline_splash_icon),
                        contentDescription = null,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )

                    // 3 indicator dots/segments placed around the logo (top, bottom-left, bottom-right)
                    // Radius ~44dp from center
                    val dotPositions: List<Modifier> = listOf(
                        Modifier.align(Alignment.TopCenter).offset(y = 4.dp),
                        Modifier.align(Alignment.BottomStart).offset(x = 14.dp, y = (-4).dp),
                        Modifier.align(Alignment.BottomEnd).offset(x = (-14).dp, y = (-4).dp)
                    )

                    dotPositions.forEachIndexed { index, posModifier ->
                        val isLit = rowsVisible > index
                        val dotColor by androidx.compose.animation.animateColorAsState(
                            targetValue = if (isLit) palette.accent else palette.textSecondary.copy(alpha = 0.25f),
                            animationSpec = onboardingTween(300),
                            label = "DotColor_$index"
                        )
                        val dotScale by animateFloatAsState(
                            targetValue = if (isLit) 1.25f else 1f,
                            animationSpec = onboardingTween(300),
                            label = "DotScale_$index"
                        )

                        Box(
                            modifier = posModifier
                                .size((8 * dotScale).dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }
            }

            // Headline & Subline
            Text(
                text = AppStrings.ThreePermissionsTitle,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = palette.textPrimary,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = AppStrings.ThreePermissionsSubtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = palette.textSecondary,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Three rows: Accessibility, Usage access, Notifications
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PermissionExplanationRow(
                    icon = Icons.Rounded.AccessibilityNew,
                    title = "Accessibility",
                    job = AppStrings.PermAccessibilityJob,
                    limit = AppStrings.PermAccessibilityLimit,
                    isVisible = rowsVisible >= 1,
                    palette = palette
                )

                PermissionExplanationRow(
                    icon = Icons.Rounded.BarChart,
                    title = "Usage access",
                    job = AppStrings.PermUsageJob,
                    limit = null,
                    isVisible = rowsVisible >= 2,
                    palette = palette
                )

                PermissionExplanationRow(
                    icon = Icons.Rounded.Notifications,
                    title = "Notifications",
                    job = AppStrings.PermNotificationsJob,
                    limit = AppStrings.PermNotificationsLimit,
                    isVisible = rowsVisible >= 3,
                    palette = palette
                )
            }
        }

        // Overlay setup button with gradient background at the bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            palette.background.copy(alpha = 0.85f),
                            palette.background
                        )
                    )
                )
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { onEvent(PermissionEvent.NextStep) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.ButtonHeight),
                shape = com.timeline.ui.theme.AppShapes.Pill,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Text(
                    text = AppStrings.ThreePermissionsButton,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Small line under button
            Text(
                text = AppStrings.ThreePermissionsFootnote,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = palette.textSecondary.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PermissionExplanationRow(
    icon: ImageVector,
    title: String,
    job: String,
    limit: String?,
    isVisible: Boolean,
    palette: IntroPalette
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = onboardingTween(250)) +
                slideInVertically(
                    initialOffsetY = { it / 3 },
                    animationSpec = onboardingTween(250)
                )
    ) {
        // Matches TimelineEntry card background: MaterialTheme.colorScheme.surfaceVariant.copy(alpha = AppAlpha.Divider)
        val cardBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = AppAlpha.Divider)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(cardBg)
                .border(0.5.dp, palette.stroke, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(palette.accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = palette.accent,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.size(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = palette.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                val description = if (limit != null) "$job $limit" else job
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun DoneStep() {
    val palette = introPalette
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.PaddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(palette.accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Success",
                    tint = palette.accent,
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "You're All Set!",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = palette.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Setting up your timeline experience...",
                style = MaterialTheme.typography.bodyLarge,
                color = palette.textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun OpeningStep(onEvent: (PermissionEvent) -> Unit) {
    val privacyUrl = "https://v0-timeline-app.vercel.app/privacy"
    val termsUrl = "https://v0-timeline-app.vercel.app/terms"

    val linkStyle = TextLinkStyles(
        style = SpanStyle(
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            textDecoration = TextDecoration.Underline
        )
    )

    val annotatedString = buildAnnotatedString {
        append("By continuing, you agree to our ")
        withLink(LinkAnnotation.Url(url = termsUrl, styles = linkStyle)) {
            append("Terms of Service")
        }
        append(" & ")
        withLink(LinkAnnotation.Url(url = privacyUrl, styles = linkStyle)) {
            append("Privacy Policy")
        }
        append(".")
    }

    val fullTitleText = AppStrings.OnboardingWelcomeTitle
    var titleTextIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(fullTitleText) {
        while (titleTextIndex < fullTitleText.length) {
            titleTextIndex++
            delay(50L.milliseconds)
        }
    }

    var isTitleVisible by remember { mutableStateOf(false) }
    var isButtonVisible by remember { mutableStateOf(false) }
    var isFooterVisible by remember { mutableStateOf(false) }

    var isImageVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isImageVisible = true
        delay(150L.milliseconds)
        isTitleVisible = true
        delay(150L.milliseconds)
        isButtonVisible = true
        delay(150L.milliseconds)
        isFooterVisible = true
    }

    val revealedTitle = buildAnnotatedString {
        append(fullTitleText.take(titleTextIndex))
        withStyle(SpanStyle(color = Color.Transparent)) {
            append(fullTitleText.drop(titleTextIndex))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = isImageVisible,
            enter = fadeIn(animationSpec = onboardingTween(350)) +
                    slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = onboardingTween(350)
                    ),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 100.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.artifacts_options),
                contentDescription = "Get Started bg",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(320.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(
                    start = Dimensions.PaddingLarge,
                    end = Dimensions.PaddingLarge,
                    bottom = Dimensions.PaddingLarge
                ),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(Dimensions.PaddingMedium)
        ) {
            AnimatedVisibility(
                visible = isTitleVisible,
                enter = fadeIn(animationSpec = onboardingTween(250)) +
                        slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = onboardingTween(250)
                        )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Image(
                        painter = painterResource(R.drawable.timeline_splash_icon),
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = revealedTitle,
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            AnimatedVisibility(
                visible = isButtonVisible,
                enter = fadeIn(animationSpec = onboardingTween(250)) +
                        slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = onboardingTween(250)
                        )
            ) {
                Button(
                    onClick = { onEvent(PermissionEvent.NextStep) },
                    modifier = Modifier
                        .fillMaxWidth()
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

            AnimatedVisibility(
                visible = isFooterVisible,
                enter = fadeIn(animationSpec = onboardingTween(250)) +
                        slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = onboardingTween(250)
                        )
            ) {
                Text(
                    text = annotatedString,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Start,
                        lineHeight = 16.sp
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}