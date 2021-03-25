package io.github.sds100.keymapper.ui

import io.github.sds100.keymapper.data.model.SliderModel

data class SliderListItem(
    override val id: String,
    val label: String,
    val sliderModel: SliderModel
) : ListItem