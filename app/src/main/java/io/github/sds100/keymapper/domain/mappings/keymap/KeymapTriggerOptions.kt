package io.github.sds100.keymapper.domain.mappings.keymap

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
data class KeymapTriggerOptions(
    val vibrate: Option<Boolean>,
    val longPressDoubleVibration: Option<Boolean>,
    val screenOffTrigger: Option<Boolean>,
    val longPressDelay: Option<Defaultable<Int>>,
    val doublePressDelay: Option<Defaultable<Int>>,
    val vibrateDuration: Option<Defaultable<Int>>,
    val sequenceTriggerTimeout: Option<Defaultable<Int>>,
    val triggerFromOtherApps: Option<Boolean>,
    val showToast: Option<Boolean>
)