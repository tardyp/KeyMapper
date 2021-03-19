package io.github.sds100.keymapper.ui.models

import androidx.annotation.StringRes
import io.github.sds100.keymapper.data.model.SliderModel

interface ListItem {
    val id: String
}

class SliderListItem(
    override val id: String,
    @StringRes val label: Int,
    val sliderModel: SliderModel
) : ListItem

class CheckBoxListItem(
    override val id: String, val isChecked: Boolean, @StringRes val label: Int
) : ListItem

class TriggerFromOtherAppsListItem(
    override val id: String,
    val isEnabled: Boolean,
    val keymapUid: String,
    @StringRes val label: Int
) : ListItem