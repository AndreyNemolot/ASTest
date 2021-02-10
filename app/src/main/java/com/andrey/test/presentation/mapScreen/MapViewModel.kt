package com.andrey.test.presentation.mapScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrey.test.domain.model.City
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class MapViewModel @Inject constructor() : ViewModel() {

    private val _commandFlow = MutableSharedFlow<Command>()
    internal val commandFlow = _commandFlow.asSharedFlow()

    private var lastStartPoint: LatLng? = null
    private var spentTime = 0L


    fun saveCityAndStart(cityFrom: City, cityTo: City) {
        viewModelScope.launch {
            _commandFlow.emit(
                Command.OnStartAnimation(
                    cityFrom,
                    cityTo,
                    START_DURATION - spentTime,
                    lastStartPoint
                )
            )
        }
    }

    fun saveCurrentMarkerLocation(currentLocation: LatLng, spentTime: Long) {
        this.lastStartPoint = currentLocation
        this.spentTime = spentTime
    }

    companion object {
        const val START_DURATION = 8000L
    }

}

