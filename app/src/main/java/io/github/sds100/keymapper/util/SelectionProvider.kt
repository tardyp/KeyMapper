package io.github.sds100.keymapper.util

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Created by sds100 on 11/02/2020.
 */

class SelectionProvider : ISelectionProvider {
    override val isSelectable = MutableStateFlow(false)

    override val selectedIds = MutableStateFlow(emptySet<Long>())

    override fun startSelecting(): Boolean {
        if (!isSelectable.value) {
            deselectAll()

            isSelectable.value = true

            return true
        }

        return false
    }

    override fun stopSelecting() {
        if (isSelectable.value) {
            isSelectable.value = false
        }

        deselectAll()
    }

    override fun toggleSelection(id: Long) {
        selectedIds.value = selectedIds.value.toMutableSet().apply {
            if (contains(id)) {
                remove(id)
            } else {
                add(id)
            }
        }
    }

    override fun isSelected(id: Long): Boolean {
        return selectedIds.value.contains(id)
    }

    override fun select(vararg id: Long) {
        selectedIds.value = selectedIds.value.plus(id.toSet())
    }

    override fun deselect(vararg id: Long) {
        selectedIds.value = selectedIds.value.minus(id.toSet())
    }

    private fun deselectAll() {
        selectedIds.value = emptySet()
    }

    override fun reset() {
        selectedIds.value = emptySet()
    }
}