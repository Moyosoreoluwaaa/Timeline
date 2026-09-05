package com.timeline.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.Primary,
    secondary = AppColors.BrandYellow,
    tertiary = AppColors.BrandCyan,
    background = AppColors.BrandBlack,
    surface = AppColors.SurfaceDark,
    surfaceVariant = Color(0xFF3E3E42),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color.White,
    outline = Color(0xFF8E8E93),
    outlineVariant = Color(0xFF48484A),
    surfaceContainerLow = Color(0xFF2C2C2E),
    surfaceContainer = Color(0xFF3A3A3C),
    surfaceContainerHigh = Color(0xFF48484A),
    surfaceContainerHighest = Color(0xFF636366),
    primaryContainer = AppColors.Primary.copy(alpha = 0.2f),
    onPrimaryContainer = AppColors.Primary,
    secondaryContainer = AppColors.BrandYellow.copy(alpha = 0.2f),
    onSecondaryContainer = AppColors.BrandYellow,
)

private val LightColorScheme = lightColorScheme(
    primary = AppColors.Primary,
    secondary = AppColors.BrandYellow,
    tertiary = AppColors.BrandCyan,
    background = Color.White,
    surface = Color.White,
    surfaceVariant = Color(0xFFF2F2F7),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onSurfaceVariant = Color.Gray,
    outline = Color(0xFFC7C7CC),
    outlineVariant = Color(0xFFE5E5EA),
    surfaceContainerLow = Color(0xFFF9F9F9),
    surfaceContainer = Color(0xFFF2F2F7),
    surfaceContainerHigh = Color(0xFFEBEBF0),
    surfaceContainerHighest = Color(0xFFFAF6F6),
    primaryContainer = AppColors.Primary.copy(alpha = 0.1f),
    onPrimaryContainer = AppColors.Primary,
    secondaryContainer = AppColors.BrandYellow.copy(alpha = 0.1f),
    onSecondaryContainer = Color.Black,
)

@Composable
fun TimelineTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
