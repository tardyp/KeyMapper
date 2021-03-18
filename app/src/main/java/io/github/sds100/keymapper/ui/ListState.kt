package io.github.sds100.keymapper.ui

/**
 * Created by sds100 on 17/03/2021.
 */
sealed class ListState<T> {
    data class Loaded<T>(val data: List<T>) : ListState<T>() {
        init {
            require(data.isNotEmpty()) { "List can't be empty!" }
        }
    }

    class Loading<T> : ListState<T>()
    class Empty<T> : ListState<T>()
}

fun <T> List<T>.createListState(): ListState<T> {
    return when {
        this.isEmpty() -> ListState.Empty()
        else -> ListState.Loaded(this)
    }
}