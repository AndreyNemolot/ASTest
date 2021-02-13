package com.andrey.test.domain.model

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.parcel.Parcelize

@Parcelize
data class City(
    val latinFullName: String? = null,
    var location: LatLng,
    val latinCityName: String,
    val nearestAirport: String? = null
) : Parcelable {
    val markerName = nearestAirport ?: latinCityName
}

