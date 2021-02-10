package com.andrey.test.data.network

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.lang.reflect.Type
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class NetworkImpl @Inject constructor(
    private val httpClient: HttpClientProvider,
    private val gson: Gson
) : Network() {

    override suspend fun <T> requestJson(url: String, type: Type): T = withContext(IO) {
        val request = Request.Builder()
            .get()
            .url(url)
            .build()

        httpClient.get().newCall(request).await().parseToModel(type)
    }

    private suspend fun Call.await(): Response {
        return suspendCancellableCoroutine { continuation ->
            enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    continuation.resume(response)
                }

                override fun onFailure(call: Call, e: IOException) {
                    if (continuation.isCancelled) return
                    continuation.resumeWithException(e)
                }
            })

            continuation.invokeOnCancellation {
                try {
                    cancel()
                } catch (ex: Throwable) {
                }
            }
        }
    }

    private suspend fun <T> Response.parseToModel(type: Type): T =
        withContext(Dispatchers.Unconfined) {
            val body = body() ?: throw NullPointerException(toString())
            gson.fromJson(body.charStream(), type)
        }

}