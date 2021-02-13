package com.andrey.test.presentation.searchScreen

import com.andrey.test.domain.model.City

internal data class State(
    val cityListFrom: List<City> = emptyList(),
    val cityListTo: List<City> = emptyList(),
    val cityFrom: City? = null,
    val cityTo: City? = null,
    val isNetworkAvailable: Boolean = true
)