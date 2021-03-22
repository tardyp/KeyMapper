package io.github.sds100.keymapper.domain.mappings.fingerprintmap

import io.github.sds100.keymapper.domain.models.Option
import io.github.sds100.keymapper.domain.utils.Defaultable

data class FingerprintMapActionOptions(
    val delayBeforeNextAction: Option<Defaultable<Int>>,
    val multiplier: Option<Defaultable<Int>>,
    val repeatUntilSwipedAgain: Option<Boolean>,
    val repeatRate: Option<Defaultable<Int>>,
    val holdDownUntilSwipedAgain: Option<Boolean>,
    val holdDownDuration: Option<Defaultable<Int>>
)