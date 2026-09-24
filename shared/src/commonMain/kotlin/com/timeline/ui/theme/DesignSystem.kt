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
    val H1 = TextStyle(
        fontSize = 28.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.SemiBold,
//        color = Palette.Ink
    )
    val H2 = TextStyle(
        fontSize = 24.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.Medium,
//        color = Palette.Ink
    )
    val H3 = TextStyle(
        fontSize = 20.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.SemiBold,
        //color = Palette.Ink
    )
    val Body = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Normal,
        //color = Palette.Ink
    )
    val Para = TextStyle(
        fontSize = 16.sp,
        lineHeight = 27.sp,
        fontWeight = FontWeight.Normal,
        //color = Palette.Ink
    )
    val Title = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold,
        //color = Palette.Ink
    )
    val Cap = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
        //color = Palette.InkSecondary
    )
    val Micro = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Normal,
        //color = Palette.InkSecondary
    )
    val Tab = TextStyle(
        fontSize = 10.sp,
        lineHeight = 12.sp,
        fontWeight = FontWeight.Medium,
        //color = Palette.InkTertiary
    )
    val Stat = TextStyle(
        fontSize = 26.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.SemiBold,
        //color = Palette.Ink
    )
}
