package ph.codeia.arch.extensions

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity


/*
 * This file is a part of the kt-arch project.
 */


inline fun <reified T : ViewModel> FragmentActivity.viewModel(): T =
        ViewModelProviders.of(this)[T::class.java]


inline fun <reified T : ViewModel> Fragment.viewModel(): T =
        ViewModelProviders.of(this)[T::class.java]


inline fun <reified T : ViewModel> FragmentActivity.viewModel(crossinline factory: () -> T): T {
    return ViewModelProviders.of(this, make(factory))[T::class.java]
}


inline fun <reified T : ViewModel> Fragment.viewModel(crossinline factory: () -> T): T {
    return ViewModelProviders.of(this, make(factory))[T::class.java]
}


inline fun make(crossinline block: () -> Any) = object : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.cast(block())
    }
}