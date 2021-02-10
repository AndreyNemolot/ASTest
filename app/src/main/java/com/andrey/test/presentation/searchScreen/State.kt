package com.andrey.test.presentation.searchScreen

import com.andrey.test.domain.model.City

internal data class State(
    var cityList: List<City> = emptyList(),
    var cityFrom: City? = null,
    var cityTo: City? = null

)