package ph.codeia.arch.extensions

import android.arch.lifecycle.GenericLifecycleObserver
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner


/*
 * This file is a part of the kt-arch project.
 */


class LifecycleScope(val current: Lifecycle.Event) {
    inline operator fun Lifecycle.Event.invoke(block: () -> Unit) {
        if (this === current) block()
    }
}


inline fun LifecycleOwner.untilDestroyed(
    crossinline block: LifecycleScope.(LifecycleOwner) -> Unit
) {
    lifecycle.addObserver(object : GenericLifecycleObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            LifecycleScope(event).block(source)
            if (event === Lifecycle.Event.ON_DESTROY) {
                source.lifecycle.removeObserver(this)
            }
        }
    })
}


inline fun LifecycleOwner.onStateChange(
    crossinline block: LifecycleScope.(LifecycleOwner) -> Unit
): LifecycleObserver {
    val observer = GenericLifecycleObserver { source, event ->
        LifecycleScope(event).block(source)
    }
    lifecycle.addObserver(observer)
    return observer
}
