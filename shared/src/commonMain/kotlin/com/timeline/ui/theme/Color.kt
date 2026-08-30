package com.timeline.ui.theme

import androidx.compose.ui.graphics.Color

object AppColors {
    // Brand Colors
    val BrandOrange = Color(0xFFE67E22)
    val BrandYellow = Color(0xFFF1C40F)
    val BrandCyan = Color(0xFF00FFFF)
    val BrandBlack = Color(0xFF000000)

    val Success = Color(0xFF2ECC71)
    
    // Backgrounds
    val SurfaceDark = Color(0xFF1C1C1E)
    
    // Permission Category Colors
    val PermissionUsage = Color(0xFF3498DB)
    val PermissionOverlay = Color(0xFFE74C3C)
    val PermissionNotifications = Color(0xFFF1C40F)
    val PermissionAccessibility = Color(0xFF9B59B6)
    val PermissionBattery = Color(0xFF2ECC71)
    val PermissionDefault = Color.Green
    
    // Paywall Specific
    val PaywallCardBackground = Color(0xFFE0E0E0) // Light Gray
    val PaywallOptionSelected = Color.Black
    val PaywallOptionUnselected = Color(0xFFF5F5F5) // Very Light Gray
    val PaywallBadgeOrange = Color(0xFFE67E22)
    val PaywallBadgePurple = Color(0xFF9B59B6)
    val PaywallOfferGold = Color(0xFFF1C40F)
    
    // Gradients
    val BrandGradient = listOf(BrandOrange, BrandYellow, BrandBlack)
    val BadgeGradient = listOf(PaywallBadgeOrange, PaywallBadgePurple)

    fun forPermission(id: String): Color = when (id) {
        "usage" -> PermissionUsage
        "overlay" -> PermissionOverlay
        "notifications" -> PermissionNotifications
        "accessibility" -> PermissionAccessibility
        "battery" -> PermissionBattery
        else -> PermissionDefault
    }
}
