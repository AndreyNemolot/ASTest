package com.andrey.test.presentation.mapScreen

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.andrey.test.R
import com.andrey.test.domain.MarkerAnimator
import com.andrey.test.domain.model.City
import com.andrey.test.presentation.observeOn
import com.andrey.test.presentation.obtainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.ui.IconGenerator


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var viewModel: MapViewModel
    lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        viewModel = obtainViewModel()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)

        viewModel.commandFlow.observeOn(lifecycleScope) {
            handleCommand(it)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        val cityFrom = requireNotNull(intent.getParcelableExtra<City>(CITY_FROM_KEY))
        val cityTo = requireNotNull(intent.getParcelableExtra<City>(CITY_TO_KEY))
        viewModel.saveCityAndStart(cityFrom, cityTo)
    }

    private fun handleCommand(command: Command) {
        when (command) {
            is Command.OnStartAnimation -> {
                prepareLoading(command.cityFrom, command.cityTo)
                loading(
                    command.startPoint ?: command.cityFrom.location,
                    command.cityTo.location,
                    command.duration
                )
            }
        }
    }

    private fun prepareLoading(cityFrom: City, cityTo: City) {
        drawCityNameMarker(cityFrom)
        drawCityNameMarker(cityTo)
        drawTravelPath(cityFrom.location, cityTo.location)
        moveCameraToPath(cityFrom.location, cityTo.location)
    }

    private fun loading(startPosition: LatLng, endPosition: LatLng, duration: Long) {
        val airplaneMarker = drawAirplaneMarker(startPosition)
        MarkerAnimator.animateMarker(
            airplaneMarker,
            endPosition,
            duration
        ) { lastPoint, spentTime ->
            viewModel.saveCurrentMarkerLocation(lastPoint, spentTime)
        }
    }


    private fun drawCityNameMarker(city: City) {
        val icg = IconGenerator(this)
        val bm = icg.apply {
            setBackground(ContextCompat.getDrawable(baseContext, R.drawable.badge_marker))
            setTextAppearance(R.style.text_white_18)
        }.makeIcon(city.latinCityName)
        googleMap.addMarker(
            MarkerOptions().position(
                LatLng(
                    city.location.latitude,
                    city.location.longitude
                )
            ).icon(BitmapDescriptorFactory.fromBitmap(bm))
        )
    }

    private fun moveCameraToPath(cityFromLatLng: LatLng, cityToLatLng: LatLng) {
        val bounds = LatLngBounds.Builder().apply {
            include(cityFromLatLng)
            include(cityToLatLng)
        }.build()
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val padding = (width * 0.15).toInt()
        val cameraFactory = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding)
        googleMap.animateCamera(cameraFactory)
    }

    private fun drawTravelPath(cityFromLatLng: LatLng, cityToLatLng: LatLng) {
        val pattern: List<PatternItem> = listOf(Dot(), Gap(20f))
        val polyLine = PolylineOptions()
            .addAll(listOf(cityFromLatLng, cityToLatLng))
            .color(Color.GRAY)
            .width(20.0f)
            .pattern(pattern)
            .geodesic(true)
        googleMap.addPolyline(polyLine)
    }

    private fun drawAirplaneMarker(location: LatLng): Marker {
        val markerOption = MarkerOptions()
            .position(location)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_plane))
            .anchor(0.5f, 0.5f)
        return googleMap.addMarker(markerOption)
    }

    companion object {
        const val CITY_FROM_KEY = "cityFrom"
        const val CITY_TO_KEY = "cityTo"
    }

}
