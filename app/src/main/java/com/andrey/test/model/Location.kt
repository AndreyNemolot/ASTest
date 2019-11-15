package com.andrey.test.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Location(
    val lat: Double=0.0,
    val lon: Double=0.0
): Parcelable