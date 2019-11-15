package com.andrey.test

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.andrey.test.model.City

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.ui.IconGenerator

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mapViewModel = MapViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        mapViewModel = ViewModelProviders.of(this).get(MapViewModel::class.java)
        registerOnError()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mapViewModel.saveCityFrom(intent.getParcelableExtra("cityFrom") as City)
        mapViewModel.saveCityTo(intent.getParcelableExtra("cityTo") as City)


    }

    private fun registerOnError() {
        mapViewModel.errorHandler().observe(this, Observer<String> {
            showError(it)
        })
    }

    private fun showError(errorText: String) {
        Toast.makeText(this, errorText, Toast.LENGTH_SHORT).show()
    }

    private fun setTextMarkerOnMap(coordinate: LatLng, markerText: String) {
        val icg = IconGenerator(this)
        icg.setColor(R.color.colorLightGray)
        icg.setTextAppearance(R.style.text_white_16)
        val bm = icg.makeIcon(markerText)
        mapViewModel.mMap.addMarker(
            MarkerOptions().position(
                LatLng(
                    coordinate.latitude,
                    coordinate.longitude
                )
            ).icon(BitmapDescriptorFactory.fromBitmap(bm))
        )
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mapViewModel.mMap = googleMap
        mapViewModel.startLoading()
        setTextMarkerOnMap(mapViewModel.startPoint, mapViewModel.cityFrom.latinCity)
        setTextMarkerOnMap(mapViewModel.endPoint, mapViewModel.cityTo.latinCity)

    }


}
