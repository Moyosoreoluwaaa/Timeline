package com.timeline.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import timeline.shared.generated.resources.Res
import timeline.shared.generated.resources.plus_jakarta_sans
import timeline.shared.generated.resources.plus_jakarta_sans_italic

@Composable
fun plusJakartaSansFamily(): FontFamily {
    return FontFamily(
        Font(Res.font.plus_jakarta_sans, weight = FontWeight.Normal, style = FontStyle.Normal),
        Font(Res.font.plus_jakarta_sans, weight = FontWeight.Medium, style = FontStyle.Normal),
        Font(Res.font.plus_jakarta_sans, weight = FontWeight.SemiBold, style = FontStyle.Normal),
        Font(Res.font.plus_jakarta_sans, weight = FontWeight.Bold, style = FontStyle.Normal),
        Font(Res.font.plus_jakarta_sans, weight = FontWeight.ExtraBold, style = FontStyle.Normal),
        Font(Res.font.plus_jakarta_sans_italic, weight = FontWeight.Normal, style = FontStyle.Italic)
    )
}

// Static default fallback styles matching the design scale
private val DisplayLargeStyle = TextStyle(
    fontWeight = FontWeight.SemiBold,
    fontSize = 32.sp,
    lineHeight = 40.sp,
    letterSpacing = (-0.32).sp,
    fontFeatureSettings = "tnum"
)

private val HeadlineLargeStyle = TextStyle(
    fontWeight = FontWeight.SemiBold,
    fontSize = 26.sp,
    lineHeight = 34.sp,
    letterSpacing = 0.sp,
    fontFeatureSettings = "tnum"
)

private val HeadlineMediumStyle = TextStyle(
    fontWeight = FontWeight.SemiBold,
    fontSize = 22.sp,
    lineHeight = 30.sp,
    letterSpacing = 0.sp,
    fontFeatureSettings = "tnum"
)

private val HeadlineSmallStyle = TextStyle(
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 26.sp,
    letterSpacing = 0.15.sp,
    fontFeatureSettings = "tnum"
)

private val TitleLargeStyle = TextStyle(
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 26.sp,
    letterSpacing = 0.15.sp,
    fontFeatureSettings = "tnum"
)

private val TitleMediumStyle = TextStyle(
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.15.sp,
    fontFeatureSettings = "tnum"
)

private val TitleSmallStyle = TextStyle(
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp,
    fontFeatureSettings = "tnum"
)

private val BodyLargeStyle = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.25.sp,
    fontFeatureSettings = "tnum"
)

private val BodyMediumStyle = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.25.sp,
    fontFeatureSettings = "tnum"
)

private val CaptionStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.2.sp,
    fontFeatureSettings = "tnum"
)

private val SectionLabelStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.72.sp,
    fontFeatureSettings = "tnum"
)

val Typography = Typography(
    displayLarge = DisplayLargeStyle,
    displayMedium = HeadlineLargeStyle,
    displaySmall = HeadlineMediumStyle,

    headlineLarge = HeadlineLargeStyle,
    headlineMedium = HeadlineMediumStyle,
    headlineSmall = HeadlineSmallStyle,

    titleLarge = TitleLargeStyle,
    titleMedium = TitleMediumStyle,
    titleSmall = TitleSmallStyle,

    bodyLarge = BodyLargeStyle,
    bodyMedium = BodyMediumStyle,
    bodySmall = CaptionStyle,

    labelLarge = TitleMediumStyle,
    labelMedium = SectionLabelStyle,
    labelSmall = SectionLabelStyle
)

@Composable
fun getAppTypography(): Typography {
    val family = plusJakartaSansFamily()
    return Typography(
        displayLarge = DisplayLargeStyle.copy(fontFamily = family),
        displayMedium = HeadlineLargeStyle.copy(fontFamily = family),
        displaySmall = HeadlineMediumStyle.copy(fontFamily = family),

        headlineLarge = HeadlineLargeStyle.copy(fontFamily = family),
        headlineMedium = HeadlineMediumStyle.copy(fontFamily = family),
        headlineSmall = HeadlineSmallStyle.copy(fontFamily = family),

        titleLarge = TitleLargeStyle.copy(fontFamily = family),
        titleMedium = TitleMediumStyle.copy(fontFamily = family),
        titleSmall = TitleSmallStyle.copy(fontFamily = family),

        bodyLarge = BodyLargeStyle.copy(fontFamily = family),
        bodyMedium = BodyMediumStyle.copy(fontFamily = family),
        bodySmall = CaptionStyle.copy(fontFamily = family),

        labelLarge = TitleMediumStyle.copy(fontFamily = family),
        labelMedium = SectionLabelStyle.copy(fontFamily = family),
        labelSmall = SectionLabelStyle.copy(fontFamily = family)
    )
}
