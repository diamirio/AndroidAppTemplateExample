package com.tailoredapps.androidapptemplate.base.ui

/**
 * Sealed class that represents an asynchronous load of a resource.
 */
sealed class Async<out T>(val complete: Boolean, val shouldLoad: Boolean) {
    open operator fun invoke(): T? = null

    object Uninitialized : Async<Nothing>(false, true)
    object Loading : Async<Nothing>(false, false)
    data class Error(val error: Throwable) : Async<Nothing>(true, true)
    data class Success<out T>(val element: T) : Async<T>(true, false) {
        override operator fun invoke(): T = element
    }

    val initialized: Boolean get() = this !is Uninitialized
    val loading: Boolean get() = this is Loading
}

fun <T, O> Async<T>.map(mapper: (T) -> O): Async<O> {
    return when (this) {
        is Async.Success -> Async.Success(mapper.invoke(this.element))
        is Async.Error -> Async.Error(error)
        is Async.Loading -> Async.Loading
        else -> Async.Uninitialized
    }
}