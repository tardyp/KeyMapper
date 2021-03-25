package io.github.sds100.keymapper.ui.mappings.keymap

import io.github.sds100.keymapper.ui.ChipUi

/**
 * Created by sds100 on 28/03/2020.
 */

data class KeymapListItem(
    val keymapUiState: KeymapUiState,
    val selectionUiState: SelectionUiState
) {
    data class KeymapUiState(
        val uid: String,
        val chipList: List<ChipUi>,
        val triggerDescription: String,
        val optionsDescription: String,
        val extraInfo: String
    ) {
        val hasTrigger: Boolean
            get() = triggerDescription.isNotBlank()

        val hasOptions: Boolean
            get() = optionsDescription.isNotBlank()

    }

    data class SelectionUiState(val isSelected: Boolean, val isSelectable: Boolean)
}