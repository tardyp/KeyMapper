package io.github.sds100.keymapper.util.delegate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.github.sds100.keymapper.util.OldDataState
import io.github.sds100.keymapper.util.ViewState

/**
 * Created by sds100 on 13/01/21.
 */

interface ModelState<T> {
    val model: LiveData<OldDataState<T>>
    val viewState: MutableLiveData<ViewState>
    fun rebuildModels()
}