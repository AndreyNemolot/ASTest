package com.andrey.test.presentation.searchScreen

import android.app.Activity
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.andrey.test.R
import com.andrey.test.databinding.FragmentSearchBinding
import com.andrey.test.di.Scope
import com.andrey.test.domain.model.Direction
import com.andrey.test.presentation.mapScreen.MapFragment
import com.andrey.test.presentation.observeOn
import com.andrey.test.presentation.obtainViewModel
import com.andrey.test.presentation.router.AppRouter
import com.andrey.test.presentation.showMessage
import com.andrey.test.presentation.textChanges
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.terrakok.cicerone.android.support.SupportAppScreen
import toothpick.Toothpick
import javax.inject.Inject


@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchFragment : Fragment(R.layout.fragment_search) {

    private var citiesAdapterFrom: AutoSuggestAdapter? = null
    private var citiesAdapterTo: AutoSuggestAdapter? = null

    private lateinit var viewModel: SearchViewModel
    private lateinit var binding: FragmentSearchBinding

    @Inject
    lateinit var router: AppRouter

    override fun onCreate(savedInstanceState: Bundle?) {
        val presentationScope = Toothpick.openScopes(Scope.PRESENTATION)
        Toothpick.inject(this, presentationScope)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchBinding.bind(view)
        viewModel = obtainViewModel()
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

        binding.autoCompleteCityFrom.textChanges()
            .filterNot { it.isNullOrBlank() }
            .debounce(DEBOUNCE_INPUT_TEXT)
            .onEach { text ->
                viewModel.sendQuery(text.toString(), Direction.FROM)
            }.launchIn(lifecycleScope)

        binding.autoCompleteCityTo.textChanges()
            .filterNot { it.isNullOrBlank() }
            .debounce(DEBOUNCE_INPUT_TEXT)
            .onEach { text ->
                viewModel.sendQuery(text.toString(), Direction.TO)
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
            is Command.OnInternetUnailable -> {
                showMessage(getString(R.string.internet_unavailable))
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
                router.navigateTo(MapFragment.Screen(command.cityFrom, command.cityTo))
            }
        }
    }

    private fun updateState(state: State) {
        val isFromFieldFocused = binding.autoCompleteCityFrom.isCursorVisible
        val isToFieldFocused = binding.autoCompleteCityTo.isCursorVisible
        showConnectionState(state.isNetworkAvailable)

        if (isFromFieldFocused) {
            citiesAdapterFrom?.setData(state.cityListFrom)
        }
        if (isToFieldFocused) {
            citiesAdapterTo?.setData(state.cityListTo)
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
            citiesAdapterTo?.getItem(index)?.let { city ->
                viewModel.saveChosenCityTo(city)
                hideKeyboard()
                binding.autoCompleteCityTo.clearFocus()
            }
        }
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
        private const val DEBOUNCE_INPUT_TEXT = 350L
    }

    @Parcelize
    class Screen : SupportAppScreen(), Parcelable {
        override fun getFragment(): Fragment {
            return SearchFragment()
        }
    }

}