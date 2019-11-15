package com.andrey.test.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class City (
    val latinFullName: String = "",
    val location: Location =Location(),
    val latinCity: String=""
):Parcelable{
    override fun toString(): String {
        return latinCity
    }
}


