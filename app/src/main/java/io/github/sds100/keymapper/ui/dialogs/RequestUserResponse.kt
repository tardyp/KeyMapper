package io.github.sds100.keymapper.ui.dialogs

/**
 * Created by sds100 on 23/03/2021.
 */

//TODO have a subclass to show a toast as well. replace showToast in configconstraintviewmodel with this

sealed class RequestUserResponse<RESPONSE : UserResponse> {

    data class SnackBar(val title: String, val long: Boolean = false, val actionText: String?
    ) : RequestUserResponse<SnackBarActionResponse>()

    object SnackBarActionResponse : UserResponse

    data class Ok(val message: String) : RequestUserResponse<OkResponse>()
    object OkResponse : UserResponse

    data class Text(val hint: String, val allowEmpty: Boolean) : RequestUserResponse<TextResponse>()
    data class TextResponse(val text: String) : UserResponse

    data class SingleChoice<ID>(val items: List<Pair<ID, String>>) :
        RequestUserResponse<SingleChoiceResponse<ID>>()

    data class SingleChoiceResponse<ID>(val item: ID) : UserResponse

    data class MultiChoice<ID>(val items: List<Pair<ID, String>>) :
        RequestUserResponse<MultiChoiceResponse<ID>>()

    data class MultiChoiceResponse<ID>(val items: List<ID>) : UserResponse
}

interface UserResponse