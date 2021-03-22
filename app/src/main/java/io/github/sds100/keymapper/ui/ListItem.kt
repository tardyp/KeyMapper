package io.github.sds100.keymapper.ui

import io.github.sds100.keymapper.data.model.SliderModel

interface ListItem {
    val id: String
}

data class SliderListItem(
    override val id: String,
    val label: String,
    val sliderModel: SliderModel
) : ListItem

data class CheckBoxListItem(
    override val id: String, val isChecked: Boolean, val label: String
) : ListItem

data class TriggerFromOtherAppsListItem(
    override val id: String,
    val isEnabled: Boolean,
    val keymapUid: String,
    val label: String,
    val areLauncherShortcutsSupported: Boolean
) : ListItem