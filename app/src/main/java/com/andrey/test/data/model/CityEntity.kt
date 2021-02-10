package com.andrey.test.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CityEntity(
    val latinFullName: String? = null,
    val location: Location? = null,
    val latinCity: String? = null
) : Parcelable {

    @Parcelize
    data class Location(
        val lat: Double? = null,
        val lon: Double? = null
    ) : Parcelable
}


