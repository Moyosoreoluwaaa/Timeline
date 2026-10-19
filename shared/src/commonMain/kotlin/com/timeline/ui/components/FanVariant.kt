package com.timeline.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
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
    val isLast = page == INTRO_PAGE_COUNT - 1

    Column(
        modifier = modifier.fillMaxSize()
    ) {
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