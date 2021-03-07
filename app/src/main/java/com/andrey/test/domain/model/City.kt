package com.andrey.test.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class City(
    val latinFullName: String? = null,
    var location: Location,
    val latinCityName: String,
    val nearestAirport: String? = null
) : Parcelable

@Parcelize
data class Location(
    val lat: Double,
    val lon: Double
) : Parcelable