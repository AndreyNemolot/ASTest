package com.andrey.test.domain

import android.content.Context
import kotlinx.coroutines.flow.Flow

interface NetworkConnectivityManager {
    fun observe(): Flow<Boolean>
    fun get(): Boolean
    fun unregister(context: Context)
}