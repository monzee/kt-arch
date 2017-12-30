package ph.codeia.arch.tasks

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future


/*
 * This file is a part of the kt-arch project.
 */

private val DEFAULT_WORKER = Executors.newSingleThreadExecutor()

class Task<I : Any, out O>(private val fetch: (I) -> O?) {
    private val state = MutableLiveData<State>()

    private lateinit var key: I
    private var data: O? = null
    private var oldData: O? = null
    private lateinit var error: Exception
    private var pending: Future<*>? = null
    private var worker: ExecutorService = DEFAULT_WORKER

    fun execute(key: I) {
        execute(key, DEFAULT_WORKER)
    }

    fun execute(key: I, worker: ExecutorService) {
        state.value?.let {
            if (it == State.FETCHING) return
        }
        this.key = key
        this.worker = worker
        pending?.cancel(true)
        state.postValue(State.START)
    }

    fun observe(owner: LifecycleOwner, listener: Listener<I, O>) {
        state.observe(owner, Observer {
            when (it) {
                null -> {}
                State.START -> {
                    oldData = data
                    pending = worker.submit {
                        state.postValue(State.FETCHING)
                        try {
                            data = fetch(key)
                            state.postValue(State.SUCCESSFUL)
                        }
                        catch (_: InterruptedException) {
                        }
                        catch (e: Exception) {
                            error = e
                            state.postValue(State.FAILURE)
                        }
                    }
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

typealias Action<T> = Task<Unit, T>


fun Action<*>.execute() = execute(Unit)


fun Action<*>.execute(worker: ExecutorService) = execute(Unit, worker)


fun <I : Any, O> LifecycleOwner.observe(
    task: Task<I, O>,
    listener: Listener<I, O>
) {
    task.observe(this, listener)
}


inline fun <I : Any, O> LifecycleOwner.observe(
    task: Task<I, O>,
    crossinline listener: OnTask<O>.(I) -> Unit
) {
    task.observe(this, listener)
}

