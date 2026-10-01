package com.timeline.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timeline.util.AppStrings

fun <T> onboardingTween(durationMillis: Int = 0): FiniteAnimationSpec<T> =
    spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )

enum class PagerStyle { SegmentedBar, PillDots }

val OnboardingPagerStyle = PagerStyle.SegmentedBar

@Composable
fun OnboardingTopBar(
    current: Int,
    total: Int,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
    style: PagerStyle = OnboardingPagerStyle,
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp)
    ) {
        if (style == PagerStyle.SegmentedBar) {
            OnboardingSegmentedProgress(current, total)
            Spacer(Modifier.height(8.dp))
        }
        Text(
            text = AppStrings.ButtonSkip,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.End)
                .clip(RoundedCornerShape(8.dp))
                .clickable(role = Role.Button, onClick = onSkip)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun OnboardingSegmentedProgress(current: Int, total: Int, modifier: Modifier = Modifier) {
    val filled = MaterialTheme.colorScheme.onSurface
    val empty = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)
    Row(
        modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = "Step ${current + 1} of $total" },
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(total) { index ->
            val color by animateColorAsState(
                targetValue = if (index <= current) filled else empty,
                label = "segment"
            )
            Box(
                Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}

@Composable
fun OnboardingPillDots(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier,
    alignment: Alignment.Horizontal = Alignment.CenterHorizontally,
) {
    val active = MaterialTheme.colorScheme.primary
    val inactive = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
    Row(
        modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = "Step ${current + 1} of $total" },
        horizontalArrangement = Arrangement.spacedBy(5.dp, alignment),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { index ->
            val selected = index == current
            val width by animateDpAsState(if (selected) 18.dp else 5.dp, label = "dotWidth")
            val color by animateColorAsState(if (selected) active else inactive, label = "dotColor")
            Box(
                Modifier
                    .width(width)
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}