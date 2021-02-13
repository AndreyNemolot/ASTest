package com.andrey.test.domain

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.andrey.test.di.modules.AppModules.Companion.PROCESS_LIFECYCLE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Named

class NetworkConnectivityManagerImpl @Inject constructor(
    private val application: Application,
    @Named(PROCESS_LIFECYCLE) private val lifecycle: Lifecycle,
) : NetworkConnectivityManager, LifecycleObserver {
    private val networkChangeReceiver: BroadcastReceiver
    private var isReceiverRegistered = false

    private val _stateFlow = MutableStateFlow(true)

    override fun observe(): Flow<Boolean> {
        return _stateFlow.asStateFlow()
    }

    override fun getLast(): Boolean {
        return _stateFlow.value
    }

    init {
        lifecycle.addObserver(this)
        networkChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val networkState =
                    getConnectivityStatus(
                        context
                    )
                _stateFlow.tryEmit(networkState != TYPE_NOT_CONNECTED)
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun stop() {
        unregisterReceiver(application)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun start() {
        registerReceiver(application)
    }

    private fun registerReceiver(context: Context) {
        if (!isReceiverRegistered) {
            context.registerReceiver(
                networkChangeReceiver,
                IntentFilter(INTENT_FILTER_CONNECTIVITY)
            )
            isReceiverRegistered = true
        }
    }

    private fun unregisterReceiver(context: Context) {
        if (isReceiverRegistered) {
            context.unregisterReceiver(networkChangeReceiver)
            isReceiverRegistered = false
        }
    }

    private fun getConnectivityStatus(context: Context): Int {
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

    companion object {
        private const val TYPE_WIFI = 1
        private const val TYPE_MOBILE = 2
        private const val TYPE_NOT_CONNECTED = 0
        private const val INTENT_FILTER_CONNECTIVITY = "android.net.conn.CONNECTIVITY_CHANGE"
    }
}