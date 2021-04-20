package io.github.sds100.keymapper.mappings.keymaps.trigger

import io.github.sds100.keymapper.data.entities.Extra
import io.github.sds100.keymapper.data.entities.TriggerEntity
import io.github.sds100.keymapper.data.entities.getData
import io.github.sds100.keymapper.mappings.ClickType
import io.github.sds100.keymapper.system.keyevents.GetEventDelegate
import io.github.sds100.keymapper.util.valueOrNull
import kotlinx.serialization.Serializable
import splitties.bitflags.hasFlag
import splitties.bitflags.withFlag

/**
 * Created by sds100 on 03/03/2021.
 */

@Serializable
data class KeyMapTrigger(
    val keys: List<TriggerKey> = emptyList(),
    val mode: TriggerMode = TriggerMode.Undefined,
    val vibrate: Boolean = false,
    val longPressDoubleVibration: Boolean = false,
    val screenOffTrigger: Boolean = false,
    val longPressDelay: Int? = null,
    val doublePressDelay: Int? = null,
    val vibrateDuration: Int? = null,
    val sequenceTriggerTimeout: Int? = null,
    val triggerFromOtherApps: Boolean = false,
    val showToast: Boolean = false
) {
    fun isVibrateAllowed(): Boolean {
        return true
    }

    fun isChangingVibrationDurationAllowed(): Boolean {
        return vibrate || longPressDoubleVibration
    }

    fun isChangingLongPressDelayAllowed(): Boolean {
        return keys.any { key -> key.clickType == ClickType.LONG_PRESS }
    }

    fun isChangingDoublePressDelayAllowed(): Boolean {
        return keys.any { key -> key.clickType == ClickType.DOUBLE_PRESS }
    }

    fun isLongPressDoubleVibrationAllowed(): Boolean {
        return (keys.size == 1 || (mode is TriggerMode.Parallel))
            && keys.getOrNull(0)?.clickType == ClickType.LONG_PRESS
    }

    fun isDetectingWhenScreenOffAllowed(): Boolean {
        return keys.isNotEmpty() && keys.all { GetEventDelegate.canDetectKeyWhenScreenOff(it.keyCode) }
    }

    fun isChangingSequenceTriggerTimeoutAllowed(): Boolean {
        return !keys.isNullOrEmpty() && keys.size > 1 && mode is TriggerMode.Sequence
    }
}

object KeymapTriggerEntityMapper {
    fun fromEntity(
        entity: TriggerEntity
    ): KeyMapTrigger {
        val keys = entity.keys.map { KeymapTriggerKeyEntityMapper.fromEntity(it) }

        val mode = when {
            entity.mode == TriggerEntity.SEQUENCE && keys.size > 1 -> TriggerMode.Sequence
            entity.mode == TriggerEntity.PARALLEL && keys.size > 1 -> TriggerMode.Parallel(keys[0].clickType)
            else -> TriggerMode.Undefined
        }

        return KeyMapTrigger(
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

    fun toEntity(trigger: KeyMapTrigger): TriggerEntity {
        val extras = mutableListOf<Extra>()

        if (trigger.isChangingSequenceTriggerTimeoutAllowed() && trigger.sequenceTriggerTimeout != null) {
            extras.add(
                Extra(
                    TriggerEntity.EXTRA_SEQUENCE_TRIGGER_TIMEOUT,
                    trigger.sequenceTriggerTimeout.toString()
                )
            )
        }

        if (trigger.isChangingLongPressDelayAllowed() && trigger.longPressDelay != null) {
            extras.add(
                Extra(
                    TriggerEntity.EXTRA_LONG_PRESS_DELAY,
                    trigger.longPressDelay.toString()
                )
            )
        }

        if (trigger.isChangingDoublePressDelayAllowed() && trigger.doublePressDelay != null) {
            extras.add(
                Extra(
                    TriggerEntity.EXTRA_DOUBLE_PRESS_DELAY,
                    trigger.doublePressDelay.toString()
                )
            )
        }

        if (trigger.isChangingVibrationDurationAllowed() && trigger.vibrateDuration != null) {
            extras.add(
                Extra(
                    TriggerEntity.EXTRA_VIBRATION_DURATION,
                    trigger.vibrateDuration.toString()
                )
            )
        }

        val mode = when (trigger.mode) {
            is TriggerMode.Parallel -> TriggerEntity.PARALLEL
            TriggerMode.Sequence -> TriggerEntity.SEQUENCE
            TriggerMode.Undefined -> TriggerEntity.UNDEFINED
        }

        var flags = 0

        if (trigger.isVibrateAllowed() && trigger.vibrate) {
            flags = flags.withFlag(TriggerEntity.TRIGGER_FLAG_VIBRATE)
        }

        if (trigger.isLongPressDoubleVibrationAllowed() && trigger.longPressDoubleVibration) {
            flags = flags.withFlag(TriggerEntity.TRIGGER_FLAG_LONG_PRESS_DOUBLE_VIBRATION)
        }

        if (trigger.isDetectingWhenScreenOffAllowed() && trigger.screenOffTrigger) {
            flags = flags.withFlag(TriggerEntity.TRIGGER_FLAG_SCREEN_OFF_TRIGGERS)
        }

        if (trigger.triggerFromOtherApps) {
            flags = flags.withFlag(TriggerEntity.TRIGGER_FLAG_FROM_OTHER_APPS)
        }

        if (trigger.showToast) {
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