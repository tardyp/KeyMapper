package io.github.sds100.keymapper.ui

import kotlinx.coroutines.flow.StateFlow

/**
 * Created by sds100 on 17/03/2021.
 */
interface UiStateProducer<T> {
    val state: StateFlow<T>
    fun rebuildUiState()
}