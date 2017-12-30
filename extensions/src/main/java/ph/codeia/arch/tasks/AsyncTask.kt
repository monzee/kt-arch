package ph.codeia.arch.tasks

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer


/*
 * This file is a part of the kt-arch project.
 */

class AsyncTask<I : Any, O>(private val fetch: Continuation<O>.(I) -> Unit) {
    private val state = MutableLiveData<State>()
    private val scope = object : Continuation<O> {
        override fun yield(value: O?) {
            data = value
            state.postValue(State.SUCCESSFUL)
        }

        override fun yield(error: Exception) {
            this@AsyncTask.error = error
            state.postValue(State.FAILURE)
        }
    }

    private lateinit var key: I
    private var data: O? = null
    private var oldData: O? = null
    private lateinit var error: Exception

    fun execute(key: I) {
        state.value?.let {
            if (it == State.FETCHING) return
        }
        this.key = key
        state.postValue(State.START)
    }

    fun observe(owner: LifecycleOwner, listener: Listener<I, O>) {
        state.observe(owner, Observer {
            when (it) {
                null -> {}
                State.START -> {
                    oldData = data
                    state.value = State.FETCHING
                    scope.fetch(key)
                }
                State.FETCHING -> listener.onExecute(key, oldData)
                State.SUCCESSFUL -> {
                    listener.onSuccess(key, data)
                    state.value = null
                }
                State.FAILURE -> {
                    listener.onFailure(key, error)
                    state.value = null
                }
            }
        })
    }

    inline fun observe(
        owner: LifecycleOwner,
        crossinline block: OnTask<O>.(I) -> Unit
    ) {
        observe(owner, Listener(block))
    }

}


typealias AsyncAction<T> = AsyncTask<Unit, T>


interface Continuation<in O> {
    fun yield(value: O?)
    fun yield(error: Exception)
}


fun AsyncAction<*>.execute() = execute(Unit)


fun <I : Any, O> LifecycleOwner.observe(
    task: AsyncTask<I, O>,
    listener: Listener<I, O>
) {
    task.observe(this, listener)
}


inline fun <I : Any, O> LifecycleOwner.observe(
    task: AsyncTask<I, O>,
    crossinline listener: OnTask<O>.(I) -> Unit
) {
    task.observe(this, listener)
}
