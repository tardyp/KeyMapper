package io.github.sds100.keymapper.ui.dialogs

import java.io.InputStream
import java.io.OutputStream

/**
 * Created by sds100 on 23/03/2021.
 */

//TODO have a subclass to show a toast as well. replace showToast in configconstraintviewmodel with this

sealed class GetUserResponse<RESPONSE : UserResponse> {

    data class SnackBar(
        val title: String, val long: Boolean = false, val actionText: String? = null
    ) : GetUserResponse<SnackBarActionResponse>()

    object SnackBarActionResponse : UserResponse

    data class Ok(val message: String,val title: String? = null) : GetUserResponse<OkResponse>()
    object OkResponse : UserResponse

    data class Dialog(
        val title: String? = null,
        val message: String,
        val positiveButtonText: String,
        val neutralButtonText: String? = null,
        val negativeButtonText: String? = null
    ) : GetUserResponse<DialogResponse>()

    data class Text(val hint: String, val allowEmpty: Boolean) : GetUserResponse<TextResponse>()
    data class TextResponse(val text: String) : UserResponse

    data class SingleChoice<ID>(val items: List<Pair<ID, String>>) :
        GetUserResponse<SingleChoiceResponse<ID>>()

    data class SingleChoiceResponse<ID>(val item: ID) : UserResponse

    data class MultiChoice<ID>(val items: List<Pair<ID, String>>) :
        GetUserResponse<MultiChoiceResponse<ID>>()

    data class MultiChoiceResponse<ID>(val items: List<ID>) : UserResponse
}

interface UserResponse

enum class DialogResponse : UserResponse {
    POSITIVE, NEUTRAL, NEGATIVE
}