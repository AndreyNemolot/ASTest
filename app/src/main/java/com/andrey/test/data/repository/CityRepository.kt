package com.andrey.test.data.repository

import com.andrey.test.data.model.CityEntity

interface CityRepository {

    suspend fun getCityList(term: String, lang: String): List<CityEntity>?
}