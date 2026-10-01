package com.timeline.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
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
import com.timeline.presentation.INTRO_PAGE_COUNT
import com.timeline.presentation.OnboardingStep
import com.timeline.presentation.PermissionEvent
import com.timeline.presentation.PermissionState
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import timeline.shared.generated.resources.Res
import timeline.shared.generated.resources.artifacts_options
import timeline.shared.generated.resources.timeline_splash_icon
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
            totalSteps = INTRO_PAGE_COUNT + state.permissions.size,
            focusAreas = state.focusAreas,
            onEvent = onEvent,
            variant = FanVariant.CenteredFan
        )

        OnboardingStep.PermissionCardStack -> PermissionCardStackStep(state, onEvent)
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
    var displayedTitleText by remember { mutableStateOf("") }
    var titleTextIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(fullTitleText) {
        while (titleTextIndex < fullTitleText.length) {
            displayedTitleText = fullTitleText.substring(0, titleTextIndex + 1)
            titleTextIndex++
            delay(50L.milliseconds)
        }
    }

    var isTitleVisible by remember { mutableStateOf(false) }
    var isButtonVisible by remember { mutableStateOf(false) }
    var isFooterVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isTitleVisible = true
        delay(150L.milliseconds)
        isButtonVisible = true
        delay(150L.milliseconds)
        isFooterVisible = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.artifacts_options),
            contentDescription = "Get Started bg",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.Center)
        )
        Spacer(modifier = Modifier.height(40.dp))

        OnboardingLayout(
            bottomBar = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Dimensions.PaddingMedium)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.timeline_splash_icon),
                            contentDescription = "Splash Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(65.dp)
                                .clip(RoundedCornerShape(20))
                                .padding(12.dp)
                        )

                        AnimatedVisibility(
                            visible = isTitleVisible,
                            enter = fadeIn(animationSpec = onboardingTween()) +
                                    slideInVertically(
                                        initialOffsetY = { -it / 2 },
                                        animationSpec = onboardingTween()
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

                    Spacer(modifier = Modifier.height(40.dp))

                    AnimatedVisibility(
                        visible = isButtonVisible,
                        enter = fadeIn(animationSpec = onboardingTween()) +
                                slideInVertically(
                                    initialOffsetY = { it / 2 },
                                    animationSpec = onboardingTween()
                                )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
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
                    }

                    AnimatedVisibility(
                        visible = isFooterVisible,
                        enter = fadeIn(animationSpec = onboardingTween()) +
                                slideInVertically(
                                    initialOffsetY = { it / 2 },
                                    animationSpec = onboardingTween()
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
        ) { }
    }
}