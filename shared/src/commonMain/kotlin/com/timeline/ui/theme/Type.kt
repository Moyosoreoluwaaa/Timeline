package com.timeline.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Adopting the design system's generous line heights and scale ratios
private val DisplayLargeStyle = TextStyle(
    fontWeight = FontWeight.SemiBold,
    fontSize = 32.sp,
    lineHeight = 40.sp,
    letterSpacing = (-0.5).sp
)

private val HeadlineStyle = TextStyle(
    fontWeight = FontWeight.SemiBold,
    fontSize = 24.sp,
    lineHeight = 32.sp,
    letterSpacing = 0.sp
)

private val TitleStyle = TextStyle(
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 26.sp,
    letterSpacing = 0.15.sp
)



private val CaptionStyle = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.4.sp
)

val Typography = Typography(
    displayLarge = DisplayLargeStyle,
    displayMedium = HeadlineStyle,
    displaySmall = TitleStyle,
    
    headlineLarge = AppTypography.H1,
    headlineMedium = AppTypography.H2,
    headlineSmall = AppTypography.H3,
    
    titleLarge = TitleStyle,
    titleMedium = AppTypography.Title,
    titleSmall = AppTypography.Title.copy(fontSize = 14.sp, lineHeight = 20.sp),
    
    bodyLarge = AppTypography.Para,
    bodyMedium = AppTypography.Body,
    bodySmall = CaptionStyle,
    
    labelLarge = AppTypography.Title,
    labelMedium = AppTypography.Cap,
    labelSmall = AppTypography.Micro
)
