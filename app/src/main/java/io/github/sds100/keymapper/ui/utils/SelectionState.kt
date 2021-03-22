package io.github.sds100.keymapper.ui.utils

/**
 * Created by sds100 on 22/03/2021.
 */
sealed class SelectionState {
    data class Selecting(val selectedIds: List<Long>) : SelectionState()
    object NotSelecting : SelectionState()
}
