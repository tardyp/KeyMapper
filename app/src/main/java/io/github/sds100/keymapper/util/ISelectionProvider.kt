package io.github.sds100.keymapper.util

import kotlinx.coroutines.flow.StateFlow

/**
 * Created by sds100 on 11/02/2020.
 */

interface ISelectionProvider {
    val isSelectable: StateFlow<Boolean>
    val selectedIds: StateFlow<Set<Long>>

    /**
     * @return true if it wasn't already selecting
     */
    fun startSelecting(): Boolean

    fun stopSelecting()

    fun toggleSelection(id: Long)

    fun select(vararg id: Long)
    fun deselect(vararg id: Long)

    fun isSelected(id: Long): Boolean

    fun reset()
}