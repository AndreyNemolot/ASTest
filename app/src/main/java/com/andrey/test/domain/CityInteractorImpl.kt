package com.andrey.test.domain

import com.andrey.test.data.repository.CityRepository
import com.andrey.test.domain.model.City
import javax.inject.Inject

class CityInteractorImpl @Inject constructor(
    private val cityRepository: CityRepository
) : CityInteractor {
    override suspend fun getCityList(term: String, lang: String): List<City> {
        return cityRepository.getCityList(term, lang) ?: emptyList()
    }

    override suspend fun getFirstSuitableCity(term: String, lang: String): City? {
        return cityRepository.getCityList(term, lang)?.firstOrNull()
    }

}