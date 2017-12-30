package ph.codeia.arch.extensions

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer


/*
 * This file is a part of the kt-arch project.
 */


inline fun <T> LifecycleOwner.observe(
    data: LiveData<T>,
    from: Lifecycle.Event = Lifecycle.Event.ON_RESUME,
    until: Lifecycle.Event = Lifecycle.Event.ON_PAUSE,
    crossinline block: (T?) -> Unit
): Observer<T> {
    if (until <= from) error("Invalid bounds")
    val observer = Observer<T> { block(it) }
    untilDestroyed {
        from {
            data.observeForever(observer)
        }
        until {
            data.removeObserver(observer)
        }
    }
    return observer
}


inline fun <T> LifecycleOwner.untilDestroyed(
    data: LiveData<T>,
    crossinline block: (T?) -> Unit
): Observer<T> {
    val observer = Observer<T> { block(it) }
    data.observe(this, observer)
    return observer
}
