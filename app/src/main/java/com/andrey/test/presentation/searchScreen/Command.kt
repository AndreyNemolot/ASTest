package com.andrey.test.presentation.searchScreen

import com.andrey.test.domain.model.City

internal sealed class Command {
    object OnInternetUnailable : Command()
    object OnShowError : Command()
    object OnShowInputCitiesMessage : Command()
    class OnShowMissingCityMessage(val cityName: String) : Command()
    class OnSearchFlight(val cityFrom: City, val cityTo: City) : Command()
}