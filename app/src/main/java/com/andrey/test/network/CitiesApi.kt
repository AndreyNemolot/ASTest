package com.andrey.test.network

import com.andrey.test.model.CitiesResponseObject
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Call

interface CitiesApi {
    @GET("autocomplete")
    fun getCityList(
        @Query("term") term: String,
        @Query("lang") lang: String
    ): Call<CitiesResponseObject>
}