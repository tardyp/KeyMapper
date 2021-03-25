package io.github.sds100.keymapper.util

import io.github.sds100.keymapper.ui.utils.SelectionState
import kotlinx.coroutines.flow.StateFlow

/**
 * Created by sds100 on 11/02/2020.
 */

interface MultiSelectProvider {
    val state: StateFlow<SelectionState>

    /**
     * @return true if it wasn't already selecting
     */
    fun startSelecting(): Boolean

    fun stopSelecting()

    fun toggleSelection(id: String)

    fun select(vararg id: String)
    fun deselect(vararg id: String)

    fun isSelected(id: String): Boolean

    fun reset()
}