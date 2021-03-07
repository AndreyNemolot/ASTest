package com.andrey.test.presentation.searchScreen

import com.andrey.test.presentation.model.CityViewModel

internal data class State(
    val cityListFrom: List<CityViewModel> = emptyList(),
    val cityListTo: List<CityViewModel> = emptyList(),
    val cityFrom: CityViewModel? = null,
    val cityTo: CityViewModel? = null,
    val isNetworkAvailable: Boolean = true
)