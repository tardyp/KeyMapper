
package io.github.sds100.keymapper.util

import android.content.Context
import android.os.Build
import io.github.sds100.keymapper.data.model.ActionEntity
import io.github.sds100.keymapper.data.model.getData
import io.github.sds100.keymapper.util.result.valueOrNull
import splitties.bitflags.hasFlag

/**
 * Created by sds100 on 03/09/2018.
 */

object ActionUtils {

    fun isVolumeAction(actionData: String): Boolean {
        return listOf(
            OldSystemAction.VOLUME_DECREASE_STREAM,
            OldSystemAction.VOLUME_INCREASE_STREAM,
            OldSystemAction.VOLUME_DOWN,
            OldSystemAction.VOLUME_UP,
            OldSystemAction.VOLUME_MUTE,
            OldSystemAction.VOLUME_TOGGLE_MUTE,
            OldSystemAction.VOLUME_UNMUTE
        ).contains(actionData)
    }
}

val ActionEntity.canBeHeldDown: Boolean
    get() {
        val useShell =
            extras.getData(ActionEntity.EXTRA_KEY_EVENT_USE_SHELL).valueOrNull().toBoolean()

        return (type == ActionEntity.Type.KEY_EVENT && !useShell)
            || (type == ActionEntity.Type.TAP_COORDINATE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
    }

val ActionEntity.requiresIME: Boolean
    get() {
        val useShell =
            extras.getData(ActionEntity.EXTRA_KEY_EVENT_USE_SHELL).valueOrNull().toBoolean()
        return (type == ActionEntity.Type.KEY_EVENT && !useShell) ||
            type == ActionEntity.Type.TEXT_BLOCK ||
            data == OldSystemAction.MOVE_CURSOR_TO_END
    }

val ActionEntity.repeat: Boolean
    get() = flags.hasFlag(ActionEntity.ACTION_FLAG_REPEAT)

val ActionEntity.holdDown: Boolean
    get() = flags.hasFlag(ActionEntity.ACTION_FLAG_HOLD_DOWN)

val ActionEntity.showVolumeUi: Boolean
    get() = flags.hasFlag(ActionEntity.ACTION_FLAG_SHOW_VOLUME_UI)

val ActionEntity.stopRepeatingWhenTriggerPressedAgain: Boolean
    get() = extras.getData(ActionEntity.EXTRA_CUSTOM_STOP_REPEAT_BEHAVIOUR).valueOrNull()
        ?.toInt() ==
        ActionEntity.STOP_REPEAT_BEHAVIOUR_TRIGGER_PRESSED_AGAIN

val ActionEntity.stopRepeatingWhenTriggerReleased: Boolean
    get() = !stopRepeatingWhenTriggerPressedAgain

val ActionEntity.stopHoldDownWhenTriggerPressedAgain: Boolean
    get() = extras.getData(ActionEntity.EXTRA_CUSTOM_HOLD_DOWN_BEHAVIOUR).valueOrNull()?.toInt() ==
        ActionEntity.STOP_HOLD_DOWN_BEHAVIOR_TRIGGER_PRESSED_AGAIN

val ActionEntity.stopHoldDownWhenTriggerReleased: Boolean
    get() = !stopHoldDownWhenTriggerPressedAgain

val ActionEntity.delayBeforeNextAction: Int?
    get() = extras.getData(ActionEntity.EXTRA_DELAY_BEFORE_NEXT_ACTION).valueOrNull()?.toInt()

val ActionEntity.multiplier: Int?
    get() = extras.getData(ActionEntity.EXTRA_MULTIPLIER).valueOrNull()?.toInt()

val ActionEntity.holdDownDuration: Int?
    get() = extras.getData(ActionEntity.EXTRA_HOLD_DOWN_DURATION).valueOrNull()?.toInt()

val ActionEntity.repeatRate: Int?
    get() = extras.getData(ActionEntity.EXTRA_REPEAT_RATE).valueOrNull()?.toInt()

val ActionEntity.repeatDelay: Int?
    get() = extras.getData(ActionEntity.EXTRA_REPEAT_DELAY).valueOrNull()?.toInt()

fun ActionEntity.getFlagLabelList(ctx: Context): List<String> = sequence {
    ActionEntity.ACTION_FLAG_LABEL_MAP.keys.forEach { flag ->
        if (flags.hasFlag(flag)) {
            yield(ctx.str(ActionEntity.ACTION_FLAG_LABEL_MAP.getValue(flag)))
        }
    }
}.toList()