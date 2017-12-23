package ph.codeia.arch.extensions

import android.arch.lifecycle.GenericLifecycleObserver
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner


/*
 * This file is a part of the kt-arch project.
 */


inline fun LifecycleOwner.onStateChange(crossinline block: (Lifecycle.Event) -> Unit) {
    lifecycle.addObserver(object : GenericLifecycleObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            block(event)
            if (event == Lifecycle.Event.ON_DESTROY) {
                source.lifecycle.removeObserver(this)
            }
        }
    })
}


inline fun LifecycleOwner.onStateChangeUnmanaged(
        crossinline block: (Lifecycle.Event) -> Unit
): LifecycleObserver {
    val observer = GenericLifecycleObserver { _, event ->
        block(event)
    }
    lifecycle.addObserver(observer)
    return observer
}
