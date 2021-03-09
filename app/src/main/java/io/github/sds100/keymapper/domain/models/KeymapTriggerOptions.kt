package io.github.sds100.keymapper.domain.models

import io.github.sds100.keymapper.data.model.TriggerEntity
import io.github.sds100.keymapper.domain.trigger.TriggerMode
import io.github.sds100.keymapper.domain.utils.ClickType
import io.github.sds100.keymapper.util.delegate.GetEventDelegate

/**
 * Created by sds100 on 26/02/2021.
 */

/**
 * Int options are null if a custom value isn't being used.
 */
data class KeymapTriggerOptions (
    val vibrate: Option<Boolean>,
    val longPressDoubleVibration: Option<Boolean>,
    val screenOffTrigger: Option<Boolean>,
    val longPressDelay: Option<Defaultable<Int>>,
    val doublePressDelay: Option<Defaultable<Int>>,
    val vibrateDuration: Option<Defaultable<Int>>,
    val sequenceTriggerTimeout: Option<Defaultable<Int>>,
    val triggerFromOtherApps: Option<Boolean>,
    val showToast: Option<Boolean>
) {
    constructor(
        keys: List<TriggerKey>,
        mode: TriggerMode,
        oldOptions: KeymapTriggerOptions
    ) : this(
        keys,
        mode,
        oldOptions.vibrate.value,
        oldOptions.longPressDoubleVibration.value,
        oldOptions.screenOffTrigger.value,
        oldOptions.longPressDelay.value,
        oldOptions.doublePressDelay.value,
        oldOptions.vibrateDuration.value,
        oldOptions.sequenceTriggerTimeout.value,
        oldOptions.triggerFromOtherApps.value,
        oldOptions.showToast.value
    )

    constructor(
        keys: List<TriggerKey>,
        mode: TriggerMode,
        vibrate: Boolean = false,
        longPressDoubleVibration: Boolean = false,
        screenOffTrigger: Boolean = false,
        longPressDelay: Defaultable<Int> = Defaultable.Default(),
        doublePressDelay: Defaultable<Int> = Defaultable.Default(),
        vibrateDuration: Defaultable<Int> = Defaultable.Default(),
        sequenceTriggerTimeout: Defaultable<Int> = Defaultable.Default(),
        triggerFromOtherApps: Boolean = false,
        showToast: Boolean = false
    ) : this(
        vibrate = Option(
            value = vibrate,
            isAllowed = (keys.size == 1 || (mode == TriggerMode.PARALLEL))
                && keys.getOrNull(0)?.clickType == ClickType.LONG_PRESS
        ),

        vibrateDuration = Option(
            value = vibrateDuration,
            isAllowed = vibrate || longPressDoubleVibration
        ),

        longPressDelay = Option(
            value = longPressDelay,
            isAllowed = keys.any { key -> key.clickType == ClickType.LONG_PRESS }
        ),

        doublePressDelay = Option(
            value = doublePressDelay,
            isAllowed = keys.any { key -> key.clickType == ClickType.DOUBLE_PRESS }
        ),

        longPressDoubleVibration = Option(
            value = longPressDoubleVibration,
            isAllowed = (keys.size == 1 || (mode == TriggerMode.PARALLEL))
                && keys.getOrNull(0)?.clickType == ClickType.LONG_PRESS
        ),

        screenOffTrigger = Option(
            value = screenOffTrigger,
            isAllowed = keys.isNotEmpty() && keys.all {
                GetEventDelegate.canDetectKeyWhenScreenOff(it.keyCode)
            }
        ),

        sequenceTriggerTimeout = Option(
            value = sequenceTriggerTimeout,
            isAllowed = !keys.isNullOrEmpty()
                && keys.size > 1
                && mode == TriggerMode.SEQUENCE
        ),

        triggerFromOtherApps = Option(
            value = triggerFromOtherApps,
            isAllowed = true
        ),

        showToast = Option(
            value = showToast,
            isAllowed = true
        )
    )
}