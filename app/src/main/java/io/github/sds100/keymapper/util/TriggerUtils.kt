package io.github.sds100.keymapper.util

import android.content.Context
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.data.model.DeviceInfoEntity
import io.github.sds100.keymapper.data.model.TriggerEntity
import io.github.sds100.keymapper.data.model.TriggerEntity.Companion.DOUBLE_PRESS
import io.github.sds100.keymapper.data.model.TriggerEntity.Companion.LONG_PRESS
import io.github.sds100.keymapper.data.model.TriggerEntity.Companion.PARALLEL
import io.github.sds100.keymapper.data.model.TriggerEntity.Companion.SEQUENCE
import io.github.sds100.keymapper.data.model.TriggerEntity.Companion.TRIGGER_FLAG_LABEL_MAP
import io.github.sds100.keymapper.ui.fragment.keymap.TriggerKeyListItemModel
import splitties.bitflags.hasFlag

/**
 * Created by sds100 on 02/03/2020.
 */

fun TriggerEntity.getFlagLabelList(ctx: Context): List<String> = sequence {
    TRIGGER_FLAG_LABEL_MAP.keys.forEach { flag ->
        if (flags.hasFlag(flag)) {
            yield(ctx.str(TRIGGER_FLAG_LABEL_MAP.getValue(flag)))
        }
    }
}.toList()

fun TriggerEntity.buildTriggerFlagsDescription(ctx: Context): String = buildString {
    getFlagLabelList(ctx).forEachIndexed { index, label ->
        if (index > 0) {
            append(" ${ctx.str(R.string.middot)} ")
        }

        append(label)
    }
}

fun TriggerEntity.buildDescription(
    ctx: Context,
    deviceInfoList: List<DeviceInfoEntity>,
    showDeviceDescriptor: Boolean
): String = buildString {
    val separator = when (mode) {
        PARALLEL -> ctx.str(R.string.plus)
        SEQUENCE -> ctx.str(R.string.arrow)
        else -> ctx.str(R.string.plus)
    }

    val longPress = ctx.str(R.string.clicktype_long_press)
    val doublePress = ctx.str(R.string.clicktype_double_press)

    keys.forEachIndexed { index, key ->
        if (index > 0) {
            append("  $separator ")
        }

        when (key.clickType) {
            LONG_PRESS -> append(longPress)
            DOUBLE_PRESS -> append(doublePress)
        }

        append(" ${KeyEventUtils.keycodeToString(key.keyCode)}")

        val deviceName = key.getDeviceName(ctx, deviceInfoList, showDeviceDescriptor)
        append(" (")

        append(deviceName)

        val flagLabels = key.getFlagLabelList(ctx)

        flagLabels.forEach { label ->
            append(" ${ctx.str(R.string.middot)} ")
            append(label)
        }

        append(")")
    }
}

fun TriggerEntity.KeyEntity.buildModel(
    ctx: Context,
    deviceInfoList: List<DeviceInfoEntity>,
    showDeviceDescriptors: Boolean
): TriggerKeyListItemModel {
//
//    val extraInfo = buildString {
//        append(getDeviceName(ctx, deviceInfoList, showDeviceDescriptors))
//
//        val flagLabels = getFlagLabelList(ctx)
//
//        flagLabels.forEach { label ->
//            append(" ${ctx.str(R.string.middot)} ")
//            append(label)
//        }
//    }
//
//    return TriggerKeyListItemModel(
//        id = uid,
//        keyCode = keyCode,
//        name = KeyEventUtils.keycodeToString(keyCode),
//        clickType = clickType,
//        extraInfo = extraInfo
//    )

    TODO()
}

fun TriggerEntity.KeyEntity.getDeviceName(
    ctx: Context,
    deviceInfoList: List<DeviceInfoEntity>,
    showDeviceDescriptor: Boolean
): String =
    when (deviceId) {
        TriggerEntity.KeyEntity.DEVICE_ID_THIS_DEVICE -> ctx.str(R.string.this_device)
        TriggerEntity.KeyEntity.DEVICE_ID_ANY_DEVICE -> ctx.str(R.string.any_device)
        else -> {
            val deviceInfo = deviceInfoList.find { it.descriptor == deviceId }

            when {
                deviceInfo == null -> ctx.str(R.string.dont_know_device_name)

                showDeviceDescriptor ->
                    "${deviceInfo.name} ${deviceInfo.descriptor.substring(0..4)}"

                else -> deviceInfo.name
            }
        }
    }

fun TriggerEntity.KeyEntity.getFlagLabelList(ctx: Context): List<String> = sequence {
    TriggerEntity.KeyEntity.TRIGGER_KEY_FLAG_LABEL_MAP.keys.forEach { flag ->
        if (flags.hasFlag(flag)) {
            yield(ctx.str(TriggerEntity.KeyEntity.TRIGGER_KEY_FLAG_LABEL_MAP.getValue(flag)))
        }
    }
}.toList()

val TriggerEntity.triggerFromOtherApps: Boolean
    get() = flags.hasFlag(TriggerEntity.TRIGGER_FLAG_FROM_OTHER_APPS)

val TriggerEntity.showToast: Boolean
    get() = flags.hasFlag(TriggerEntity.TRIGGER_FLAG_SHOW_TOAST)

val TriggerEntity.vibrate: Boolean
    get() = flags.hasFlag(TriggerEntity.TRIGGER_FLAG_VIBRATE)