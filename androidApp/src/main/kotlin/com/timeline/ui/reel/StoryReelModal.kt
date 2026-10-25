package com.timeline.ui.reel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timeline.domain.model.ReelSlide
import com.timeline.domain.model.ReelSlideKind
import com.timeline.domain.model.ReelSlideScreenshot
import com.timeline.presentation.ReelPlaybackState
import com.timeline.ui.theme.AppColors

/**
 * Full-screen Instagram Stories-style reel modal.
 *
 * Features:
 * - Segmented animated progress bars (one per slide)
 * - Tap-left / tap-right navigation zones (35/65 split)
 * - Hold-to-pause
 * - Share ticket overlay
 * - Optional shared-element morph from the source card
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun StoryReelModal(
    reelState: ReelPlaybackState,
    dateLabel: String,
    onClose: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleTts: () -> Unit,
    onToggleTicketShare: (Boolean) -> Unit,
    onPauseChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val slide = reelState.currentSlide ?: return
    val slideBg = Color(slide.bgColorHex)
    val slideFg = Color(slide.fgColorHex)
    val slideAc = Color(slide.accentColorHex)

    val containerShape = RoundedCornerShape(26.dp)

    // Animate the per-slide progress bar from 0→1 over 5 s
    val slideProgress = remember(reelState.currentSlideIndex) { Animatable(0f) }
    LaunchedEffect(reelState.currentSlideIndex, reelState.isPaused, reelState.showTicketShare) {
        if (reelState.isPaused || reelState.showTicketShare) {
            slideProgress.stop()
        } else {
            val remaining = (1f - slideProgress.value).coerceAtLeast(0f)
            val duration = (remaining * 5000).toInt()
            if (duration > 0) {
                slideProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = duration, easing = LinearEasing)
                )
            }
        }
    }

    val containerModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            modifier
                .fillMaxSize()
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(
                        key = "reel_card_container_${reelState.filter.name}"
                    ),
                    animatedVisibilityScope = animatedVisibilityScope,
                    enter = EnterTransition.None,
                    exit = ExitTransition.None,
                    clipInOverlayDuringTransition = OverlayClip(containerShape),
                    zIndexInOverlay = 1f,
                    boundsTransform = { _, _ ->
                        tween(durationMillis = 460, easing = FastOutSlowInEasing)
                    }
                )
                .clip(containerShape)
                .background(slideBg, containerShape)
        }
    } else {
        modifier
            .fillMaxSize()
            .clip(containerShape)
            .background(slideBg, containerShape)
    }

    Box(modifier = containerModifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(containerShape)
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            // ── Top bar: segmented progress + close + title ──────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Segmented progress bars
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    reelState.slides.forEachIndexed { index, _ ->
                        val fill = when {
                            index < reelState.currentSlideIndex -> 1f
                            index == reelState.currentSlideIndex -> slideProgress.value
                            else -> 0f
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(slideFg.copy(alpha = 0.25f))
                        ) {
                            if (fill > 0f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fill)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(slideFg)
                                )
                            }
                        }
                    }
                }

                // Close + segment title row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = slideFg,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    val titleModifier =
                        if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                            with(sharedTransitionScope) {
                                Modifier.sharedElement(
                                    sharedContentState = rememberSharedContentState(
                                        key = "reel_period_title_${reelState.filter.name}"
                                    ),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    boundsTransform = { _, _ ->
                                        tween(durationMillis = 460, easing = FastOutSlowInEasing)
                                    }
                                )
                            }
                        } else Modifier

                    Text(
                        text = reelState.segmentTitle,
                        color = slideFg,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = titleModifier
                    )
                }
            }

            // ── Interactive body: tap zones ──────────────────────────────
            val currentOnNext by rememberUpdatedState(onNext)
            val currentOnPrevious by rememberUpdatedState(onPrevious)
            val currentOnPauseChanged by rememberUpdatedState(onPauseChanged)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                currentOnPauseChanged(true)
                                tryAwaitRelease()
                                currentOnPauseChanged(false)
                            },
                            onTap = { offset ->
                                if (offset.x < size.width * 0.35f) currentOnPrevious()
                                else currentOnNext()
                            }
                        )
                    }
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                // OUTLIER sticker variant — tilted card
                if (slide.kind == ReelSlideKind.OUTLIER) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .rotate(-1.5f)
                            .shadow(16.dp, RoundedCornerShape(22.dp))
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color(0xFFFFFDF7))
                            .padding(20.dp)
                    ) {
                        SlideContent(
                            slide = slide,
                            slideFg = Color(0xFF1A1208),
                            slideAc = Color(0xFFF28A2E),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    SlideContent(
                        slide = slide,
                        slideFg = slideFg,
                        slideAc = slideAc,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // ── Bottom action bar ────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // TTS toggle
                IconButton(
                    onClick = onToggleTts,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (reelState.isTtsSpeaking) slideFg
                            else slideFg.copy(alpha = 0.15f)
                        )
                ) {
                    Icon(
                        imageVector = if (reelState.isTtsSpeaking) Icons.AutoMirrored.Rounded.VolumeUp
                        else Icons.AutoMirrored.Rounded.VolumeOff,
                        contentDescription = "Read aloud",
                        tint = if (reelState.isTtsSpeaking) slideBg else slideFg,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Share ticket
                IconButton(
                    onClick = { onToggleTicketShare(true) },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(slideFg.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = "Share",
                        tint = slideFg,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // ── Ticket share overlay ─────────────────────────────────────────
        AnimatedVisibility(
            visible = reelState.showTicketShare,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ReelHighlightTicketView(
                slide = slide,
                dateLabel = dateLabel,
                periodTitle = reelState.segmentTitle,
                onClose = { onToggleTicketShare(false) }
            )
        }
    }
}

// ── Slide content ────────────────────────────────────────────────────────────

@Composable
private fun SlideContent(
    slide: ReelSlide,
    slideFg: Color,
    slideAc: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Badge / key label
            when {
                slide.kind == ReelSlideKind.OUTLIER -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(slideAc.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = slideAc,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = slide.badgeTag ?: "Outlier",
                            color = slideAc,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                slide.keyLabel.isNotEmpty() -> {
                    val isCounter = slide.keyLabel.contains(" of ", ignoreCase = true)
                    if (!isCounter) {
                        Text(
                            text = slide.keyLabel,
                            color = slideFg.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Headline
            Text(
                text = slide.headline,
                color = slideFg,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 34.sp
            )
        }

        // Visual (stat circle or screenshot thumbnails)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (slide.kind == ReelSlideKind.STAT && slide.stat != null) {
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .clip(CircleShape)
                        .background(slideAc),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = slide.stat,
                            color = slideFg,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = slide.sub ?: "",
                            color = slideFg.copy(alpha = 0.85f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else if (slide.shots.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        10.dp,
                        Alignment.CenterHorizontally
                    )
                ) {
                    slide.shots.forEach { shot ->
                        ScreenshotThumbnail(shot = shot)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScreenshotThumbnail(shot: ReelSlideScreenshot, modifier: Modifier = Modifier) {
    val shotColor = Color(shot.colorHex)
    Box(
        modifier = modifier
            .width(96.dp)
            .height(160.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF18181B))
            .then(
                if (shot.isHighlighted)
                    Modifier.border(2.5.dp, AppColors.BrandOrange, RoundedCornerShape(16.dp))
                else
                    Modifier.border(
                        1.dp,
                        Color.White.copy(alpha = 0.15f),
                        RoundedCornerShape(16.dp)
                    )
            )
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // App icon row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(shotColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            shot.appName.contains(
                                "YouTube",
                                ignoreCase = true
                            ) -> Icons.Rounded.PlayArrow

                            shot.appName.contains(
                                "Amazon",
                                ignoreCase = true
                            ) -> Icons.Rounded.ShoppingBag

                            shot.appName.contains(
                                "Maps",
                                ignoreCase = true
                            ) -> Icons.Rounded.WbSunny

                            else -> Icons.Rounded.AutoAwesome
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = shot.appName,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Placeholder content lines
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.35f))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.25f))
                )
            }

            // Timestamp badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = shot.timestamp,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
