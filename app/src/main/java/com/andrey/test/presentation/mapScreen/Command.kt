package com.andrey.test.presentation.mapScreen

import com.andrey.test.domain.model.City
import com.google.android.gms.maps.model.LatLng

internal sealed class Command {
    class OnStartAnimation(
        val cityFrom: City,
        val cityTo: City,
        val duration: Long,
        val lastPosition: LatLng?
    ) : Command()
}