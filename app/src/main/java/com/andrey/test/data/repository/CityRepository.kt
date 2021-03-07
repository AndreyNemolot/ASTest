package com.andrey.test.data.repository

import com.andrey.test.domain.model.City

interface CityRepository {

    suspend fun getCityList(term: String, lang: String): List<City>?
}