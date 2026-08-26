package com.timeline.ui.paywall

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class PaywallColors(
    val background: Color,
    val onBackground: Color,
    val onBackgroundMuted: Color,
    val onBackgroundFaint: Color,
    val cardSurface: Color,
    val cardSurfaceElevated: Color,
    val divider: Color,
    val proBadgeBackground: Color,
    val proBadgeContent: Color,
    val goldAccent: Color,
    val goldCardBorder: Color,
    val goldCardBackgroundTop: Color,
    val goldCardBackgroundBottom: Color,
    val gradientOrange: Color,
    val gradientYellow: Color,
    val gradientCyan: Color,
    val gradientTeal: Color,
    val saveBadgeStart: Color,
    val saveBadgeEnd: Color,
    val checkGreen: Color,
    val crossMuted: Color,
    val ctaContainer: Color,
    val ctaContent: Color
)

private val DarkPaywallColors = PaywallColors(
    background = Color(0xFF0B0B0C),
    onBackground = Color(0xFFFFFFFF),
    onBackgroundMuted = Color(0xFFAFAFAF),
    onBackgroundFaint = Color(0xFF7A7A7A),
    cardSurface = Color(0xFF1C1C1E),
    cardSurfaceElevated = Color(0xFF232326),
    divider = Color(0xFF2E2E30),
    proBadgeBackground = Color(0xFFF2E7C9),
    proBadgeContent = Color(0xFF1A1A1A),
    goldAccent = Color(0xFFD9B65B),
    goldCardBorder = Color(0xFF6B5427),
    goldCardBackgroundTop = Color(0xFF2A2010),
    goldCardBackgroundBottom = Color(0xFF17140C),
    gradientOrange = Color(0xFFE8622C),
    gradientYellow = Color(0xFFF4B740),
    gradientCyan = Color(0xFF2FB8C6),
    gradientTeal = Color(0xFF1C8C8C),
    saveBadgeStart = Color(0xFFEC4899),
    saveBadgeEnd = Color(0xFF8B5CF6),
    checkGreen = Color(0xFF34C759),
    crossMuted = Color(0xFF5A5A5C),
    ctaContainer = Color(0xFFF5F0E6),
    ctaContent = Color(0xFF141414)
)

private val LightPaywallColors = PaywallColors(
    background = Color(0xFFF8F9FA),
    onBackground = Color(0xFF1A1A1A),
    onBackgroundMuted = Color(0xFF6C757D),
    onBackgroundFaint = Color(0xFFADB5BD),
    cardSurface = Color(0xFFFFFFFF),
    cardSurfaceElevated = Color(0xFFF1F3F5),
    divider = Color(0xFFE9ECEF),
    proBadgeBackground = Color(0xFFFFF3CD),
    proBadgeContent = Color(0xFF856404),
    goldAccent = Color(0xFFB8860B),
    goldCardBorder = Color(0xFFD3D3D3),
    goldCardBackgroundTop = Color(0xFFFFF8DC),
    goldCardBackgroundBottom = Color(0xFFFAFAD2),
    gradientOrange = Color(0xFFFF7F50),
    gradientYellow = Color(0xFFFFD700),
    gradientCyan = Color(0xFF00CED1),
    gradientTeal = Color(0xFF20B2AA),
    saveBadgeStart = Color(0xFFEC4899),
    saveBadgeEnd = Color(0xFF8B5CF6),
    checkGreen = Color(0xFF28A745),
    crossMuted = Color(0xFF6C757D),
    ctaContainer = Color(0xFF1A1A1A),
    ctaContent = Color(0xFFFFFFFF)
)

private val LocalPaywallColors = staticCompositionLocalOf { DarkPaywallColors }

object NewPaywallPalette {
    val colors: PaywallColors
        @Composable
        @ReadOnlyComposable
        get() = LocalPaywallColors.current

    @Composable
    fun ProvideNewPaywallColors(
        darkTheme: Boolean,
        content: @Composable () -> Unit
    ) {
        val targetColors = if (darkTheme) DarkPaywallColors else LightPaywallColors
        CompositionLocalProvider(LocalPaywallColors provides targetColors, content = content)
    }

    // Convenience mappings so component code doesn't break
    val Background @Composable get() = colors.background
    val OnBackground @Composable get() = colors.onBackground
    val OnBackgroundMuted @Composable get() = colors.onBackgroundMuted
    val CardSurface @Composable get() = colors.cardSurface
    val CardSurfaceElevated @Composable get() = colors.cardSurfaceElevated
    val GradientCyan @Composable get() = colors.gradientCyan
    val GradientTeal @Composable get() = colors.gradientTeal
    val GoldAccent @Composable get() = colors.goldAccent
    val GoldCardBorder @Composable get() = colors.goldCardBorder
    val GoldCardBackgroundTop @Composable get() = colors.goldCardBackgroundTop
    val GoldCardBackgroundBottom @Composable get() = colors.goldCardBackgroundBottom
    val GradientOrange @Composable get() = colors.gradientOrange
    val GradientYellow @Composable get() = colors.gradientYellow
    val SaveBadgeStart @Composable get() = colors.saveBadgeStart
    val SaveBadgeEnd @Composable get() = colors.saveBadgeEnd
    val CrossMuted @Composable get() = colors.crossMuted
}