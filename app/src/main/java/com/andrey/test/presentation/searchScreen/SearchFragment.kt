package com.andrey.test.presentation.searchScreen

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.andrey.test.R
import com.andrey.test.databinding.FragmentSearchBinding
import com.andrey.test.presentation.mapScreen.MapsActivity.Companion.CITY_FROM_KEY
import com.andrey.test.presentation.mapScreen.MapsActivity.Companion.CITY_TO_KEY
import com.andrey.test.presentation.observeOn
import com.andrey.test.presentation.obtainViewModel
import com.andrey.test.presentation.searchScreen.adapter.AutoSuggestAdapter
import com.andrey.test.presentation.textChanges
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

@OptIn(FlowPreview::class)
class SearchFragment : Fragment(R.layout.fragment_search) {

    private var citiesAdapterFrom: AutoSuggestAdapter? = null
    private var citiesAdapterTo: AutoSuggestAdapter? = null

    private lateinit var viewModel: SearchViewModel
    private lateinit var binding: FragmentSearchBinding
    private lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchBinding.bind(view)
        viewModel = obtainViewModel()
        navController = NavHostFragment.findNavController(this)
        citiesAdapterFrom = AutoSuggestAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line
        ).also {
            binding.autoCompleteCityFrom.setAdapter(it)
        }

        citiesAdapterTo = AutoSuggestAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line
        ).also {
            binding.autoCompleteCityTo.setAdapter(it)
        }

        binding.changeDirections.setOnClickListener {
            viewModel.changeDirection()
            binding.changeDirections.startAnimation(
                AnimationUtils.loadAnimation(
                    requireContext(),
                    R.anim.rotate_indefinitely
                )
            )
            binding.autoCompleteCityFrom.text = binding.autoCompleteCityTo.text.also {
                binding.autoCompleteCityTo.text = binding.autoCompleteCityFrom.text
            }

        }

        binding.autoCompleteCityTo.textChanges()
            .filterNot { it.isNullOrBlank() }
            .debounce(DEBOUNCE_INPUT_TEXT)
            .onEach {
                viewModel.sendQuery(it.toString())
            }.launchIn(lifecycleScope)

        binding.autoCompleteCityFrom.textChanges()
            .filterNot { it.isNullOrBlank() }
            .debounce(DEBOUNCE_INPUT_TEXT)
            .onEach {
                viewModel.sendQuery(it.toString())
            }.launchIn(lifecycleScope)

        viewModel.stateFlow.observeOn(viewLifecycleOwner) {
            updateState(it)
        }

        viewModel.commandFlow.observeOn(viewLifecycleOwner) {
            handleCommand(it)
        }

        binding.searchFlight.setOnClickListener {
            viewModel.validateCities(
                binding.autoCompleteCityFrom.text.toString(),
                binding.autoCompleteCityTo.text.toString()
            )
        }

        autoSuggestClickHandlerFrom()
        autoSuggestClickHandlerTo()

    }

    private fun handleCommand(command: Command) {
        when (command) {
            is Command.OnInternetAvailable -> {
                showConnectionState(command.isNetworkAvailable)
            }
            is Command.OnShowError -> {
                showMessage(getString(R.string.some_error))
            }
            is Command.OnShowInputCitiesMessage -> {
                showMessage(getString(R.string.please_input_cities))
            }
            is Command.OnShowMissingCityMessage -> {
                showMessage(getString(R.string.missing_city, command.cityName))
            }
            is Command.OnSearchFlight -> {
                val bundle = bundleOf(
                    CITY_FROM_KEY to command.cityFrom,
                    CITY_TO_KEY to command.cityTo
                )
                navController.navigate(R.id.mapsActivity, bundle)
            }
        }
    }

    private fun updateState(state: State) {
        val isFromFieldFocused = binding.autoCompleteCityFrom.isCursorVisible
        val isToFieldFocused = binding.autoCompleteCityFrom.isCursorVisible
        val cityList = state.cityList

        if (cityList.isNotEmpty()) {
            if (isFromFieldFocused) {
                citiesAdapterFrom?.setData(cityList)
                citiesAdapterFrom?.notifyDataSetChanged()
            }
            if (isToFieldFocused) {
                citiesAdapterTo?.setData(cityList)
                citiesAdapterTo?.notifyDataSetChanged()
            }
        }

    }

    private fun showConnectionState(isAvailable: Boolean) {
        if (isAvailable) {
            binding.internetStateMessage.visibility = View.GONE
        } else {
            binding.internetStateMessage.visibility = View.VISIBLE
        }
    }

    private fun autoSuggestClickHandlerFrom() {
        binding.autoCompleteCityFrom.setOnItemClickListener { _, _, index, _ ->
            citiesAdapterFrom?.getItem(index)?.let { city ->
                viewModel.saveChosenCityFrom(city)
                hideKeyboard()
                binding.autoCompleteCityFrom.clearFocus()
            }
        }
    }

    private fun autoSuggestClickHandlerTo() {
        binding.autoCompleteCityTo.setOnItemClickListener { _, _, index, _ ->
            citiesAdapterFrom?.getItem(index)?.let { city ->
                viewModel.saveChosenCityTo(city)
                hideKeyboard()
                binding.autoCompleteCityFrom.clearFocus()
            }
        }
    }


    private fun showMessage(errorText: String) {
        Toast.makeText(requireContext(), errorText, Toast.LENGTH_SHORT).show()
    }

    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun onDestroy() {
        citiesAdapterFrom = null
        citiesAdapterTo = null
        super.onDestroy()
    }

    companion object {

        const val DEBOUNCE_INPUT_TEXT = 400L

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


}