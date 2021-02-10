package com.andrey.test.presentation.searchScreen

import androidx.lifecycle.*
import com.andrey.test.domain.CityInteractor
import com.andrey.test.domain.NetworkConnectivityManager
import com.andrey.test.domain.model.City
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class SearchViewModel @Inject constructor(
    private val cityInteractor: CityInteractor,
    private val networkManager: NetworkConnectivityManager
) : ViewModel() {

    private var state: State = State()
    private val _stateFlow = MutableStateFlow(State())
    internal val stateFlow = _stateFlow.asStateFlow()
    private val _commandFlow = MutableSharedFlow<Command>()
    internal val commandFlow = _commandFlow.asSharedFlow()

    private var job: Job? = null

    init {
        networkManager.observe().onEach {
            _commandFlow.emit(Command.OnInternetAvailable(it))
        }.launchIn(viewModelScope)
    }

    fun sendQuery(query: String) {
        job?.cancel()
        job = viewModelScope.launch {
            getCityList(query)
        }
    }

    private suspend fun getCityList(searchString: String) {
        if (!networkManager.get()) return
        try {
            val cityList = cityInteractor.getCityList(searchString, LANG)
            updateViewState {
                state.copy(cityList = cityList)
            }
        } catch (e: Exception) {
            _commandFlow.emit(Command.OnShowError)
        }
    }

    private fun updateViewState(state0: (State) -> State) {
        state = state0(state)
        _stateFlow.tryEmit(state)
    }

    fun saveChosenCityFrom(city: City) {
        state = state.copy(cityFrom = city)
    }

    fun saveChosenCityTo(city: City) {
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
                val city = cityInteractor.getFirstSuitableCity(inputedCityFrom, LANG)
                if (city != null) {
                    state = state.copy(cityFrom = city)
                } else {
                    _commandFlow.emit(Command.OnShowMissingCityMessage(inputedCityFrom))
                    return@launch

                }
            }
            if (inputedCityTo != state.cityTo?.latinCityName) {
                val city = cityInteractor.getFirstSuitableCity(inputedCityTo, LANG)
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

    fun changeDirection() {
        state = state.copy(cityFrom = state.cityTo, cityTo = state.cityFrom)
    }

    companion object {
        const val LANG = "ru"
    }

}
