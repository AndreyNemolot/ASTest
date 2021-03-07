package com.andrey.test.data.model

import android.os.Parcelable
import com.andrey.test.domain.model.Location
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CityEntity(
    val latinFullName: String? = null,
    val location: Location? = null,
    val latinCity: String? = null,
    val iata: List<String>? = null
) : Parcelable


