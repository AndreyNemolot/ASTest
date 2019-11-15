package com.andrey.test

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.andrey.test.model.City
import java.util.ArrayList
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.Color
import android.icu.text.CollationKey
import android.os.Handler
import android.view.animation.LinearInterpolator
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil
import com.google.android.gms.maps.model.LatLngBounds




class MapViewModel : ViewModel() {

    var cityFrom = City()
    var cityTo = City()
    lateinit var mMap: GoogleMap
    private var isLoading = false
    private lateinit var handler: Handler
    private val ANIMATION_DELAY = 250L
    private var errorHandle = MutableLiveData<String>()

    var startPoint = LatLng(0.0, 0.0)
    var endPoint = LatLng(0.0, 0.0)

    var newPolylineList = ArrayList<LatLng>()

    fun errorHandler(): LiveData<String> {
        return errorHandle
    }

    fun saveCityFrom(city: City) {
        this.cityFrom = city
        startPoint = (LatLng(city.location.lat, city.location.lon))
    }

    fun saveCityTo(city: City) {
        this.cityTo = city
        endPoint = (LatLng(city.location.lat, city.location.lon))

    }

    fun startLoading() {

        if (!isLoading) {
            isLoading = true

            calculateCustomPolyLinesPath()
            var i = 0
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(getCameraBounds(), 10))

            handler = Handler()
            handler.postDelayed(object : Runnable {
                override fun run() {
                    loading(newPolylineList[i], newPolylineList[i + 1])
                    i++
                    if (i < newPolylineList.size - 1) {
                        handler.postDelayed(this, ANIMATION_DELAY)
                    } else {
                        loadingDone()

                    }
                }
            }, ANIMATION_DELAY)
        }

    }

    private fun getCameraBounds(): LatLngBounds{
        val builder = LatLngBounds.Builder()
        builder.include(startPoint)
        builder.include(endPoint)
        return builder.build()
    }


    private fun calculateCustomPolyLinesPath() {
        val step = 0.01f
        var v = 0.0f
        while (v < 1.0f) {
            newPolylineList.add(
                PathCalculation.LatLngInterpolatorNew
                    .LinearFixed().interpolate(v, startPoint, endPoint)
            )
            v += step
        }
    }

    private fun loading(start: LatLng, dest: LatLng) {
        val polyLine = createTravelPolyLine()
        mMap.addPolyline(polyLine)
        val airplaneMarker = setAirplaneMarkerOnMap()
        animateMarker(start, dest, airplaneMarker)

    }

    private fun createTravelPolyLine(): PolylineOptions {
        return PolylineOptions()
            .addAll(newPolylineList)
            .color(Color.GRAY)
            .width(10.0f)

    }

    private fun setAirplaneMarkerOnMap(): Marker {
        val pathCalculation = PathCalculation()
        return mMap.addMarker(
            MarkerOptions().position(
                LatLng(
                    startPoint.latitude,
                    startPoint.longitude
                )
            ).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_plane)).rotation(
                pathCalculation.getBearing(startPoint, endPoint)
            )
        )
    }


    private fun animateMarker(startPosition: LatLng, destination: LatLng, marker: Marker?) {

        if (marker != null) {
            val pathCalculation = PathCalculation()
            val latLngInterpolator = PathCalculation.LatLngInterpolatorNew.LinearFixed()

            val valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
            valueAnimator.duration = ANIMATION_DELAY + 10
            valueAnimator.interpolator = LinearInterpolator()
            valueAnimator.addUpdateListener { animation ->
                try {
                    val v = animation.animatedFraction
                    val newPosition = latLngInterpolator.interpolate(v, startPosition, destination)
                    marker.position = newPosition
                    marker.rotation = pathCalculation.getBearing(
                        startPosition,
                        destination
                    )
                } catch (ex: Exception) {
                    errorHandle.postValue("Что то пошло не так ${ex.message}")
                }
            }
            valueAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    marker.remove()
                }
            })
            valueAnimator.start()
        }
    }

    private fun loadingDone(){
        isLoading = false
    }


}