package io.github.sds100.keymapper.data.model

import io.github.sds100.keymapper.domain.utils.defaultable.Defaultable

/**
 * Created by sds100 on 04/06/20.
 */
data class SliderModel(
    val value: Defaultable<Int>,
    val isDefaultStepEnabled: Boolean,
    val min: Int,
    val max: Int,
    val stepSize: Int
)