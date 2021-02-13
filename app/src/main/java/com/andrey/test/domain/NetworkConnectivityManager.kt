package com.andrey.test.domain

import kotlinx.coroutines.flow.Flow

interface NetworkConnectivityManager {
    fun observe(): Flow<Boolean>
    fun getLast(): Boolean
}