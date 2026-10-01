package com.timeline.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Photo
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.WbCloudy
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timeline.presentation.INTRO_PAGE_COUNT
import com.timeline.presentation.PermissionEvent
import com.timeline.ui.theme.AppShapes
import com.timeline.ui.theme.Dimensions
import com.timeline.util.AppStrings
import org.jetbrains.compose.resources.painterResource
import timeline.shared.generated.resources.Res
import timeline.shared.generated.resources.artifacts_options
import timeline.shared.generated.resources.cards_around
import timeline.shared.generated.resources.on_balance
import timeline.shared.generated.resources.on_fence
import timeline.shared.generated.resources.open_hand
import timeline.shared.generated.resources.secure_lock

enum class FanVariant { CenteredFan, CardHand, ScreenshotWall }

val IntroFocusAreas = listOf("Concise", "Balanced", "Explanatory")

private const val HIGHLIGHT_PAGE = 1
private const val EXCLUSION_PAGE = 2     // "Page 3"
private const val NOTIFICATION_PAGE = 3  // "Page 4"
private const val FOCUS_PAGE = 4         // "Page 5"

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

@Immutable
private class IntroPalette(
    val background: Color,
    val card: Color,
    val shot: Color,
    val stroke: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val accentSoft: Color,
    val onAccent: Color,
    val ink: Color,
    val isLight: Boolean,
) {
    companion object {
        fun from(c: ColorScheme) = IntroPalette(
            background = c.background,
            card = c.surfaceContainer,
            shot = c.surfaceContainerLow,
            stroke = c.outlineVariant.copy(alpha = 0.6f),
            textPrimary = c.onSurface,
            textSecondary = c.onSurfaceVariant,
            accent = c.primary,
            accentSoft = c.primary.copy(alpha = 0.14f),
            onAccent = c.onPrimary,
            ink = c.onSurface,
            isLight = c.background.luminance() > 0.5f,
        )
    }
}

private val LocalIntroPalette = staticCompositionLocalOf<IntroPalette> {
    error("IntroPalette not provided")
}

private val palette: IntroPalette
    @Composable get() = LocalIntroPalette.current

private const val MOCK_SCALE = 1.6f
private val Int.m: Dp get() = (this * MOCK_SCALE).dp

private data class IntroCopy(val title: String, val subtitle: String)

private val IntroPages = listOf(
    IntroCopy(AppStrings.IntroPage1Title, AppStrings.IntroPage1Subtitle),
    IntroCopy(AppStrings.IntroPage2Title, AppStrings.IntroPage2Subtitle),
    IntroCopy(AppStrings.IntroPage3Title, AppStrings.IntroPage3Subtitle),
    IntroCopy(AppStrings.IntroPage4Title, AppStrings.IntroPage4Subtitle),
    IntroCopy(AppStrings.IntroPage5Title, AppStrings.IntroPage5Subtitle),
)

private fun Modifier.backdropGlow(variant: FanVariant, color: Color, softer: Boolean): Modifier {
    val k = if (softer) 0.55f else 1f
    return when (variant) {
        FanVariant.CenteredFan -> radialGlow(color, alpha = 0.30f * k, centerYFraction = 0.34f)
        FanVariant.CardHand -> radialGlow(color, alpha = 0.38f * k, centerYFraction = 1.0f)
        FanVariant.ScreenshotWall -> this
    }
}

private fun Modifier.radialGlow(color: Color, alpha: Float, centerYFraction: Float): Modifier =
    drawBehind {
        drawRect(
            Brush.radialGradient(
                colors = listOf(color.copy(alpha = alpha), Color.Transparent),
                center = Offset(size.width / 2f, size.height * centerYFraction),
                radius = size.maxDimension * 0.62f
            )
        )
    }

@Composable
private fun IntroFooter(
    page: Int,
    totalSteps: Int,
    variant: FanVariant,
    pagerStyle: PagerStyle,
    buttonLabel: String,
    onNext: () -> Unit,
) {
    val leftAligned = variant == FanVariant.ScreenshotWall
    val horizontal = if (leftAligned) Alignment.Start else Alignment.CenterHorizontally
    val textAlign = if (leftAligned) TextAlign.Start else TextAlign.Center

    val headlineStyle = when (variant) {
        FanVariant.CenteredFan -> TextStyle(
            fontSize = 26.sp,
            lineHeight = 31.sp,
            fontWeight = FontWeight.Medium
        )

        FanVariant.CardHand -> TextStyle(
            fontSize = 28.sp,
            lineHeight = 33.sp,
            fontFamily = FontFamily.Serif
        )

        FanVariant.ScreenshotWall -> TextStyle(
            fontSize = 28.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = (-0.3).sp
        )
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 20.dp),
        horizontalAlignment = horizontal
    ) {
        if (leftAligned) {
            Box(
                Modifier
                    .width(24.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(palette.accent)
            )
            Spacer(Modifier.height(10.dp))
        }

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
            Column(horizontalAlignment = horizontal) {
                Text(
                    text = copy.title,
                    style = headlineStyle,
                    color = palette.textPrimary,
                    textAlign = textAlign
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = copy.subtitle,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = palette.textSecondary,
                    textAlign = textAlign
                )
            }
        }

        if (pagerStyle == PagerStyle.PillDots) {
            Spacer(Modifier.height(16.dp))
            OnboardingPillDots(
                current = page,
                total = totalSteps,
                alignment = if (leftAligned) Alignment.Start else Alignment.CenterHorizontally
            )
            Spacer(Modifier.height(16.dp))
        } else {
            Spacer(Modifier.height(20.dp))
        }

        IntroButton(variant, buttonLabel, onNext)
    }
}

@Composable
private fun ButtonLabel(text: String) {
    AnimatedContent(
        targetState = text,
        transitionSpec = {
            fadeIn(onboardingTween(200)) togetherWith fadeOut(onboardingTween(150))
        },
        label = "ButtonLabelTransition"
    ) { targetText ->
        Text(
            text = targetText,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun IntroButton(variant: FanVariant, label: String, onClick: () -> Unit) {
    val buttonModifier = Modifier
        .fillMaxWidth()
        .height(Dimensions.ButtonHeight)

    when (variant) {
        FanVariant.CenteredFan -> Button(
            onClick = onClick,
            modifier = buttonModifier,
            shape = AppShapes.Pill,
            colors = ButtonDefaults.buttonColors(
                containerColor = palette.ink,
                contentColor = palette.background
            )
        ) { ButtonLabel(label) }

        FanVariant.CardHand -> Button(
            onClick = onClick,
            modifier = buttonModifier,
            shape = AppShapes.Pill,
            colors = ButtonDefaults.buttonColors(
                containerColor = palette.accent,
                contentColor = palette.onAccent
            )
        ) { ButtonLabel(label) }

        FanVariant.ScreenshotWall -> Button(
            onClick = onClick,
            modifier = buttonModifier,
            shape = AppShapes.Pill,
            colors = ButtonDefaults.buttonColors(
                containerColor = palette.card,
                contentColor = palette.textPrimary
            ),
            border = BorderStroke(1.dp, palette.stroke),
            contentPadding = PaddingValues(start = 22.dp, end = 6.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ButtonLabel(label)
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(palette.accent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = palette.onAccent
                    )
                }
            }
        }
    }
}

private data class ShotSpec(val rotation: Float, val dx: Dp, val dy: Dp, val accentAlpha: Float?)
private data class FanSpec(val widthFraction: Float, val shots: List<ShotSpec>)

private val CenteredFanSpec = FanSpec(
    widthFraction = 0.70f,
    shots = listOf(
        ShotSpec(-11f, (-16).m, (-4).m, accentAlpha = 0.40f),
        ShotSpec(8f, 14.m, 0.m, accentAlpha = null),
    )
)

private val CardHandSpec = FanSpec(
    widthFraction = 0.66f,
    shots = listOf(
        ShotSpec(-17f, (-30).m, (-6).m, accentAlpha = 0.35f),
        ShotSpec(15f, 30.m, (-2).m, accentAlpha = null),
        ShotSpec(-5f, (-8).m, (-12).m, accentAlpha = 0.20f),
    )
)

@Composable
private fun CardFan(
    spec: FanSpec,
    page: Int,
    focusAreas: Set<String>,
    onEvent: (PermissionEvent) -> Unit,
) {
    val showFan = page == 0 || page == FOCUS_PAGE
    val showBadges = page != HIGHLIGHT_PAGE && page != EXCLUSION_PAGE
    val mirrorBadges = page == FOCUS_PAGE

    val backCardImage = painterResource(Res.drawable.on_balance)
    val frontTiltState = animateFloatAsState(
        targetValue = if (page == 0) -7f else 0f,
        animationSpec = onboardingTween(250),
        label = "FrontTilt"
    )

    Box(Modifier.fillMaxWidth(spec.widthFraction)) {
        AnimatedVisibility(
            visible = showFan,
            modifier = Modifier.matchParentSize(),
            enter = fadeIn(onboardingTween(250)),
            exit = fadeOut(onboardingTween(150))
        ) {
            Box(Modifier.fillMaxSize()) {
                spec.shots.forEachIndexed { i, shot ->
                    val isPage1Back = page == 0 && i == 1
                    MockShot(
                        spec = if (isPage1Back) shot.copy(
                            rotation = shot.rotation + 10f,
                            dx = shot.dx + 8.m
                        ) else shot,
                        modifier = Modifier.fillMaxSize(),
                        image = if (isPage1Back) backCardImage else null
                    )
                }
            }
        }
        ConceptCardHost(
            page, focusAreas, onEvent,
            modifier = Modifier.graphicsLayer { rotationZ = frontTiltState.value }
        )
        if (showBadges) FanBadges(mirrored = mirrorBadges)
    }
}

@Composable
private fun BoxScope.FanBadges(mirrored: Boolean = false) {
    Box(
        Modifier
            .align(if (mirrored) Alignment.TopEnd else Alignment.TopStart)
            .offset(x = if (mirrored) 18.dp else (-18).dp, y = (-28).dp)
            .size(40.dp)
            .clip(CircleShape)
            .background(palette.accent),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Rounded.Photo, null,
            tint = palette.onAccent, modifier = Modifier.size(20.dp)
        )
    }
    Box(
        Modifier
            .align(if (mirrored) Alignment.BottomStart else Alignment.BottomEnd)
            .offset(x = if (mirrored) (-16).dp else 16.dp, y = 32.dp)
            .size(36.dp)
            .clip(CircleShape)
            .background(palette.card)
            .border(0.5.dp, palette.stroke, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Rounded.Schedule, null,
            tint = palette.accent, modifier = Modifier.size(18.dp)
        )
    }
}

private fun Modifier.overhang(top: Dp, bottom: Dp): Modifier = layout { measurable, constraints ->
    val topPx = top.roundToPx()
    val bottomPx = bottom.roundToPx()
    val w = constraints.maxWidth
    val h = constraints.maxHeight
    val placeable = measurable.measure(Constraints.fixed(w, h + topPx + bottomPx))
    layout(w, h) { placeable.place(0, -topPx) }
}

@Composable
private fun MockShot(spec: ShotSpec, modifier: Modifier = Modifier, image: Painter? = null) {
    val shape = RoundedCornerShape(14.dp)
    val headerColor =
        spec.accentAlpha?.let { palette.accent.copy(alpha = it) } ?: palette.ink.copy(alpha = 0.10f)
    Column(
        modifier
            .overhang(top = 16.m, bottom = 34.m)
            .graphicsLayer {
                rotationZ = spec.rotation
                translationX = spec.dx.toPx()
                translationY = spec.dy.toPx()
            }
            .clip(shape)
            .background(palette.shot)
            .border(0.5.dp, palette.stroke, shape)
    ) {
        Box(Modifier.fillMaxWidth().height(34.m).background(headerColor))
        if (image != null) {
            Image(
                painter = image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
        } else {
            Column(Modifier.padding(10.m)) { }
        }
    }
}

@Composable
private fun ScreenshotWall(
    page: Int,
    focusAreas: Set<String>,
    onEvent: (PermissionEvent) -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        AnimatedVisibility(
            visible = page != HIGHLIGHT_PAGE,
            modifier = Modifier.matchParentSize(),
            enter = fadeIn(onboardingTween(250)),
            exit = fadeOut(onboardingTween(150))
        ) {
            Box(Modifier.fillMaxSize()) {
                Column(
                    Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .graphicsLayer {
                            rotationZ = -10f
                            scaleX = 1.3f
                            scaleY = 1.3f
                            alpha = 0.55f
                        },
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) { row ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            repeat(4) { col ->
                                WallTile(
                                    highlighted = (row * 4 + col) % 3 == 0,
                                    modifier = Modifier.weight(1f).height(140.dp)
                                )
                            }
                        }
                    }
                }

                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0f to palette.background.copy(alpha = 0.10f),
                                    0.94f to palette.background
                                )
                            )
                        )
                )
            }
        }

        ConceptCardHost(
            page = page,
            focusAreas = focusAreas,
            onEvent = onEvent,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
                .graphicsLayer { rotationZ = if (page == HIGHLIGHT_PAGE) 0f else -2f }
        )
    }
}

@Composable
private fun WallTile(highlighted: Boolean, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier
            .clip(shape)
            .background(palette.shot)
            .border(0.5.dp, palette.stroke, shape)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(22.dp)
                .background(
                    if (highlighted) palette.accent.copy(alpha = 0.45f) else palette.ink.copy(
                        alpha = 0.08f
                    )
                )
        )
        Column(Modifier.padding(8.dp)) {
            Box(
                Modifier.fillMaxWidth().height(5.dp).clip(CircleShape)
                    .background(palette.ink.copy(alpha = 0.10f))
            )
            Spacer(Modifier.height(6.dp))
            Box(
                Modifier.fillMaxWidth(0.6f).height(5.dp).clip(CircleShape)
                    .background(palette.ink.copy(alpha = 0.10f))
            )
        }
    }
}

@Composable
private fun ConceptCardHost(
    page: Int,
    focusAreas: Set<String>,
    onEvent: (PermissionEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = page,
        modifier = modifier,
        transitionSpec = {
            val forward = targetState > initialState
            val enter = fadeIn(onboardingTween(250)) + slideInVertically(animationSpec = onboardingTween(250)) { if (forward) it / 8 else -it / 8 }
            val exit = fadeOut(onboardingTween(150)) + slideOutVertically(animationSpec = onboardingTween(150)) { if (forward) -it / 8 else it / 8 }

            (enter togetherWith exit).using(SizeTransform(clip = false))
        },
        label = "IntroCard"
    ) { p ->
        when (p) {
            0 -> UsageCard()
            HIGHLIGHT_PAGE -> HighlightStack()
            EXCLUSION_PAGE -> ExclusionCard()
            NOTIFICATION_PAGE -> NotificationCard()
            else -> FocusCard(focusAreas) { onEvent(PermissionEvent.ToggleFocusArea(it)) }
        }
    }
}

@Composable
private fun IntroCard(
    modifier: Modifier = Modifier,
    padding: Dp = 16.dp,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(palette.card)
            .border(0.5.dp, palette.stroke, shape)
            .padding(padding)
    ) { content() }
}

@Composable
private fun IconTile(icon: ImageVector) {
    Box(
        Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(palette.accentSoft),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = palette.accent,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun AppRow(
    icon: ImageVector,
    name: String,
    subtitle: String? = null,
    trailing: @Composable () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(icon)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(name, color = palette.textPrimary, fontSize = 14.sp)
            if (subtitle != null) {
                Text(subtitle, color = palette.textSecondary, fontSize = 11.sp)
            }
        }
        trailing()
    }
}

@Composable
private fun Caption(text: String) {
    Text(text, color = palette.textSecondary, fontSize = 12.sp)
}

@Composable
private fun UsageCard() {
    IntroCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Caption("Today")
            Caption("3h 42m")
        }
        Spacer(Modifier.height(10.dp))
        Image(
            painter = painterResource(Res.drawable.cards_around),
            contentDescription = "Get Started bg",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
        )
    }
}

private data class HighlightItem(
    val icon: ImageVector,
    val label: String,
    val range: String,
    val text: String,
)

@Composable
private fun HighlightStack() {
    val items = listOf(
        HighlightItem(
            Icons.Rounded.WbSunny,
            "Morning highlight",
            "6 AM – 12 PM",
            "Two hours of deep research before noon."
        ),
        HighlightItem(
            Icons.Rounded.WbCloudy,
            "Afternoon highlight",
            "12 – 6 PM",
            "A long break around 2, then back to focused work."
        ),
        HighlightItem(
            Icons.Rounded.NightsStay,
            "Evening highlight",
            "6 – 11 PM",
            "You wound down with messages and music after 8."
        ),
    )
    val cardHeight = 124.dp
    val peek = 48.dp

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(cardHeight + peek * (items.size - 1))
        ) {
            items.forEachIndexed { index, item ->
                val depth = items.lastIndex - index
                IntroCard(
                    modifier = Modifier
                        .height(cardHeight)
                        .offset(y = peek * index)
                        .graphicsLayer { scaleX = 1f - depth * 0.06f },
                    padding = 14.dp
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconTile(item.icon)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            item.label,
                            color = palette.accent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Caption(item.range)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        item.text,
                        color = palette.textPrimary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Image(
            painter = painterResource(Res.drawable.open_hand),
            contentDescription = "Open hand illustration",
            modifier = Modifier.size(120.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun ExclusionCard() {
    val rows = listOf(
        Icons.Rounded.AccountBalance to "Bank app",
        Icons.Rounded.ChatBubble to "Messages",
        Icons.Rounded.Language to "Chrome",
    )
    val enabled = remember { mutableStateListOf(false, true, true) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(Res.drawable.secure_lock),
            contentDescription = "Secure lock",
            modifier = Modifier.size(120.dp),
            contentScale = ContentScale.Fit
        )

        IntroCard {
            Caption("Screenshots")
            Spacer(Modifier.height(4.dp))
            rows.forEachIndexed { i, (icon, name) ->
                AppRow(icon, name, subtitle = if (enabled[i]) null else "excluded") {
                    Switch(checked = enabled[i], onCheckedChange = { enabled[i] = it })
                }
            }
        }
    }
}

@Composable
private fun NotificationCard() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopEnd
    ) {
        Column(Modifier.fillMaxWidth()) {
            val peek = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
            Box(
                Modifier
                    .padding(horizontal = 28.dp)
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(peek)
                    .background(palette.card.copy(alpha = 0.5f))
            )
            Box(
                Modifier
                    .padding(horizontal = 14.dp)
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(peek)
                    .background(palette.card.copy(alpha = 0.75f))
            )
            IntroCard(padding = 14.dp) {
                Row {
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(palette.accent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Schedule,
                            null,
                            tint = palette.onAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Caption("Timeline · now")
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "Your afternoon highlight is ready",
                            color = palette.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "You focused for 2 hours, then wound down around 6.",
                            color = palette.textSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        Image(
            painter = painterResource(Res.drawable.on_fence),
            contentDescription = "Character sitting on fence",
            modifier = Modifier
                .offset(x = (-12).dp, y = (-48).dp)
                .size(90.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FocusCard(selected: Set<String>, onToggle: (String) -> Unit) {
    val orangeColor = Color(0xFFFF6F00)
    val whiteColor = Color.White

    IntroCard {
        Caption("Pick a few")
        Spacer(Modifier.height(10.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            IntroFocusAreas.forEachIndexed { index, id ->
                val isOn = id in selected
                val isExplanatory = index == 2 || id == "Explanatory"

                val backgroundColor = if (isOn) orangeColor else Color.Transparent
                val textColor = if (isOn) whiteColor else palette.textPrimary
                val borderColor = if (isOn) orangeColor else orangeColor.copy(alpha = 0.6f)

                Box(
                    modifier = Modifier
                        .then(if (isExplanatory) Modifier.fillMaxWidth() else Modifier)
                        .clip(CircleShape)
                        .background(backgroundColor)
                        .border(1.dp, borderColor, CircleShape)
                        .toggleable(
                            value = isOn,
                            role = Role.Checkbox,
                            onValueChange = { onToggle(id) }
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = id,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}