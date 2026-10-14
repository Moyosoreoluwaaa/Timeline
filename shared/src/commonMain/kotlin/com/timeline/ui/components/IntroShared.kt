package com.timeline.ui.components

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.timeline.util.AppStrings

enum class FanVariant { CenteredFan, CardHand, ScreenshotWall }

val IntroFocusAreas: List<String> = listOf("Concise", "Balanced", "Explanatory")

internal const val HIGHLIGHT_PAGE = 1
internal const val EXCLUSION_PAGE = 2     // "Page 3"
internal const val NOTIFICATION_PAGE = 3  // "Page 4"
internal const val FOCUS_PAGE = 4         // "Page 5"

internal const val MOCK_SCALE = 1.6f
internal val Int.m: Dp get() = (this * MOCK_SCALE).dp

@Immutable
internal class IntroPalette(
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

internal val LocalIntroPalette = staticCompositionLocalOf<IntroPalette> {
    error("IntroPalette not provided")
}

internal val introPalette: IntroPalette
    @Composable get() = LocalIntroPalette.current

internal data class IntroCopy(val title: String, val subtitle: String)

internal val IntroPages = listOf(
    IntroCopy(AppStrings.IntroPage1Title, AppStrings.IntroPage1Subtitle),
    IntroCopy(AppStrings.IntroPage2Title, AppStrings.IntroPage2Subtitle),
    IntroCopy(AppStrings.IntroPage3Title, AppStrings.IntroPage3Subtitle),
    IntroCopy(AppStrings.IntroPage4Title, AppStrings.IntroPage4Subtitle),
    IntroCopy(AppStrings.IntroPage5Title, AppStrings.IntroPage5Subtitle),
)
