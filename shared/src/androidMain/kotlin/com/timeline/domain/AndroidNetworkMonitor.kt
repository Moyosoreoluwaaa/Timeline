package com.timeline.domain

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidNetworkMonitor(
    context: Context
) : NetworkMonitor {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val lock = Any()
    private val validatedNetworks = mutableSetOf<Network>()

    private val _status = MutableStateFlow(NetworkStatus.CONNECTED)
    override val status: StateFlow<NetworkStatus> = _status.asStateFlow()

    init {
        // Seed from the current active network so the first value is accurate
        connectivityManager?.let { cm ->
            val active = cm.activeNetwork
            val caps = active?.let { cm.getNetworkCapabilities(it) }
            if (active != null && caps.isValidated()) validatedNetworks.add(active)
            _status.value = if (validatedNetworks.isEmpty()) NetworkStatus.DISCONNECTED
            else NetworkStatus.CONNECTED
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {
            // Intentionally no onAvailable: "available" != "has working internet"

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                update {
                    if (networkCapabilities.isValidated()) add(network) else remove(network)
                }
            }

            override fun onLost(network: Network) {
                update { remove(network) }
            }
        }

        try {
            connectivityManager?.registerNetworkCallback(request, callback)
        } catch (_: Exception) {
            _status.value = NetworkStatus.CONNECTED // can't monitor; don't show false offline
        }
    }

    private fun update(block: MutableSet<Network>.() -> Unit) {
        synchronized(lock) {
            validatedNetworks.block()
            _status.value =
                if (validatedNetworks.isEmpty()) NetworkStatus.DISCONNECTED
                else NetworkStatus.CONNECTED
        }
    }

    private fun NetworkCapabilities?.isValidated(): Boolean =
        this != null &&
                hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}