package io.github.sds100.keymapper.domain.models

import io.github.sds100.keymapper.domain.mappings.keymap.StopHoldDown
import io.github.sds100.keymapper.domain.mappings.keymap.StopRepeating

data class KeymapActionOptions(
    val repeat: Option<Boolean>,
    val holdDown: Option<Boolean>,
    val stopRepeating: Option<StopRepeating>,
    val stopHoldDown: Option<StopHoldDown>,
    val repeatRate: Option<Defaultable<Int>>,
    val repeatDelay: Option<Defaultable<Int>>,
    val holdDownDuration: Option<Defaultable<Int>>,
    val delayBeforeNextAction: Option<Defaultable<Int>>,
    val multiplier: Option<Defaultable<Int>>
)