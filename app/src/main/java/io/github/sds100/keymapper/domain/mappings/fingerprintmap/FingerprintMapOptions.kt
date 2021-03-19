package io.github.sds100.keymapper.domain.mappings.fingerprintmap

import io.github.sds100.keymapper.domain.models.Defaultable
import io.github.sds100.keymapper.domain.models.Option
import kotlinx.serialization.Serializable

/**
 * Created by sds100 on 26/02/2021.
 */

/**
 * Int options are null if a custom value isn't being used.
 */
@Serializable
data class FingerprintMapOptions(
    val vibrate: Option<Boolean>,
    val vibrateDuration: Option<Defaultable<Int>>,
    val showToast: Option<Boolean>
)