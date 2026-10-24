package com.timeline.domain

import kotlinx.coroutines.flow.StateFlow

enum class NetworkStatus {
    CONNECTED,
    DISCONNECTED
}

interface NetworkMonitor {
    val status: StateFlow<NetworkStatus>
}
