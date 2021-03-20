package io.github.sds100.keymapper.ui.constraints

import android.graphics.drawable.Drawable
import io.github.sds100.keymapper.util.TintType

/**
 * Created by sds100 on 17/03/2020.
 */

data class ConstraintListItem(
    val id: String,
    val tintType: TintType,
    val title: String,
    val icon: Drawable? = null,
    val errorMessage: String? = null,
)