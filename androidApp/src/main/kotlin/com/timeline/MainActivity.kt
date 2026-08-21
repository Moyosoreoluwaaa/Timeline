package com.timeline

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import com.timeline.service.TrackingService

class MainActivity : ComponentActivity() {
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Permissions will be checked again by SetupViewModel on app resume or manual refresh
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
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
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        requestNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
                onNavigateToAccessibility = {
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                },
                onStartService = {
                    startForegroundService(Intent(this@MainActivity, TrackingService::class.java))
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
