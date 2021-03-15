package io.github.sds100.keymapper.domain.mappings.keymap

import io.github.sds100.keymapper.domain.models.Defaultable
import io.github.sds100.keymapper.domain.models.Option

@Serializable
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