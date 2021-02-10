package com.andrey.test.domain

import com.andrey.test.domain.model.City

interface CityInteractor {

    suspend fun getCityList(term: String, lang: String): List<City>

    suspend fun getFirstSuitableCity(term: String, lang: String): City?


}