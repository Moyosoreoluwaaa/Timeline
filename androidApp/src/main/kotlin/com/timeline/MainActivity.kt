package com.timeline

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timeline.presentation.SetupEffect
import com.timeline.presentation.SetupEvent
import com.timeline.presentation.SetupViewModel
import com.timeline.presentation.TimelineViewModel
import com.timeline.service.TrackingService
import org.koin.compose.viewmodel.koinViewModel

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
            val viewModel: TimelineViewModel = koinViewModel()
            val setupViewModel: SetupViewModel = koinViewModel()
            val setupState by setupViewModel.state.collectAsStateWithLifecycle()

            LaunchedEffect(setupViewModel) {
                setupViewModel.onEvent(SetupEvent.CheckPermissions)
                setupViewModel.effects.collect { effect ->
                    if (effect is SetupEffect.AllPermissionsGranted) {
                        startForegroundService(Intent(this@MainActivity, TrackingService::class.java))
                    }
                }
            }

            // Start service if permissions are already granted
            LaunchedEffect(setupState.isUsageStatsGranted, setupState.isOverlayGranted, 
                setupState.isNotificationGranted, setupState.isAccessibilityGranted) {
                if (setupState.isUsageStatsGranted && setupState.isOverlayGranted && 
                    setupState.isNotificationGranted && setupState.isAccessibilityGranted) {
                    startForegroundService(Intent(this@MainActivity, TrackingService::class.java))
                }
            }

            App(
                viewModel = viewModel,
                setupViewModel = setupViewModel,
                onNavigateToUsageStats = {
                    startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                },
                onNavigateToOverlay = {
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                        data = Uri.parse("package:$packageName")
                    })
                },
                onNavigateToNotification = {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        requestNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
                onNavigateToAccessibility = {
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        // Force refresh permissions when returning to app
    }
}
