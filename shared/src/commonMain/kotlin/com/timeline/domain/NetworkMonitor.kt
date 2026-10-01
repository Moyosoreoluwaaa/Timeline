package com.timeline.domain

import kotlinx.coroutines.flow.StateFlow

enum class NetworkStatus {
    CONNECTED,
    DISCONNECTED
}

interface NetworkMonitor {
    val status: StateFlow<NetworkStatus>
}

class AlwaysConnectedNetworkMonitor : NetworkMonitor {
    private val _status = kotlinx.coroutines.flow.MutableStateFlow(NetworkStatus.CONNECTED)
    override val status: StateFlow<NetworkStatus> = _status
}
