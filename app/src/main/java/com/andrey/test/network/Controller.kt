package com.andrey.test.network

import com.andrey.test.model.CitiesResponseObject
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Controller {
    private val BASE_URL = "https://yasen.hotellook.com/"
    private var retrofit: Retrofit

    init {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

    }

    fun getCityList(term: String, lang: String): Call<CitiesResponseObject> {
        val cityApi = retrofit.create(CitiesApi::class.java)

        return cityApi.getCityList(term, lang)
    }

}