package com.andrey.test.domain

import com.andrey.test.data.model.CityEntity
import com.andrey.test.data.repository.CityRepository
import com.andrey.test.domain.model.City
import com.google.android.gms.maps.model.LatLng
import javax.inject.Inject

class CityInteractorImpl @Inject constructor(
    private val cityRepository: CityRepository
) : CityInteractor {
    override suspend fun getCityList(term: String, lang: String): List<City> {
        return cityRepository.getCityList(term, lang)?.mapNotNull { it.mapToDomain() }
            ?: emptyList()
    }

    override suspend fun getFirstSuitableCity(term: String, lang: String): City? {
        return cityRepository.getCityList(term, lang)?.firstOrNull()?.mapToDomain()
    }

    private fun CityEntity?.mapToDomain(): City? {
        this ?: return null
        return City(
            latinFullName = latinFullName,
            latinCityName = latinCity!!,
            location = LatLng(location?.lat!!, location.lon!!),
            nearestAirport = iata?.firstOrNull()
        )
    }
}