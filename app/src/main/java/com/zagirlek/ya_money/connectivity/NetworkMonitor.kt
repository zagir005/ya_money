package com.zagirlek.ya_money.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface NetworkMonitor {
    val isOnline: StateFlow<Boolean>
}

class AndroidNetworkMonitor(
    context: Context,
    private val onNetworkAvailable: () -> Unit,
) : NetworkMonitor {
    private val connectivityManager =
        context.getSystemService(ConnectivityManager::class.java)
    private val mutableIsOnline = MutableStateFlow(connectivityManager.hasValidatedNetwork())

    override val isOnline: StateFlow<Boolean> = mutableIsOnline.asStateFlow()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities,
        ) {
            updateConnectivity(networkCapabilities.hasValidatedInternet())
        }

        override fun onLost(network: Network) {
            updateConnectivity(false)
        }
    }

    init {
        connectivityManager.registerDefaultNetworkCallback(callback)
    }

    private fun updateConnectivity(isOnline: Boolean) {
        val wasOnline = mutableIsOnline.value
        mutableIsOnline.value = isOnline
        if (!wasOnline && isOnline) {
            onNetworkAvailable()
        }
    }
}

private fun ConnectivityManager.hasValidatedNetwork(): Boolean =
    activeNetwork
        ?.let(::getNetworkCapabilities)
        ?.hasValidatedInternet()
        ?: false

private fun NetworkCapabilities.hasValidatedInternet(): Boolean =
    hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
