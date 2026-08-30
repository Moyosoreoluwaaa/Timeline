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
        "com.timeline_records",
        "android",
        "com.android.systemui",
        "com.google.android.apps.nexuslauncher",
        "com.android.launcher3",
        "com.transsion.XOSLauncher",
        // OEM Stock Launchers
        "com.sec.android.app.launcher",       // Samsung One UI Home
        "com.miui.home",                      // Xiaomi MIUI / HyperOS
        "com.miui.pocolauncher",              // POCO Launcher
        "com.oppo.launcher",                  // OnePlus / OPPO ColorOS
        "com.bbk.launcher2",                  // Vivo / iQOO Funtouch / OriginOS
        "com.motorola.launcher3",             // Motorola
        "com.nothing.launcher",               // Nothing OS
        "com.asus.launcher",                  // Asus ZenUI
        "com.sonyericsson.home",              // Sony Xperia Home
        "com.huawei.android.launcher",        // Huawei EMUI
        "com.transsion.hilauncher",           // Tecno / Infinix HiOS
        // Popular 3rd-Party Launchers
        "com.teslacoilsw.launcher",           // Nova Launcher
        "ginlemon.flowerpro",                 // Smart Launcher 6
        "com.microsoft.launcher",             // Microsoft Launcher
        "com.actionlauncher.playstore",       // Action Launcher
        "ch.deletescape.lawnchair.plah",      // Lawnchair
        "project.launcher",                   // Hyperion Launcher
        "com.ss.launcher2",                   // Total Launcher
        "com.ss.square2",                     // Square Home
        "com.anddoes.launcher",               // Apex Launcher
        "com.gau.go.launcherex",              // GO Launcher EX
        // Minimalist & Productive
        "bitpit.launcher",                    // Niagara Launcher
        "app.launcher.olauncher",             // OLauncher
        "com.beforelabs.launcher",            // Before Launcher
        "ru.evgeniy.aio",                     // AIO Launcher
        "fr.neamar.kiss",                     // KISS Launcher
        "com.indistractablelauncher.app",     // Indistractable Launcher
        // Specialized Wrappers
        "com.luutinhit.ioslauncher",          // Launcher iOS
        "name.k2700.biglauncher",             // BIG Launcher
        "eu.chainfire.sideloadlauncher",      // Sideload Launcher
        "org.xbmc.kodi"                        // Kodi Media Center
    )
}
