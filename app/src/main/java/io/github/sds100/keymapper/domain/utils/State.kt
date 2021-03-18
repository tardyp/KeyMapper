package io.github.sds100.keymapper.domain.utils

/**
 * Created by sds100 on 17/03/2021.
 */
sealed class State<T> {
    data class Data<T>(val data: T) : State<T>()
    class Loading<T> : State<T>()
}

fun <T, O> State<T>.mapData(block: (data: T) -> O): State<O> = when (this) {
    is State.Loading -> State.Loading()
    is State.Data -> State.Data(block.invoke(this.data))
}

inline fun <T> State<T>.ifIsData(block: (data: T) -> Unit) {
    if (this is State.Data) {
        block.invoke(this.data)
    }
}