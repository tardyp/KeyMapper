package io.github.sds100.keymapper.domain.mappings.keymap

import io.github.sds100.keymapper.domain.models.Option
import io.github.sds100.keymapper.domain.utils.Defaultable

data class KeymapActionOptions(
    val repeat: Option<Boolean>,
    val holdDown: Option<Boolean>,
    val stopRepeating: Option<StopRepeating>,
    val stopHoldDown: Option<Defaultable<StopHoldDown>>,
    val repeatRate: Option<Defaultable<Int>>,
    val repeatDelay: Option<Defaultable<Int>>,
    val holdDownDuration: Option<Defaultable<Int>>,
    val delayBeforeNextAction: Option<Defaultable<Int>>,
    val multiplier: Option<Defaultable<Int>>
)