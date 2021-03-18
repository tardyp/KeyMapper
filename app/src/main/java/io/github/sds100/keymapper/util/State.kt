@file:Suppress("CanSealedSubClassBeObject")

package io.github.sds100.keymapper.util

import androidx.lifecycle.MutableLiveData

/**
 * Created by sds100 on 06/11/20.
 */

//TODO delete. have observable UiState object in all view models and use ListState for list models in that UiState object
sealed class ViewState
open class ViewPopulated : ViewState()
class ViewLoading : ViewState()
class ViewEmpty : ViewState()

//TODO delete
sealed class OldDataState<out T>

data class Data<out T>(val data: T) : OldDataState<T>()
class Loading : OldDataState<Nothing>()

/*
TODO is this really necessary. data shouldn't care about being empty,
 only the UI should because often UI needs to change if data is empty.
 also don't need to remember to use the getDataState method on lists
 */
class Empty : OldDataState<Nothing>()

fun <T, O> OldDataState<T>.mapData(block: (data: T) -> O): OldDataState<O> = when (this) {
    is Loading -> Loading()
    is Empty -> Empty()
    is Data -> Data(block.invoke(this.data))
}

suspend fun <T, O> OldDataState<T>.mapDataSuspend(block: suspend (data: T) -> O): OldDataState<O> =
    when (this) {
        is Loading -> Loading()
        is Empty -> Empty()
        is Data -> Data(block.invoke(this.data))
    }

inline fun <T> OldDataState<T>.ifIsData(block: (data: T) -> Unit) {
    if (this is Data) {
        block.invoke(this.data)
    }
}

fun <T> List<T>?.getDataState() =
    if (this.isNullOrEmpty()) {
        Empty()
    } else {
        Data(this)
    }

fun <K, T> Map<K, T>?.getDataState() =
    if (this.isNullOrEmpty()) {
        Empty()
    } else {
        Data(this)
    }

fun <T> T.data() = Data(this)

//TODO remove
fun MutableLiveData<ViewState>.populated() {
    value = ViewPopulated()
}

//TODO remove
fun MutableLiveData<ViewState>.loading() {
    value = ViewLoading()
}

//TODO remove
fun MutableLiveData<ViewState>.empty() {
    value = ViewEmpty()
}