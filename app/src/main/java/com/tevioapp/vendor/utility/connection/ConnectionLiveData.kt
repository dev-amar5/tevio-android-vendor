package com.tevioapp.vendor.utility.connection

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.lifecycle.LiveData
import com.tevioapp.vendor.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ConnectionLiveData @Inject constructor(
    @ApplicationContext private val context: Context
) : LiveData<ConnectionLiveData.NetworkState>() {

    private val strOk = context.getString(R.string.internet_ok)
    private val strError = context.getString(R.string.internet_error)

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            postValue(NetworkState(true, strOk))
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            postValue(NetworkState(false, strError))
        }
    }

    override fun onActive() {
        super.onActive()
        // Initial state
        val isConnected = isConnected()
        postValue(NetworkState(isConnected, if (isConnected) strOk else strError))
        // Register callback
        connectivityManager.registerDefaultNetworkCallback(callback)
    }

    override fun onInactive() {
        super.onInactive()
        try {
            connectivityManager.unregisterNetworkCallback(callback)
        } catch (_: Exception) {
            // Ignore if already unregistered
        }
    }

    fun isConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    data class NetworkState(val connected: Boolean, val message: String)
}
