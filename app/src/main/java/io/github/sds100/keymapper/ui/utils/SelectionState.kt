package io.github.sds100.keymapper.ui.utils

/**
 * Created by sds100 on 22/03/2021.
 */
sealed class SelectionState {
    data class Selecting(val selectedIds: Set<String>) : SelectionState()
    object NotSelecting : SelectionState()
}

fun SelectionState.isSelected(id: String): Boolean {
    if (this !is SelectionState.Selecting) return false

    return this.selectedIds.contains(id)
}