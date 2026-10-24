package com.timeline.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun IntroFooter(
    page: Int,
    totalSteps: Int,
    variant: FanVariant,
    pagerStyle: PagerStyle,
    buttonLabel: String,
    onNext: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.Start
    ) {
        AnimatedContent(
            targetState = page,
            transitionSpec = {
                val forward = targetState > initialState
                val enter = fadeIn(onboardingTween(250)) + slideInVertically(
                    animationSpec = onboardingTween(250)
                ) { if (forward) it / 6 else -it / 6 }
                val exit = fadeOut(onboardingTween(150)) + slideOutVertically(
                    animationSpec = onboardingTween(150)
                ) { if (forward) -it / 6 else it / 6 }

                (enter togetherWith exit).using(SizeTransform(clip = false))
            },
            label = "IntroCopy"
        ) { p ->
            val copy = IntroPages[p.coerceIn(0, IntroPages.lastIndex)]
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = copy.title,
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = introPalette.textPrimary,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = copy.subtitle,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = introPalette.textSecondary,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (pagerStyle == PagerStyle.PillDots) {
            Spacer(Modifier.height(16.dp))
            OnboardingPillDots(
                current = page,
                total = totalSteps,
                alignment = Alignment.Start
            )
            Spacer(Modifier.height(16.dp))
        } else {
            Spacer(Modifier.height(20.dp))
        }

        IntroButton(variant, buttonLabel, onNext)
    }
}