package io.github.sds100.keymapper.domain.mappings.keymap

import io.github.sds100.keymapper.data.model.TriggerEntity
import io.github.sds100.keymapper.domain.adapter.ExternalDeviceAdapter
import io.github.sds100.keymapper.domain.models.Defaultable
import io.github.sds100.keymapper.domain.models.Option
import io.github.sds100.keymapper.domain.trigger.TriggerMode
import io.github.sds100.keymapper.domain.utils.ClickType
import io.github.sds100.keymapper.util.delegate.GetEventDelegate
import kotlinx.serialization.Serializable

/**
 * Created by sds100 on 03/03/2021.
 */

@Serializable
data class KeymapTrigger(
    val keys: List<TriggerKey> = emptyList(),
    val mode: TriggerMode = TriggerMode.UNDEFINED,

    private val vibrate: Boolean = false,
    private val longPressDoubleVibration: Boolean = false,
    private val screenOffTrigger: Boolean = false,
    private val longPressDelay: Defaultable<Int> = Defaultable.Default(),
    private val doublePressDelay: Defaultable<Int> = Defaultable.Default(),
    private val vibrateDuration: Defaultable<Int> = Defaultable.Default(),
    private val sequenceTriggerTimeout: Defaultable<Int> = Defaultable.Default(),
    private val triggerFromOtherApps: Boolean = false,
    private val showToast: Boolean = false
) {
    val options = KeymapTriggerOptions(
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

object KeymapTriggerEntityMapper {
    fun fromEntity(
        entity: TriggerEntity,
        deviceAdapter: ExternalDeviceAdapter
    ): KeymapTrigger {
        return KeymapTrigger(
            keys = entity.keys.map { KeymapTriggerKeyEntityMapper.fromEntity(it, deviceAdapter) }
        )
    }
}