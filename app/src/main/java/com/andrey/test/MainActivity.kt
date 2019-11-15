package com.andrey.test

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.andrey.test.adapter.AutoSuggestAdapter
import com.andrey.test.model.CitiesResponseObject
import com.andrey.test.model.City
import com.andrey.test.model.Direction
import com.andrey.test.network.NetworkConnectivityManager
import com.andrey.test.viewModel.SearchViewModel


class MainActivity : AppCompatActivity() {

    private lateinit var citiesAdapterFrom: AutoSuggestAdapter
    private lateinit var citiesAdapterTo: AutoSuggestAdapter
    private val networkManager = NetworkConnectivityManager()


    private var searchViewModel = SearchViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchViewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)

        citiesAdapterFrom = AutoSuggestAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line
        )
        citiesAdapterTo = AutoSuggestAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line
        )

        registerOnError()

        autoSuggestClickHandlerFrom()
        autoSuggestClickHandlerTo()

        createAutoSuggestListenerFrom()
        createAutoSuggestListenerTo()

        getCitiesFromList()
        getCitiesToList()
    }

    override fun onStart() {
        super.onStart()
        connectionCheck()
    }

    private fun connectionCheck() {
        applicationContext
        networkManager.subscribeOnConnectionUnavailable(this,
            Runnable {
                searchViewModel.setConnectionAvailable(false)
                showConnectionState(false)
            })

        networkManager.subscribeOnConnectionAvailable(this,
            Runnable {
                searchViewModel.setConnectionAvailable(true)
                showConnectionState(true)
            })
    }

    private fun showConnectionState(isAvailable: Boolean) {
        if (isAvailable) {
            internetStateMessage.visibility = View.GONE
        } else {
            internetStateMessage.visibility = View.VISIBLE
        }
    }

    private fun autoSuggestClickHandlerFrom() {
        autoCompleteCityFrom.setAdapter(citiesAdapterFrom)
        autoCompleteCityFrom.setOnItemClickListener { _, _, p2, _ ->
            searchViewModel.saveChosenCityFrom(citiesAdapterFrom.getObject(p2) as City)
            hideKeyboard()
            autoCompleteCityFrom.clearFocus()
        }
    }

    private fun autoSuggestClickHandlerTo() {
        autoCompleteCityTo.setAdapter(citiesAdapterTo)
        autoCompleteCityTo.setOnItemClickListener { _, _, p2, _ ->
            searchViewModel.saveChosenCityTo(citiesAdapterTo.getObject(p2) as City)
            hideKeyboard()
            autoCompleteCityTo.clearFocus()
        }
    }

    private fun createAutoSuggestListenerFrom() {
        autoCompleteCityFrom.setAdapter(citiesAdapterFrom)
        autoCompleteCityFrom.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {
                searchViewModel.sendQuery(autoCompleteCityFrom.text.toString(), Direction.FROM)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun createAutoSuggestListenerTo() {
        autoCompleteCityTo.setAdapter(citiesAdapterTo)
        autoCompleteCityTo.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(editable: Editable) {
                searchViewModel.sendQuery(autoCompleteCityTo.text.toString(), Direction.TO)

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }


    private fun registerOnError() {
        searchViewModel.errorHandler().observe(this, Observer<String> {
            showError(it)
        })
    }

    private fun showError(errorText: String) {
        Toast.makeText(this, errorText, Toast.LENGTH_SHORT).show()

    }

    private fun getCitiesFromList() {
        searchViewModel.getCitiesFrom().observe(this, Observer<CitiesResponseObject> {
            showCitiesListFrom(it)
        })
    }

    private fun getCitiesToList() {
        searchViewModel.getCitiesTo().observe(this, Observer<CitiesResponseObject> {
            showCitiesListTo(it)
        })
    }

    private fun showCitiesListFrom(citiesObjectFrom: CitiesResponseObject) {
        citiesAdapterFrom.setData(citiesObjectFrom.cities)
        citiesAdapterFrom.notifyDataSetChanged()
    }

    private fun showCitiesListTo(citiesObjectFrom: CitiesResponseObject) {
        citiesAdapterTo.setData(citiesObjectFrom.cities)
        citiesAdapterTo.notifyDataSetChanged()

    }

    fun searchFlight(view: View) {
        if (searchViewModel.isCityInputed(
                autoCompleteCityFrom.text.toString(),
                autoCompleteCityTo.text.toString()
            )
        ) {
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra("cityFrom", searchViewModel.cityFrom)
            intent.putExtra("cityTo", searchViewModel.cityTo)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Выберите города", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideKeyboard() {
        val imm = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = this.currentFocus
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun changeDirections(view: View) {
        changeDirections.startAnimation(
            AnimationUtils.loadAnimation(
                this,
                R.anim.rotate_indefinitely
            )
        )

        searchViewModel.changeDirections()
        autoCompleteCityFrom.setText(searchViewModel.cityFrom.latinCity)
        autoCompleteCityTo.setText(searchViewModel.cityTo.latinCity)
    }

    override fun onStop() {
        super.onStop()
        val manager = NetworkConnectivityManager()
        manager.unregister(this)
    }
}
