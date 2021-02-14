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
    private var lastPosition: LatLng? = null
    private var spentTime: Long = 0

    fun startLoading(cityFrom: City, cityTo: City) {
        viewModelScope.launch {
            _commandFlow.emit(
                Command.OnStartAnimation(
                    cityFrom,
                    cityTo,
                    DURATION - spentTime,
                    lastPosition
                )
            )
        }
    }

    fun saveLastAnimationData(position: LatLng, spentTime: Long) {
        this.lastPosition = position
        this.spentTime = spentTime
    }

    companion object {
        const val DURATION = 8000L
    }

}

