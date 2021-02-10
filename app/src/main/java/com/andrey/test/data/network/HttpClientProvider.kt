package com.andrey.test.data.network

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit


class HttpClientProvider {

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5000, TimeUnit.MILLISECONDS)
        .readTimeout(40000, TimeUnit.MILLISECONDS)
        .writeTimeout(15000, TimeUnit.MILLISECONDS)
        .retryOnConnectionFailure(true)
        .build()

    fun get(): OkHttpClient {
        return okHttpClient
    }
}