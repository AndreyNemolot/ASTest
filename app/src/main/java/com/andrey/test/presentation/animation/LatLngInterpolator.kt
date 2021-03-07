package com.andrey.test.presentation.animation

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil

interface LatLngInterpolator {
    fun interpolate(fraction: Float, from: LatLng?, to: LatLng?): LatLng?
    class Spherical : LatLngInterpolator {
        override fun interpolate(fraction: Float, from: LatLng?, to: LatLng?): LatLng {
            return SphericalUtil.interpolate(from, to, fraction.toDouble())
        }
    }
}
