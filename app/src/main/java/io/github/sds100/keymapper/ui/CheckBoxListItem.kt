package io.github.sds100.keymapper.ui

data class CheckBoxListItem(
    override val id: String, val isChecked: Boolean, val label: String
) : ListItem