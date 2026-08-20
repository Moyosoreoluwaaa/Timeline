package com.timeline

import androidx.compose.ui.window.ComposeUIViewController
import com.timeline.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        // Koin should be initialized only once. 
        // In KMP, usually initKoin is called from the platform entry point.
    }
) { 
    App() 
}
