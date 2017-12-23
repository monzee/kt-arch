package ph.codeia.arch.extensions

import android.arch.lifecycle.*


/*
 * This file is a part of the kt-arch project.
 */


inline fun <T> LifecycleOwner.observe(
        data: LiveData<T>,
        start: Lifecycle.Event = Lifecycle.Event.ON_RESUME,
        end: Lifecycle.Event = Lifecycle.Event.ON_PAUSE,
        crossinline block: (T?) -> Unit
): Observer<T> {
    if (end <= start) error("Invalid bounds")
    val observer = Observer<T> { block(it) }
    onStateChange {
        when (it) {
            start -> data.observeForever(observer)
            end -> data.removeObserver(observer)
            else -> {}
        }
    }
    return observer
}


inline fun <T> LifecycleOwner.observeUnmanaged(
        data: LiveData<T>,
        crossinline block: (T?) -> Unit
): Observer<T> {
    val observer = Observer<T> { block(it) }
    data.observe(this, observer)
    return observer
}
