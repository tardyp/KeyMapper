package io.github.sds100.keymapper.domain.mappings.keymap.trigger

import io.github.sds100.keymapper.data.model.Extra
import io.github.sds100.keymapper.data.model.TriggerEntity
import io.github.sds100.keymapper.domain.adapter.ExternalDeviceAdapter
import io.github.sds100.keymapper.domain.models.Defaultable
import io.github.sds100.keymapper.domain.models.Option
import io.github.sds100.keymapper.domain.models.ifIsAllowed
import io.github.sds100.keymapper.domain.utils.ClickType
import io.github.sds100.keymapper.util.delegate.GetEventDelegate
import kotlinx.serialization.Serializable
import splitties.bitflags.withFlag

/**
 * Created by sds100 on 03/03/2021.
 */

@Serializable
data class KeymapTrigger(
    val keys: List<TriggerKey> = emptyList(),
    val mode: TriggerMode = TriggerMode.Undefined,

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
            isAllowed = (keys.size == 1 || (mode is TriggerMode.Parallel))
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
            isAllowed = (keys.size == 1 || (mode is TriggerMode.Parallel))
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
                && mode is TriggerMode.Sequence
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
            //TODO
            keys = entity.keys.map { KeymapTriggerKeyEntityMapper.fromEntity(it, deviceAdapter) }
        )
    }

    fun toEntity(trigger: KeymapTrigger): TriggerEntity {
        val extras = mutableListOf<Extra>()

        trigger.options.sequenceTriggerTimeout.ifIsAllowed {
            if (it is Defaultable.Custom) {
                extras.add(Extra(TriggerEntity.EXTRA_SEQUENCE_TRIGGER_TIMEOUT, it.data.toString()))
            }
        }

        trigger.options.longPressDelay.ifIsAllowed {
            if (it is Defaultable.Custom) {
                extras.add(Extra(TriggerEntity.EXTRA_LONG_PRESS_DELAY, it.data.toString()))
            }
        }

        trigger.options.doublePressDelay.ifIsAllowed {
            if (it is Defaultable.Custom) {
                extras.add(Extra(TriggerEntity.EXTRA_DOUBLE_PRESS_DELAY, it.data.toString()))
            }
        }

        trigger.options.vibrateDuration.ifIsAllowed {
            if (it is Defaultable.Custom) {
                extras.add(Extra(TriggerEntity.EXTRA_VIBRATION_DURATION, it.data.toString()))
            }
        }

        val mode = when (trigger.mode) {
            is TriggerMode.Parallel -> TriggerEntity.PARALLEL
            TriggerMode.Sequence -> TriggerEntity.SEQUENCE
            TriggerMode.Undefined -> TriggerEntity.UNDEFINED
        }

        var flags = 0

        trigger.options.vibrate.ifIsAllowed {
            flags = flags.withFlag(TriggerEntity.TRIGGER_FLAG_VIBRATE)
        }

        trigger.options.longPressDoubleVibration.ifIsAllowed {
            flags = flags.withFlag(TriggerEntity.TRIGGER_FLAG_LONG_PRESS_DOUBLE_VIBRATION)
        }

        trigger.options.screenOffTrigger.ifIsAllowed {
            flags = flags.withFlag(TriggerEntity.TRIGGER_FLAG_SCREEN_OFF_TRIGGERS)
        }

        trigger.options.triggerFromOtherApps.ifIsAllowed {
            flags = flags.withFlag(TriggerEntity.TRIGGER_FLAG_FROM_OTHER_APPS)
        }

        trigger.options.showToast.ifIsAllowed {
            flags = flags.withFlag(TriggerEntity.TRIGGER_FLAG_SHOW_TOAST)
        }

        return TriggerEntity(
            keys = trigger.keys.map { KeymapTriggerKeyEntityMapper.toEntity(it) },
            extras = extras,
            mode = mode,
            flags = flags
        )
    }
}