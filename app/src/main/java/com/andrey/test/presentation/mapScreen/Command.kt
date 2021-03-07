package com.andrey.test.presentation.mapScreen

import com.andrey.test.presentation.model.CityViewModel
import com.google.android.gms.maps.model.LatLng

internal sealed class Command {
    class OnStartAnimation(
        val cityFrom: CityViewModel,
        val cityTo: CityViewModel,
        val duration: Long,
        val lastPosition: LatLng?
    ) : Command()
}