package com.andrey.test.domain

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class NetworkConnectivityManagerImpl @Inject constructor(
    application: Application
) : NetworkConnectivityManager {
    private val networkChangeReceiver: BroadcastReceiver
    private var isReceiverRegistered = false

    private val _stateFlow = MutableStateFlow(true)

    override fun observe(): Flow<Boolean> {
        return _stateFlow.asStateFlow()
    }

    override fun get(): Boolean {
        return _stateFlow.value
    }

    init {
        this.networkChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val networkState =
                    getConnectivityStatus(
                        context
                    )
                _stateFlow.tryEmit(networkState != TYPE_NOT_CONNECTED)
            }
        }
        registerReceiver(application)

    }

    private fun registerReceiver(context: Context) {
        if (!isReceiverRegistered) {
            context.registerReceiver(
                networkChangeReceiver,
                IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
            )
            isReceiverRegistered = true
        }
    }

    override fun unregister(context: Context) {
        if (isReceiverRegistered) {
            context.unregisterReceiver(networkChangeReceiver)
            isReceiverRegistered = false
        }
    }

    companion object {
        private const val TYPE_WIFI = 1
        private const val TYPE_MOBILE = 2
        private const val TYPE_NOT_CONNECTED = 0

        fun getConnectivityStatus(context: Context): Int {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val activeNetwork = cm.activeNetworkInfo
            if (null != activeNetwork) {
                if (activeNetwork.type == ConnectivityManager.TYPE_WIFI)
                    return TYPE_WIFI

                if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE)
                    return TYPE_MOBILE
            }

            return TYPE_NOT_CONNECTED
        }
    }
}