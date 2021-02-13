package com.andrey.test.domain

import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.util.Property
import androidx.core.animation.doOnEnd
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.SphericalUtil


class MarkerAnimator(
    private val marker: Marker,
    startPosition: LatLng,
    private val finalPosition: LatLng,
    private val duration: Long,
    private val callback: AnimationCallback?
) {
    var lastPosition = startPosition

    fun animateMarker() {
        val sphericalInterpolator = LatLngInterpolator.Spherical()
        val property = Property.of(Marker::class.java, LatLng::class.java, PROPERTY_NAME)

        val typeEvaluator: TypeEvaluator<LatLng> = TypeEvaluator { fraction, startValue, endValue ->
            val newPointPosition = sphericalInterpolator.interpolate(
                fraction,
                startValue,
                endValue
            )
            val angle = SphericalUtil.computeHeading(lastPosition, newPointPosition)
            lastPosition = newPointPosition
            marker.rotation = angle.toFloat()
            callback?.lastPosition(lastPosition)
            newPointPosition
        }

        val animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition)
        animator.duration = duration
        animator.doOnEnd {
            callback?.onLoadingEnded()
        }
        animator.start()
    }

    interface AnimationCallback {
        fun onLoadingEnded()
        fun lastPosition(position: LatLng)
    }

    companion object {
        private const val PROPERTY_NAME = "position"
    }

}