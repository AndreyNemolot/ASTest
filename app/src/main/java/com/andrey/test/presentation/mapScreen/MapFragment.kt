package com.andrey.test.presentation.mapScreen

import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
import kotlinx.android.parcel.Parcelize
import ru.terrakok.cicerone.android.support.SupportAppScreen

class MapFragment : SupportMapFragment(), OnMapReadyCallback, MarkerAnimator.AnimationCallback {

    lateinit var googleMap: GoogleMap
    lateinit var viewModel: MapViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = obtainViewModel()
        viewModel.commandFlow.observeOn(lifecycleScope) {
            handleCommand(it)
        }
        getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        arguments?.let {
            val cityFrom = requireNotNull(it.getParcelable<City>(CITY_FROM_KEY))
            val cityTo = requireNotNull(it.getParcelable<City>(CITY_TO_KEY))
            viewModel.startLoading(cityFrom, cityTo)
        }
    }

    private fun handleCommand(command: Command) {
        when (command) {
            is Command.OnStartAnimation -> {
                prepareLoading(command.cityFrom, command.cityTo)
                loading(
                    command.lastPosition ?: command.cityFrom.location,
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
        MarkerAnimator(
            airplaneMarker,
            startPosition,
            endPosition,
            duration,
            this
        ).animateMarker()
    }

    private fun drawCityNameMarker(city: City) {
        val icg = IconGenerator(requireContext())
        val bm = icg.apply {
            setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.badge_marker))
            setTextAppearance(R.style.text_white_18)
        }.makeIcon(city.markerName)
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
        val pattern: List<PatternItem> = listOf(Dot(), Gap(POLYLINE_GAP))
        val polyLine = PolylineOptions()
            .addAll(listOf(cityFromLatLng, cityToLatLng))
            .color(Color.GRAY)
            .width(POLYLINE_WIDTH)
            .pattern(pattern)
            .geodesic(true)
        googleMap.addPolyline(polyLine)
    }

    private fun drawAirplaneMarker(location: LatLng): Marker {
        val markerOption = MarkerOptions()
            .position(location)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_plane))
            .anchor(MARKER_ANCHOR, MARKER_ANCHOR)
        return googleMap.addMarker(markerOption)
    }

    override fun onLoadingEnded() {
        // use for end of loading action
    }

    override fun lastPosition(position: LatLng) {
        viewModel.saveLastPosition(position)
    }

    companion object {
        private const val MARKER_ANCHOR = 0.5f
        private const val POLYLINE_WIDTH = 20.0f
        private const val POLYLINE_GAP = 20f
        const val CITY_FROM_KEY = "cityFrom"
        const val CITY_TO_KEY = "cityTo"

        @JvmStatic
        fun newInstance(cityFrom: City, cityTo: City) =
            MapFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(CITY_FROM_KEY, cityFrom)
                    putParcelable(CITY_TO_KEY, cityTo)
                }
            }
    }

    @Parcelize
    class Screen(private val cityFrom: City, private val cityTo: City) : SupportAppScreen(),
        Parcelable {
        override fun getFragment(): Fragment {
            return newInstance(cityFrom, cityTo)
        }
    }

}