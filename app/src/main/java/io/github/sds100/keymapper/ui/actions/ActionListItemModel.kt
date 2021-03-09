package io.github.sds100.keymapper.ui.actions

import android.graphics.drawable.Drawable
import io.github.sds100.keymapper.util.TintType
import io.github.sds100.keymapper.util.result.Error

/**
 * Created by sds100 on 26/03/2020.
 */

data class ActionListItemModel(
    val id: String,
    val tintType: TintType,
    val title: String? = null,
    val icon: Drawable? = null,
    val extraInfo: String? = null,
    val error: Error? = null,
    val briefErrorMessage: String? = null
) {
    val hasError: Boolean
        get() = error != null
}