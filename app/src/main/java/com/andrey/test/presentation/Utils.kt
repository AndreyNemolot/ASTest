package com.andrey.test.presentation

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.andrey.test.di.Scope
import com.andrey.test.di.ViewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import toothpick.Toothpick

fun <T> Flow<T>.observeOn(viewLifecycleOwner: LifecycleOwner, block: (T) -> Unit) {
    viewLifecycleOwner.lifecycleScope.launchWhenCreated {
        collect {
            block(it)
        }
    }
}

fun <T> Flow<T>.observeOn(lifecycleScope: LifecycleCoroutineScope, block: (T) -> Unit) {
    lifecycleScope.launchWhenCreated {
        collect {
            block(it)
        }
    }
}

inline fun <reified T : ViewModel> Fragment.obtainViewModel(): T =
    ViewModelLazy(
        T::class,
        { viewModelStore },
        { Toothpick.openScope(Scope.APP).getInstance(ViewModelFactory::class.java) }
    ).value

inline fun <reified T : ViewModel> AppCompatActivity.obtainViewModel(): T =
    ViewModelLazy(
        T::class,
        { viewModelStore },
        { Toothpick.openScope(Scope.APP).getInstance(ViewModelFactory::class.java) }
    ).value

@ExperimentalCoroutinesApi
fun EditText.textChanges(): Flow<CharSequence?> {
    return callbackFlow<CharSequence?> {
        val listener = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                offer(s)
            }
        }
        addTextChangedListener(listener)
        awaitClose { removeTextChangedListener(listener) }
    }.onStart { emit(text) }
}

inline var TextView.textOrGone: CharSequence?
    get() = this.text
    set(value) {
        if (value == null) {
            visibility = View.GONE
        } else {
            visibility = View.VISIBLE
            text = value
        }
    }

fun Fragment.showMessage(errorText: String) {
    Toast.makeText(requireContext(), errorText, Toast.LENGTH_SHORT).show()
}
