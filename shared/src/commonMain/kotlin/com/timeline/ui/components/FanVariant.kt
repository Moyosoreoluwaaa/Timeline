package com.timeline.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.timeline.presentation.INTRO_PAGE_COUNT
import com.timeline.presentation.PermissionEvent
import com.timeline.util.AppStrings

/**
 * Main onboarding intro step carousel hosting the active [FanVariant] for A/B testing experimentation.
 */
@Composable
fun IntroCarouselStep(
    page: Int,
    totalSteps: Int,
    focusAreas: Set<String>,
    onEvent: (PermissionEvent) -> Unit,
    modifier: Modifier = Modifier,
    variant: FanVariant = FanVariant.CenteredFan,
    pagerStyle: PagerStyle = OnboardingPagerStyle,
) {
    val scheme = MaterialTheme.colorScheme
    val palette = remember(scheme) { IntroPalette.from(scheme) }
    val isLast = page == INTRO_PAGE_COUNT - 1

    CompositionLocalProvider(LocalIntroPalette provides palette) {
        Box(
            modifier
                .fillMaxSize()
                .background(palette.background)
                .backdropGlow(variant, palette.accent, softer = palette.isLight)
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                OnboardingTopBar(
                    current = page,
                    total = totalSteps,
                    style = pagerStyle,
                    onSkip = { onEvent(PermissionEvent.SkipIntro) }
                )

                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    when (variant) {
                        FanVariant.CenteredFan -> CardFan(
                            CenteredFanSpec,
                            page,
                            focusAreas,
                            onEvent
                        )

                        FanVariant.CardHand -> CardFan(CardHandSpec, page, focusAreas, onEvent)
                        FanVariant.ScreenshotWall -> ScreenshotWall(page, focusAreas, onEvent)
                    }
                }

                IntroFooter(
                    page = page,
                    totalSteps = totalSteps,
                    variant = variant,
                    pagerStyle = pagerStyle,
                    buttonLabel = if (isLast) AppStrings.ButtonContinue else AppStrings.ButtonNext,
                    onNext = { onEvent(PermissionEvent.NextStep) }
                )
            }
        }
    }
}