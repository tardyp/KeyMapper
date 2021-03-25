package io.github.sds100.keymapper.ui

import io.github.sds100.keymapper.ui.dialogs.DialogResponse
import io.github.sds100.keymapper.ui.dialogs.DialogResponseEvent
import io.github.sds100.keymapper.ui.dialogs.DialogUi
import io.github.sds100.keymapper.ui.dialogs.ShowDialogEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Created by sds100 on 23/03/2021.
 */

class DialogViewModelImpl : DialogViewModel {

    private val _dialogResponse = MutableSharedFlow<DialogResponseEvent>()
    override val dialogResponse = _dialogResponse.asSharedFlow()

    private val _showDialog = MutableSharedFlow<ShowDialogEvent>()
    override val showDialog = _showDialog.asSharedFlow()

    override suspend fun showDialog(event: ShowDialogEvent) {
        _showDialog.emit(event)
    }

    override fun onDialogResponse(event: DialogResponseEvent) {
        runBlocking { _dialogResponse.emit(event) }
    }
}

interface DialogViewModel {
    val showDialog: SharedFlow<ShowDialogEvent>
    val dialogResponse: SharedFlow<DialogResponseEvent>

    suspend fun showDialog(event: ShowDialogEvent)
    fun onDialogResponse(event: DialogResponseEvent)
}

fun DialogViewModel.onDialogResponse(key: String, response: DialogResponse) {
    onDialogResponse(DialogResponseEvent(key, response))
}

suspend inline fun <reified R : DialogResponse> DialogViewModel.showDialog(
    key: String,
    ui: DialogUi<R>,
    onResponse: (R) -> Unit
) {
    onResponse(showDialog(key, ui))
}

suspend inline fun <reified R : DialogResponse> DialogViewModel.showDialog(
    key: String,
    ui: DialogUi<R>
): R {
    showDialog(ShowDialogEvent(key, ui))

    return dialogResponse.first { it.response is R && it.key == key }.response as R
}