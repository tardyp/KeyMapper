package io.github.sds100.keymapper.domain.mappings.fingerprintmap

import io.github.sds100.keymapper.domain.models.Defaultable
import io.github.sds100.keymapper.domain.models.Option

@Serializable
data class FingerprintMapActionOptions(
    val delayBeforeNextAction: Option<Defaultable<Int>>,
    val multiplier: Option<Defaultable<Int>>
)