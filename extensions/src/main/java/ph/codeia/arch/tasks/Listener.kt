package ph.codeia.arch.tasks


/*
 * This file is a part of the kt-arch project.
 */

internal enum class State { START, FETCHING, SUCCESSFUL, FAILURE }


interface Listener<in I : Any, in O> {
    fun onExecute(key: I, oldData: O?)
    fun onSuccess(key: I, data: O?)
    fun onFailure(key: I, error: Exception)

    companion object {
        inline operator fun <I : Any, O> invoke(
            crossinline block: OnTask<O>.(I) -> Unit
        ) = object : Listener<I, O> {
            override fun onExecute(key: I, oldData: O?) {
                block(OnTask.Execute(oldData), key)
            }

            override fun onSuccess(key: I, data: O?) {
                block(OnTask.Success(data), key)
            }

            override fun onFailure(key: I, error: Exception) {
                block(OnTask.Failure(error), key)
            }
        }
    }
}


sealed class OnTask<out O> {
    open fun onExecute(block: (O?) -> Unit) {}
    open fun onSuccess(block: (O?) -> Unit) {}
    open fun onFailure(block: (Exception) -> Unit) {}

    class Execute<out O>(private val oldData: O?) : OnTask<O>() {
        override fun onExecute(block: (O?) -> Unit) {
            block(oldData)
        }
    }

    class Success<out O>(private val data: O?) : OnTask<O>() {
        override fun onSuccess(block: (O?) -> Unit) {
            block(data)
        }
    }

    class Failure(private val error: Exception) : OnTask<Nothing>() {
        override fun onFailure(block: (Exception) -> Unit) {
            block(error)
        }
    }
}

