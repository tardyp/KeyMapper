package io.github.sds100.keymapper.ui.fragment.keymap

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import io.github.sds100.keymapper.domain.utils.ClickType

/**
 * Created by sds100 on 27/03/2020.
 */
data class TriggerKeyListItemModel(
    val id: String,
    val keyCode: Int,
    val name: String,

    /**
     * null if should be hidden
     */
    val clickTypeString: String? = null,

    val extraInfo: String?,

    /**
     * null if should be hidden
     */
    @DrawableRes
    val linkDrawable: Int? = null,

    val isDragDropEnabled: Boolean
)