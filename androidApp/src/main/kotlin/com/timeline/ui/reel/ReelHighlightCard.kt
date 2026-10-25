package com.timeline.ui.reel

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timeline.domain.model.ReelSegment
import com.timeline.presentation.TimeOfDayFilter

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ReelHighlightCard(
    segment: ReelSegment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val accentColor = Color(segment.accentColor)
    val containerShape = RoundedCornerShape(26.dp)
    val cardBorder = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    val cardBackground = MaterialTheme.colorScheme.surfaceContainer

    val containerModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            modifier
                .fillMaxWidth()
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "reel_card_container_${segment.filter.name}"),
                    animatedVisibilityScope = animatedVisibilityScope,
                    enter = EnterTransition.None,
                    exit = ExitTransition.None,
                    clipInOverlayDuringTransition = OverlayClip(containerShape),
                    zIndexInOverlay = 0f,
                    boundsTransform = { _, _ ->
                        tween(durationMillis = 460, easing = FastOutSlowInEasing)
                    }
                )
                .border(cardBorder, containerShape)
                .clip(containerShape)
                .background(cardBackground)
                .clickable { onClick() }
        }
    } else {
        modifier
            .fillMaxWidth()
            .border(cardBorder, containerShape)
            .clip(containerShape)
            .background(cardBackground)
            .clickable { onClick() }
    }

    Box(modifier = containerModifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Period icon and Period title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getPeriodIcon(segment.filter),
                        contentDescription = segment.title,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                val titleModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                    with(sharedTransitionScope) {
                        Modifier.sharedElement(
                            sharedContentState = rememberSharedContentState(key = "reel_period_title_${segment.filter.name}"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = { _, _ ->
                                tween(durationMillis = 460, easing = FastOutSlowInEasing)
                            }
                        )
                    }
                } else Modifier

                Text(
                    text = segment.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = titleModifier
                )
            }

            // Body: Story narrative paragraphs
            val displayParagraphs = if (segment.paragraphs.isNotEmpty()) {
                segment.paragraphs
            } else if (segment.narrative.isNotBlank()) {
                listOf(segment.narrative)
            } else {
                emptyList()
            }

            if (displayParagraphs.isNotEmpty()) {
                val fullNarrative = displayParagraphs.joinToString("\n\n")
                Text(
                    text = fullNarrative,
                    color = if (segment.isUpcoming) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun getPeriodIcon(filter: TimeOfDayFilter): ImageVector {
    return when (filter) {
        TimeOfDayFilter.MORNING -> Icons.Rounded.WbSunny
        TimeOfDayFilter.AFTERNOON -> Icons.Rounded.WbTwilight
        TimeOfDayFilter.EVENING -> Icons.Rounded.Bedtime
        else -> Icons.Rounded.WbSunny
    }
}
