package com.timeline.util

object Constants {
    // Tracking Intervals
    const val POLLING_INTERVAL_MS = 5000L
    const val SCREENSHOT_INTERVAL_MS = 60000L
    const val USAGE_STATS_LOOKBACK_MS = 10000L
    
    // Notification IDs
    const val TRACKING_NOTIFICATION_ID = 1001
    const val ACCESSIBILITY_LOST_NOTIFICATION_ID = 1002
    const val TRACKING_CHANNEL_ID = "tracking_channel"
    
    // Exclusion Policies
    val HARDCODED_EXCLUSIONS = setOf(
        "com.timeline",
        "android",
        "com.android.systemui",
        "com.google.android.apps.nexuslauncher",
        "com.android.launcher3",
        "com.transsion.XOSLauncher"
    )
}
