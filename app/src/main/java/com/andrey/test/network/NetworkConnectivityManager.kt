package com.andrey.test.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager

class NetworkConnectivityManager {
    private val networkChangeReceiver: BroadcastReceiver
    private var onUnavailable: Runnable? = null
    private var onAvailable: Runnable? = null
    private var isReceiverRegistered = false

    init {
        this.networkChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val networkState =
                    getConnectivityStatus(
                        context
                    )
                if (networkState == TYPE_NOT_CONNECTED) {
                    onUnavailable!!.run()
                }else{
                    onAvailable!!.run()
                }
            }
        }
    }

    fun subscribeOnConnectionUnavailable(context: Context, onUnavailable: Runnable) {
        this.onUnavailable = onUnavailable
        registerReceiver(context)

    }

    fun subscribeOnConnectionAvailable(context: Context, onAvailable: Runnable) {
        this.onAvailable = onAvailable
        registerReceiver(context)

    }

    private fun registerReceiver(context: Context){
        if(!isReceiverRegistered){
            context.registerReceiver(
                networkChangeReceiver,
                IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
            )
            isReceiverRegistered = true
        }
    }

    fun unregister(context: Context) {
        if (isReceiverRegistered) {
            context.unregisterReceiver(networkChangeReceiver)
            isReceiverRegistered = false
        }
    }

    companion object {
        private val TYPE_WIFI = 1
        private val TYPE_MOBILE = 2
        private val TYPE_NOT_CONNECTED = 0

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