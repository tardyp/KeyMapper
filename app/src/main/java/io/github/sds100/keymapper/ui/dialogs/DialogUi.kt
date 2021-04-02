package io.github.sds100.keymapper.ui.dialogs

/**
 * Created by sds100 on 23/03/2021.
 */
sealed class DialogUi<RESPONSE : DialogResponse> {

    data class SnackBar(val title: String, val long: Boolean = false, val actionText: String?
    ) : DialogUi<SnackBarActionResponse>()

    object SnackBarActionResponse : DialogResponse

    data class OkMessage(val message: String) : DialogUi<OkResponse>()
    object OkResponse : DialogResponse

    data class Text(val hint: String, val allowEmpty: Boolean) : DialogUi<TextResponse>()
    data class TextResponse(val text: String) : DialogResponse

    data class SingleChoice<ID>(val items: List<Pair<ID, String>>) :
        DialogUi<SingleChoiceResponse<ID>>()

    data class SingleChoiceResponse<ID>(val item: ID) : DialogResponse

    data class MultiChoice<ID>(val items: List<Pair<ID, String>>) :
        DialogUi<MultiChoiceResponse<ID>>()

    data class MultiChoiceResponse<ID>(val items: List<ID>) : DialogResponse
}

interface DialogResponse