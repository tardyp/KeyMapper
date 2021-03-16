package io.github.sds100.keymapper.ui.actions

import android.os.Build
import io.github.sds100.keymapper.domain.actions.ActionData
import io.github.sds100.keymapper.domain.actions.KeyEventAction
import io.github.sds100.keymapper.domain.actions.TapCoordinateAction

/**
 * Created by sds100 on 16/03/2021.
 */
object ActionUtils {
    fun canBeHeldDown(action: ActionData) = when (action) {
        is KeyEventAction -> !action.useShell
        is TapCoordinateAction -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        else -> false
    }
}