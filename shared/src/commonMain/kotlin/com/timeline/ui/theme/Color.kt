package com.timeline.ui.theme

import androidx.compose.ui.graphics.Color

object AppColors {
    // Brand Colors
    val BrandOrange = Color(0xFFE67E22)
    val BrandYellow = Color(0xFFF1C40F)
    val BrandCyan = Color(0xFF00FFFF)
    val BrandBlack = Color(0xFF000000)
    
    // Primary Brand Color (Using Orange as Primary)
    val Primary = BrandOrange

    val Success = Color(0xFF2ECC71)
    
    // Backgrounds
    val SurfaceDark = Color(0xFF1C1C1E)
    
    // Permission Category Colors
    val PermissionUsage = Color(0xFF3498DB)
    val PermissionOverlay = Color(0xFFE74C3C)
    val PermissionNotifications = Color(0xFFF1C40F)
    val PermissionAccessibility = Color(0xFFE67E22)
    val PermissionBattery = Color(0xFF2ECC71)
    val PermissionDefault = Color.Green
    
    // Text Hierarchy Tiers
    val TextPrimary = Color(0xFFF2F2F3)
    val TextSecondary = Color(0xFFA8A8B0)
    val TextTertiary = Color(0xFF85858D)

    // Window Theme Accents (day bar, card outlines, status pills)
    val WindowMorningAmber = Color(0xFFFFB703)
    val WindowAfternoonTeal = Color(0xFF5CD6C4) // Lightened for 5.6:1 contrast
    val WindowEveningIndigo = Color(0xFFA9B0FF) // Lightened for 5.6:1 contrast

    // High-contrast text on orange actions
    val OnOrangeDark = Color(0xFF1A1208)

    // Paywall Specific
    val PaywallCardBackground = Color(0xFFE0E0E0) // Light Gray
    val PaywallOptionSelected = Color.Black
    val PaywallOptionUnselected = Color(0xFFF5F5F5) // Very Light Gray
    val PaywallOfferGold = Color(0xFFF1C40F)
    
    // Gradients
    val BrandGradient = listOf(BrandOrange, BrandYellow, BrandBlack)
    val BadgeGradient = listOf(BrandOrange, BrandYellow)

    fun forPermission(id: String): Color = when (id) {
        "usage" -> PermissionUsage
        "overlay" -> PermissionOverlay
        "notifications" -> PermissionNotifications
        "accessibility" -> PermissionAccessibility
        "battery" -> PermissionBattery
        else -> PermissionDefault
    }
}
