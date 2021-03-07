package com.andrey.test.presentation.network

import kotlinx.coroutines.flow.Flow

interface NetworkConnectivityManager {
    fun observe(): Flow<Boolean>
    fun getLast(): Boolean
}