package com.andrey.test.presentation.searchScreen

import androidx.lifecycle.*
import com.andrey.test.domain.CityInteractor
import com.andrey.test.domain.model.City
import com.andrey.test.domain.model.Direction
import com.andrey.test.presentation.model.CityViewModel
import com.andrey.test.presentation.network.NetworkConnectivityManager
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

class SearchViewModel @Inject constructor(
    private val cityInteractor: CityInteractor,
    networkManager: NetworkConnectivityManager
) : ViewModel() {

    private var job: Job? = null
    private var state: State = State()
    private val _stateFlow = MutableStateFlow(State())
    internal val stateFlow = _stateFlow.asStateFlow()
    private val _commandFlow = MutableSharedFlow<Command>()
    internal val commandFlow = _commandFlow.asSharedFlow()

    init {
        networkManager.observe().onEach { isNetworkAvailable ->
            updateViewState { state.copy(isNetworkAvailable = isNetworkAvailable) }
        }.launchIn(viewModelScope)
    }

    fun sendQuery(query: String, direction: Direction) {
        job?.cancel()
        job = viewModelScope.launch {
            getCityList(query, direction)
        }
    }

    private suspend fun getCityList(searchString: String, direction: Direction) {
        if (!state.isNetworkAvailable) {
            _commandFlow.emit(Command.OnInternetUnailable)
            return
        }
        try {
            val cityList =
                cityInteractor.getCityList(searchString, LANG).map { it.toCityViewModel() }
            updateViewState {
                when (direction) {
                    Direction.FROM -> {
                        state.copy(cityListFrom = cityList)
                    }
                    Direction.TO -> {
                        state.copy(cityListTo = cityList)

                    }
                }
            }
        } catch (e: Exception) {
            _commandFlow.emit(Command.OnShowError)
        }
    }

    private fun updateViewState(state0: (State) -> State) {
        state = state0(state)
        _stateFlow.tryEmit(state)
    }

    fun saveChosenCityFrom(city: CityViewModel) {
        state = state.copy(cityFrom = city)
    }

    fun saveChosenCityTo(city: CityViewModel) {
        state = state.copy(cityTo = city)
    }

    fun validateCities(inputedCityFrom: String, inputedCityTo: String) {
        job?.cancel()
        job = viewModelScope.launch {
            if (inputedCityFrom.isBlank() || inputedCityTo.isBlank()) {
                _commandFlow.emit(Command.OnShowInputCitiesMessage)
                return@launch
            }

            if (inputedCityFrom != state.cityFrom?.latinCityName) {
                val city = getCity(inputedCityFrom)
                if (city != null) {
                    state = state.copy(cityFrom = city)
                } else {
                    _commandFlow.emit(Command.OnShowMissingCityMessage(inputedCityFrom))
                    return@launch
                }
            }
            if (inputedCityTo != state.cityTo?.latinCityName) {
                val city = getCity(inputedCityTo)
                if (city != null) {
                    state = state.copy(cityTo = city)
                } else {
                    _commandFlow.emit(Command.OnShowMissingCityMessage(inputedCityTo))
                    return@launch
                }
            }
            val cityFrom = state.cityFrom
            val cityTo = state.cityTo
            if (cityFrom == null || cityTo == null) {
                _commandFlow.emit(Command.OnShowError)
            } else {
                _commandFlow.emit(Command.OnSearchFlight(cityFrom, cityTo))
            }
        }
    }

    private suspend fun getCity(cityName: String): CityViewModel? {
        return try {
            cityInteractor.getFirstSuitableCity(cityName, LANG)?.toCityViewModel()
        } catch (e: Exception) {
            if (e is IOException) {
                _commandFlow.emit(Command.OnInternetUnailable)
            }
            null
        }
    }

    fun changeDirection() {
        state = state.copy(cityFrom = state.cityTo, cityTo = state.cityFrom)
    }

    private fun City.toCityViewModel(): CityViewModel {
        return CityViewModel(
            latinFullName = latinFullName,
            location = LatLng(location.lat, location.lon),
            latinCityName = latinCityName,
            nearestAirport = nearestAirport
        )
    }

    companion object {
        private const val LANG = "ru"
    }

}
