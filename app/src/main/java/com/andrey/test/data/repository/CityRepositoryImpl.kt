package com.andrey.test.data.repository

import androidx.core.net.toUri
import com.andrey.test.data.ApiData.CITY_SERVICE_URL
import com.andrey.test.data.model.CityEntity
import com.andrey.test.data.model.CityResponse
import com.andrey.test.data.network.Network
import com.andrey.test.domain.model.City
import javax.inject.Inject

class CityRepositoryImpl @Inject constructor(
    private val network: Network
) : CityRepository {

    override suspend fun getCityList(term: String, lang: String): List<City>? {
        val url = CITY_SERVICE_URL.toUri().buildUpon()
            .appendPath("autocomplete")
            .appendQueryParameter("term", term)
            .appendQueryParameter("lang", lang)
            .build().toString()
        return network.get<CityResponse>(url).cities?.mapNotNull { it.mapToDomain() }
    }

    private fun CityEntity?.mapToDomain(): City? {
        if (this == null || latinCity == null || location == null) return null
        return City(
            latinFullName = latinFullName,
            latinCityName = latinCity,
            location = location,
            nearestAirport = iata?.firstOrNull()
        )
    }
}