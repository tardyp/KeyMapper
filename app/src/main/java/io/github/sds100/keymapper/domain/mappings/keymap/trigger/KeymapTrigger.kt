package io.github.sds100.keymapper.domain.mappings.keymap.trigger

import io.github.sds100.keymapper.data.model.Extra
import io.github.sds100.keymapper.data.model.TriggerEntity
import io.github.sds100.keymapper.data.model.getData
import io.github.sds100.keymapper.domain.models.Option
import io.github.sds100.keymapper.domain.models.ifIsAllowed
import io.github.sds100.keymapper.domain.utils.ClickType
import io.github.sds100.keymapper.domain.utils.Defaultable
import io.github.sds100.keymapper.util.delegate.GetEventDelegate
import io.github.sds100.keymapper.util.result.valueOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import splitties.bitflags.hasFlag
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
    private val longPressDelay: Int? = null,
    private val doublePressDelay: Int? = null,
    private val vibrateDuration: Int? = null,
    private val sequenceTriggerTimeout: Int? = null,
    private val triggerFromOtherApps: Boolean = false,
    private val showToast: Boolean = false
) {


    @Transient
    val options = KeymapTriggerOptions(
        vibrate = Option(
            value = vibrate,
            isAllowed = (keys.size == 1 || (mode is TriggerMode.Parallel))
                && keys.getOrNull(0)?.clickType == ClickType.LONG_PRESS
        ),

        vibrateDuration = Option(
            value = Defaultable.create(vibrateDuration),
            isAllowed = vibrate || longPressDoubleVibration
        ),

        longPressDelay = Option(
            value = Defaultable.create(longPressDelay),
            isAllowed = keys.any { key -> key.clickType == ClickType.LONG_PRESS }
        ),

        doublePressDelay = Option(
            value = Defaultable.create(doublePressDelay),
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
            value = Defaultable.create(sequenceTriggerTimeout),
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
        entity: TriggerEntity
    ): KeymapTrigger {
        val keys = entity.keys.map { KeymapTriggerKeyEntityMapper.fromEntity(it) }

        val mode = when (entity.mode) {
            TriggerEntity.SEQUENCE -> TriggerMode.Sequence
            TriggerEntity.PARALLEL -> TriggerMode.Parallel(keys[0].clickType)
            TriggerEntity.UNDEFINED -> TriggerMode.Undefined
            else -> throw Exception("don't know how to convert trigger mode ${entity.mode}")
        }

        return KeymapTrigger(
            keys = keys,
            mode = mode,

            vibrate = entity.flags.hasFlag(TriggerEntity.TRIGGER_FLAG_VIBRATE),

            longPressDoubleVibration =
            entity.flags.hasFlag(TriggerEntity.TRIGGER_FLAG_LONG_PRESS_DOUBLE_VIBRATION),

            longPressDelay = entity.extras.getData(TriggerEntity.EXTRA_LONG_PRESS_DELAY)
                .valueOrNull()?.toIntOrNull(),

            doublePressDelay = entity.extras.getData(TriggerEntity.EXTRA_DOUBLE_PRESS_DELAY)
                .valueOrNull()?.toIntOrNull(),

            vibrateDuration = entity.extras.getData(TriggerEntity.EXTRA_VIBRATION_DURATION)
                .valueOrNull()?.toIntOrNull(),

            sequenceTriggerTimeout = entity.extras.getData(TriggerEntity.EXTRA_SEQUENCE_TRIGGER_TIMEOUT)
                .valueOrNull()?.toIntOrNull(),

            triggerFromOtherApps = entity.flags.hasFlag(TriggerEntity.TRIGGER_FLAG_FROM_OTHER_APPS),
            showToast = entity.flags.hasFlag(TriggerEntity.TRIGGER_FLAG_SHOW_TOAST),
            screenOffTrigger = entity.flags.hasFlag(TriggerEntity.TRIGGER_FLAG_SCREEN_OFF_TRIGGERS),
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
            if (it) {
                flags = flags.withFlag(TriggerEntity.TRIGGER_FLAG_VIBRATE)
            }
        }

        trigger.options.longPressDoubleVibration.ifIsAllowed {
            if (it) {
                flags =
                    flags.withFlag(TriggerEntity.TRIGGER_FLAG_LONG_PRESS_DOUBLE_VIBRATION)
            }
        }

        trigger.options.screenOffTrigger.ifIsAllowed {
            if (it) {
                flags = flags.withFlag(TriggerEntity.TRIGGER_FLAG_SCREEN_OFF_TRIGGERS)
            }
        }

        trigger.options.triggerFromOtherApps.ifIsAllowed {
            if (it) {
                flags = flags.withFlag(TriggerEntity.TRIGGER_FLAG_FROM_OTHER_APPS)
            }
        }

        trigger.options.showToast.ifIsAllowed {
            if (it) {
                flags = flags.withFlag(TriggerEntity.TRIGGER_FLAG_SHOW_TOAST)
            }
        }

        return TriggerEntity(
            keys = trigger.keys.map { KeymapTriggerKeyEntityMapper.toEntity(it) },
            extras = extras,
            mode = mode,
            flags = flags
        )
    }
}