package io.github.sds100.keymapper.domain.mappings.fingerprintmap

import io.github.sds100.keymapper.domain.models.Option
import io.github.sds100.keymapper.domain.utils.Defaultable

/**
 * Created by sds100 on 26/02/2021.
 */

/**
 * Int options are null if a custom value isn't being used.
 */

data class FingerprintMapOptions(
    val vibrate: Option<Boolean>,
    val vibrateDuration: Option<Defaultable<Int>>,
    val showToast: Option<Boolean>
)