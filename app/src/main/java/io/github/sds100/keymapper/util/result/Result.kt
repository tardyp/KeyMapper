package io.github.sds100.keymapper.util.result

/**
 * Created by sds100 on 26/02/2020.
 */

/**
 * Inspired from @antonyharfield great example!
 */

sealed class Result<out T>

data class Success<T>(val value: T) : Result<T>()

abstract class Error : Result<Nothing>()
abstract class RecoverableError : Error()

fun <T1, T2> combineOnSuccess(
    result1: Result<T1>,
    result2: Result<T2>,
    onSuccess: (value1: T1, value2: T2) -> Unit
) {
    if (result1 is Success && result2 is Success) {
        onSuccess.invoke(result1.value, result2.value)
    }
}

inline fun <T> Result<T>.onSuccess(f: (T) -> Unit): Result<T> {
    if (this is Success) {
        f(this.value)
    }

    return this
}

inline fun <T, U> Result<T>.onFailure(f: (error: Error) -> U): Result<T> {
    if (this is Error) {
        f(this)
    }

    return this
}

infix fun <T, U> Result<T>.then(f: (T) -> Result<U>) =
    when (this) {
        is Success -> f(this.value)
        is Error -> this
    }

suspend infix fun <T, U> Result<T>.suspendThen(f: suspend (T) -> Result<U>) =
    when (this) {
        is Success -> f(this.value)
        is Error -> this
    }

infix fun <T> Result<T>.otherwise(f: (error: Error) -> Result<T>) =
    when (this) {
        is Success -> this
        is Error -> f(this)
    }

fun <T> Result<T>.errorOrNull(): Error? {
    when (this) {
        is Error -> return this
    }

    return null
}

fun <T> Result<T>.valueOrNull(): T? {
    when (this) {
        is Success -> return this.value
    }

    return null
}

val <T> Result<T>.isError: Boolean
    get() = this is Error

val <T> Result<T>.isSuccess: Boolean
    get() = this is Success

fun <T, U> Result<T>.handle(onSuccess: (value: T) -> U, onError: (error: Error) -> U): U {
    return when (this) {
        is Success -> onSuccess(value)
        is Error -> onError(this)
    }
}

suspend fun <T, U> Result<T>.handleAsync(
    onSuccess: suspend (value: T) -> U,
    onFailure: suspend (error: Error) -> U
): U {
    return when (this) {
        is Success -> onSuccess(value)
        is Error -> onFailure(this)
    }
}


fun <T> T.success() = Success(this)