package io.github.sds100.keymapper.util

import io.github.sds100.keymapper.ui.utils.SelectionState
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Created by sds100 on 11/02/2020.
 */

class MultiSelectProviderImpl : MultiSelectProvider {
    override val state = MutableStateFlow<SelectionState>(SelectionState.NotSelecting)

    override fun startSelecting(): Boolean {
        if (state.value !is SelectionState.Selecting) {
            state.value = SelectionState.Selecting(emptyList())

            return true
        }

        return false
    }

    override fun stopSelecting() {
        if (state.value is SelectionState.Selecting) {
            state.value = SelectionState.NotSelecting
        }
    }

    override fun toggleSelection(id: Long) {
        if (state.value !is SelectionState.Selecting) return
        val newIds = (state.value as SelectionState.Selecting).selectedIds.toMutableSet().apply {
            if (contains(id)) {
                remove(id)
            } else {
                add(id)
            }
        }

        state.value = SelectionState.Selecting(newIds.toList())
    }

    override fun isSelected(id: Long): Boolean {
        return state.value is SelectionState.Selecting
            && (state.value as SelectionState.Selecting).selectedIds.contains(id)
    }

    override fun select(vararg id: Long) {
        state.value.apply {
            if (this !is SelectionState.Selecting) return

            state.value = SelectionState.Selecting(selectedIds.plus(id.toSet()))
        }
    }

    override fun deselect(vararg id: Long) {
        state.value.apply {
            if (this !is SelectionState.Selecting) return

            state.value = SelectionState.Selecting(selectedIds.minus(id.toSet()))
        }
    }

    override fun reset() {
        state.value = SelectionState.NotSelecting
    }
}