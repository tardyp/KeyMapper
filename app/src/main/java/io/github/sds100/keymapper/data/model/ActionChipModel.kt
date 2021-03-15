package io.github.sds100.keymapper.data.model

import android.graphics.drawable.Drawable
import io.github.sds100.keymapper.util.ActionType
import io.github.sds100.keymapper.util.result.Error

/**
 * Created by sds100 on 26/02/2020.
 */

//TODO use sealed class for ChipModel because this is also used by constraints. subclass for error and normal
data class ActionChipModel(
    val type: ActionType,
    val description: String? = null,
    val error: Error? = null,
    val icon: Drawable? = null) {

    val hasError: Boolean
        get() = error != null
}