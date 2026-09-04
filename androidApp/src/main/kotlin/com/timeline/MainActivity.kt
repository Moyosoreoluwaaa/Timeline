package com.timeline

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import co.touchlab.kermit.Logger
import com.timeline.domain.NotificationManager
import com.timeline.service.TrackingService
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val notificationManager: NotificationManager by inject()

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Logger.d(tag = "MainActivity") { "System notification permission granted: $isGranted" }
        Logger.d(tag = "MainActivity") { "OneSignal notification permission state (via interface): ${notificationManager.hasPermission()}" }
    }

    @SuppressLint("BatteryLife")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT
            )
        )

        // Disable automatic navigation bar contrast enforcement on Android 10+ (API 29+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        super.onCreate(savedInstanceState)

        setContent {
            App(
                onNavigateToUsageStats = {
                    startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                },
                onNavigateToOverlay = {
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                        data = "package:$packageName".toUri()
                    })
                },
                onNavigateToNotification = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
                onNavigateToAccessibility = {
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                },
                onNavigateToBatteryOptimization = {
                    startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = "package:$packageName".toUri()
                    })
                },
                onStartService = {
                    startForegroundService(Intent(this@MainActivity, TrackingService::class.java))
                },
                onExitApp = {
                    finish()
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        // Koin doesn't easily provide access to the same VM instance here without complexity
        // But since we use collectAsStateWithLifecycle, the UI will refresh when it becomes active
        // and we have CheckPermissions in AppNavigation or SetupScreen.
    }
}