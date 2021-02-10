package com.andrey.test.data.network

import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

abstract class Network {

    suspend inline fun <reified T> get(url: String): T {
        return requestJson(url, object : TypeToken<T>() {}.type)
    }

    abstract suspend fun <T> requestJson(url: String, type: Type): T

}