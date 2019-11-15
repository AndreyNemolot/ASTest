package com.andrey.test.viewModel

import androidx.lifecycle.ViewModel
import com.andrey.test.network.Controller
import com.andrey.test.model.CitiesResponseObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.andrey.test.model.City
import com.andrey.test.model.Direction

class SearchViewModel : ViewModel() {
    var cityFrom: City = City()
    var cityTo: City = City()

    private var isConnectionAvailable = true

    private var citiesObjectFrom = MutableLiveData<CitiesResponseObject>()
    private var citiesObjectTo = MutableLiveData<CitiesResponseObject>()
    private var errorHandle = MutableLiveData<String>()
    private var citiesList = CitiesResponseObject(ArrayList())




    fun sendQuery(query: String, direction: Direction) {
        getCitiesList(query,  direction)
    }

    fun getCitiesFrom(): LiveData<CitiesResponseObject> {
        return citiesObjectFrom
    }

    fun getCitiesTo(): LiveData<CitiesResponseObject> {
        return citiesObjectTo
    }

    fun setConnectionAvailable(isConnectionAvailable: Boolean) {
        this.isConnectionAvailable = isConnectionAvailable
        if (!isConnectionAvailable) {
            internetUnavailable()
        }
    }

    private fun internetUnavailable() {
        citiesList = CitiesResponseObject(ArrayList())

    }

    fun getCitiesList(searchString: String, direction: Direction) {
        if (!isConnectionAvailable) {
            internetUnavailable()
            sendCitiesList(direction)
            return
        }
        Controller.getCityList(searchString, "ru")
            .enqueue(object : Callback<CitiesResponseObject> {
                override fun onFailure(call: Call<CitiesResponseObject>?, t: Throwable?) {
                    errorHandle.postValue("При получении списка городов возникла ошибка")
                }

                override fun onResponse(
                    call: Call<CitiesResponseObject>?,
                    response: Response<CitiesResponseObject>?
                ) {
                    if (response != null) {
                        saveCitiesList(response.body())
                        sendCitiesList(direction)
                    }
                }

            })

    }

    fun errorHandler(): LiveData<String> {
        return errorHandle
    }

    private fun saveCitiesList(
        citiesResponseObject: CitiesResponseObject
    ) {
        citiesList = citiesResponseObject
    }

    private fun sendCitiesList(direction: Direction) {
        when (direction) {
            Direction.FROM -> citiesObjectFrom.postValue(citiesList)
            Direction.TO -> citiesObjectTo.postValue(citiesList)
        }

    }

    fun saveChosenCityFrom(city: City) {
        this.cityFrom = city
        isCityInputed(cityFrom.latinCity, cityTo.latinCity)
    }

    fun saveChosenCityTo(city: City) {
        this.cityTo = city
        isCityInputed(cityFrom.latinCity, cityTo.latinCity)
    }

    fun isCityInputed(inputedCityFrom: String, inputedCityTo: String): Boolean {
        return (this.cityTo.latinCity != "" && this.cityFrom.latinCity != ""
                && this.cityTo.latinCity == inputedCityTo && this.cityFrom.latinCity == inputedCityFrom
                && inputedCityFrom!=inputedCityTo)
    }

    fun changeDirections() {
        val cityBuf = cityFrom
        cityFrom = cityTo
        cityTo = cityBuf
    }


}
