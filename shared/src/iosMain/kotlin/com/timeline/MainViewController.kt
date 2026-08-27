package com.timeline

import androidx.compose.ui.window.ComposeUIViewController
import com.timeline.di.initKoin
import com.timeline.domain.configureRevenueCat

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
        configureRevenueCat()
    }
) { 
    App() 
}
