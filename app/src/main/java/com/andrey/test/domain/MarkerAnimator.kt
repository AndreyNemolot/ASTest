package com.andrey.test.domain

import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.util.Property
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker


object MarkerAnimator {

    fun animateMarker(
        marker: Marker,
        finalPosition: LatLng?,
        duration: Long,
        saveLastMarkerData: (LatLng, Long) -> Unit = { _: LatLng, _: Long -> }
    ) {
        var lastPosition: LatLng? = null
        val startTimeStamp = System.currentTimeMillis()
        val typeEvaluator: TypeEvaluator<LatLng> =
            TypeEvaluator { fraction, startValue, endValue ->
                val newEnd =
                    LatLngInterpolator.Spherical().interpolate(fraction, startValue, endValue)
                val angle = if (lastPosition == null) {
                    getBearing(startValue, newEnd)
                } else {
                    getBearing(lastPosition!!, newEnd)
                }
                lastPosition = newEnd
                marker.rotation = angle
                val currentTimeStamp = System.currentTimeMillis()

                saveLastMarkerData(newEnd, currentTimeStamp - startTimeStamp)
                newEnd
            }
        val property = Property.of(
            Marker::class.java,
            LatLng::class.java, "position"
        )
        val animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition)
        animator.duration = duration
        animator.start()
    }

    private fun getBearing(begin: LatLng, end: LatLng): Float {
        val lat = Math.abs(begin.latitude - end.latitude)
        val lng = Math.abs(begin.longitude - end.longitude)

        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return Math.toDegrees(Math.atan(lng / lat)).toFloat()
        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (90 - Math.toDegrees(Math.atan(lng / lat)) + 90).toFloat()
        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (Math.toDegrees(Math.atan(lng / lat)) + 180).toFloat()
        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (90 - Math.toDegrees(Math.atan(lng / lat)) + 270).toFloat()
        return -1f
    }

}