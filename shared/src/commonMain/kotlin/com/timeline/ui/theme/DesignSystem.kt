package com.timeline.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

object Palette {
    val Ink = Color(0xFF152801)
    val InkSecondary = Color(0xFF5F6A56)
    val InkTertiary = Color(0xFFA1A39D)
    var Brand = Color(0xFF264311)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceWarm = Color(0xFFF7F4ED)
    val FillNeutral = Color(0xFFEFEFEC)
    val FillNeutral2 = Color(0xFFF4F4F4)
    val Border = Color(0xFFC1C2BC)
    val BorderSoft = Color(0xFFD8D8D2)
    val AccentPremium = Color(0xFF4563FF)
    val AccentTint = Color(0xFFA3B2FF)
    val AccentTintBody = Color(0xFFA3B2FF)
    val Rating = Color(0xFF64F67B)
    val Danger = Color(0xFFB34C36)
    val Notify = Color(0xFF51ADC7)
    val Tooltip = Color(0xFF353534)
}

object Space {
    val x1 = 4.dp;
    val x2 = 8.dp;
    val x3 = 12.dp;
    val x4 = 16.dp;
    val x6 = 24.dp;
    val x8 = 32.dp;
    val x10 = 40.dp;
    val x12 = 48.dp
}

object AppShapes {
    val Pill = RoundedCornerShape(percent = 50)
    val Card = RoundedCornerShape(16.dp)
    val Inset = RoundedCornerShape(12.dp)
    val Sheet = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    val Tip = RoundedCornerShape(8.dp)
}

object AppTypography {
    // Display (welcome, paywall): 32, semibold, tracking -1%
    val Display = TextStyle(
        fontSize = 32.sp,
        lineHeight = 40.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.32).sp,
        fontFeatureSettings = "tnum"
    )
    // Screen title: 26, semibold
    val ScreenTitle = TextStyle(
        fontSize = 26.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight.SemiBold,
        fontFeatureSettings = "tnum"
    )
    val H1 = ScreenTitle
    val H2 = TextStyle(
        fontSize = 22.sp,
        lineHeight = 30.sp,
        fontWeight = FontWeight.SemiBold,
        fontFeatureSettings = "tnum"
    )
    val H3 = TextStyle(
        fontSize = 18.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.SemiBold,
        fontFeatureSettings = "tnum"
    )
    // Narrative (Highlights): 25, regular, Plus Jakarta Sans
    val Narrative = TextStyle(
        fontSize = 25.sp,
        lineHeight = 35.sp,
        fontWeight = FontWeight.Normal,
        fontFeatureSettings = "tnum"
    )
    // Body: 16, regular
    val Body = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Normal,
        fontFeatureSettings = "tnum"
    )
    val Para = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Normal,
        fontFeatureSettings = "tnum"
    )
    // Card title: 18, semibold
    val Title = TextStyle(
        fontSize = 18.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.SemiBold,
        fontFeatureSettings = "tnum"
    )
    // Caption and times: 13, medium
    val Cap = TextStyle(
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Medium,
        fontFeatureSettings = "tnum"
    )
    // Section label: 12, medium, all caps with +6% tracking (+0.72sp). Nothing smaller than 12.
    val Micro = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.72.sp,
        fontFeatureSettings = "tnum"
    )
    val Tab = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        fontFeatureSettings = "tnum"
    )
    val Stat = TextStyle(
        fontSize = 26.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight.SemiBold,
        fontFeatureSettings = "tnum"
    )
}
