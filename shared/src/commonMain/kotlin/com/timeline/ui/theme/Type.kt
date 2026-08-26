package com.timeline.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val TitleStyle = TextStyle(
    fontWeight = FontWeight.Bold,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.sp
)

private val HeadingStyle = TextStyle(
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.15.sp
)

private val BodyStyle = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.25.sp
)

private val CaptionStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.4.sp
)

val Typography = Typography(
    // Title tier
    headlineLarge = TitleStyle,
    headlineMedium = TitleStyle,
    headlineSmall = TitleStyle,
    titleLarge = TitleStyle,

    // Heading tier
    titleMedium = HeadingStyle,
    titleSmall = HeadingStyle,
    labelLarge = HeadingStyle, // Typically used for buttons

    // Body tier
    bodyLarge = BodyStyle,
    bodyMedium = BodyStyle,

    // Caption tier
    bodySmall = CaptionStyle,
    labelMedium = CaptionStyle,
    labelSmall = CaptionStyle,

    // Display tier (Mapping to Title for consistency, or keeping defaults if preferred)
    // The plan didn't explicitly mention displays, but for consistency:
    displayLarge = TitleStyle.copy(fontSize = 32.sp),
    displayMedium = TitleStyle.copy(fontSize = 28.sp),
    displaySmall = TitleStyle.copy(fontSize = 24.sp)
)
