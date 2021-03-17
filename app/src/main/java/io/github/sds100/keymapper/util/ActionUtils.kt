
package io.github.sds100.keymapper.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.KeyEvent
import io.github.sds100.keymapper.Constants
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.data.model.*
import io.github.sds100.keymapper.util.SystemActionUtils.getDescriptionWithOption
import io.github.sds100.keymapper.util.SystemActionUtils.getDescriptionWithOptionSet
import io.github.sds100.keymapper.util.result.*
import splitties.bitflags.hasFlag

/**
 * Created by sds100 on 03/09/2018.
 */

object ActionUtils {

    fun isVolumeAction(actionData: String): Boolean {
        return listOf(
            SystemAction.VOLUME_DECREASE_STREAM,
            SystemAction.VOLUME_INCREASE_STREAM,
            SystemAction.VOLUME_DOWN,
            SystemAction.VOLUME_UP,
            SystemAction.VOLUME_MUTE,
            SystemAction.VOLUME_TOGGLE_MUTE,
            SystemAction.VOLUME_UNMUTE
        ).contains(actionData)
    }
}

@Suppress("EXPERIMENTAL_API_USAGE")
fun ActionEntity.buildModel(
    ctx: Context,
    deviceInfoList: List<DeviceInfoEntity>,
    showDeviceDescriptors: Boolean,
    hasRootPermission: Boolean
): ActionModel {
    var title: String? = null
    var icon: Drawable? = null

    val error = getTitle(ctx, deviceInfoList, showDeviceDescriptors).onSuccess { title = it }
        .then { getIcon(ctx).onSuccess { icon = it } }
        .then { canBePerformed(ctx, hasRootPermission) }
        .errorOrNull()

    val extraInfo = buildString {
        val interpunct = ctx.str(R.string.middot)
        val flagLabels = getFlagLabelList(ctx)

        flagLabels.forEachIndexed { index, label ->
            if (index != 0) {
                append(" $interpunct ")
            }

            append(label)
        }

        extras.getData(ActionEntity.EXTRA_DELAY_BEFORE_NEXT_ACTION).onSuccess {
            if (this.isNotBlank()) {
                append(" $interpunct ")
            }

            append(ctx.str(R.string.action_title_wait, it))
        }
    }.takeIf { it.isNotBlank() }

    return ActionModel(uid, type, title, icon, extraInfo, error, error?.getBriefMessage(ctx))
}

fun ActionEntity.buildChipModel(
    ctx: Context,
    deviceInfoList: List<DeviceInfoEntity>,
    showDeviceDescriptors: Boolean,
    hasRootPermission: Boolean
): ActionChipModel {
    var title: String? = null
    var icon: Drawable? = null

    val error = getTitle(ctx, deviceInfoList, showDeviceDescriptors).onSuccess { title = it }
        .then { getIcon(ctx).onSuccess { icon = it } }
        .then { canBePerformed(ctx, hasRootPermission) }
        .errorOrNull()

    val description = buildString {
        val interpunct = ctx.str(R.string.middot)

        val flagLabels = getFlagLabelList(ctx)

        if (title == null) {
            append(error?.getBriefMessage(ctx))
        } else {
            append(title)
        }

        flagLabels.forEach {
            append(" $interpunct $it")
        }

        extras.getData(ActionEntity.EXTRA_DELAY_BEFORE_NEXT_ACTION).onSuccess {
            append(" $interpunct ${ctx.str(R.string.action_title_wait, it)}")
        }
    }.takeIf { it.isNotBlank() }

    return ActionChipModel(type, description, error, icon)
}

fun ActionEntity.getTitle(
    ctx: Context,
    deviceInfoList: List<DeviceInfoEntity>,
    showDeviceDescriptors: Boolean
): Result<String> {
    return when (type) {
        ActionType.APP -> {
            try {
                val applicationInfo =
                    ctx.packageManager.getApplicationInfo(data, PackageManager.GET_META_DATA)

                val applicationLabel = ctx.packageManager.getApplicationLabel(applicationInfo)

                Success(ctx.str(R.string.description_open_app, applicationLabel.toString()))
            } catch (e: PackageManager.NameNotFoundException) {
                //the app isn't installed
                RecoverableError.AppNotFound(data)
            }
        }

        ActionType.APP_SHORTCUT -> {
            extras.getData(ActionEntity.EXTRA_SHORTCUT_TITLE)
        }

        ActionType.KEY_EVENT -> {
            val key = if (data.toInt() > KeyEvent.getMaxKeyCode()) {
                "Key Code $data"
            } else {
                KeyEvent.keyCodeToString(data.toInt())
            }

            val metaStateString = buildString {

                extras.getData(ActionEntity.EXTRA_KEY_EVENT_META_STATE).onSuccess { metaState ->
                    KeyEventUtils.MODIFIER_LABELS.entries.forEach {
                        val modifier = it.key
                        val labelRes = it.value

                        if (metaState.toInt().hasFlag(modifier)) {
                            append("${ctx.str(labelRes)} + ")
                        }
                    }
                }
            }

            val useShell = extras.getData(ActionEntity.EXTRA_KEY_EVENT_USE_SHELL)
                .valueOrNull()
                .toBoolean()

            val title = extras.getData(ActionEntity.EXTRA_KEY_EVENT_DEVICE_DESCRIPTOR).handle(
                onSuccess = { descriptor ->
                    val deviceName =
                        deviceInfoList.find { it.descriptor == descriptor }?.name?.let { name ->
                            if (showDeviceDescriptors) {
                                "$name (${descriptor.substring(0..4)})"
                            } else {
                                name
                            }
                        }

                    val strRes = if (useShell) {
                        R.string.description_keyevent_from_device_through_shell
                    } else {
                        R.string.description_keyevent_from_device
                    }

                    ctx.str(
                        strRes,
                        formatArgArray = arrayOf(metaStateString, key, deviceName)
                    )
                },

                onError = {
                    val strRes = if (useShell) {
                        R.string.description_keyevent_through_shell
                    } else {
                        R.string.description_keyevent
                    }

                    ctx.str(strRes, formatArgArray = arrayOf(metaStateString, key))
                }
            )

            Success(title)
        }

        ActionType.TEXT_BLOCK -> {
            val text = data
            Success(ctx.str(R.string.description_text_block, text))
        }

        ActionType.URL -> {
            Success(ctx.str(R.string.description_url, data))
        }

        ActionType.SYSTEM_ACTION -> {
            val systemActionId = data

            SystemActionUtils.getSystemActionDef(systemActionId) then { systemActionDef ->
                if (systemActionDef.hasOptions) {
                    val optionData =
                        extras.getData(SystemActionOption.getExtraIdForOption(systemActionId))

                    when (systemActionDef.optionType) {
                        OptionType.SINGLE -> {
                            optionData then {
                                SystemActionOption.getOptionLabel(ctx, systemActionId, it)
                            } then {
                                Success(systemActionDef.getDescriptionWithOption(ctx, it))

                            } otherwise {
                                if (systemActionId == SystemAction.SWITCH_KEYBOARD) {

                                    extras.getData(ActionEntity.EXTRA_IME_NAME) then {
                                        Success(systemActionDef.getDescriptionWithOption(ctx, it))
                                    }

                                } else {
                                    Success(ctx.str(systemActionDef.descriptionRes))
                                }
                            }
                        }

                        OptionType.MULTIPLE -> {
                            optionData then {
                                SystemActionOption.optionSetFromString(it)
                            } then {
                                SystemActionOption.labelsFromOptionSet(ctx, systemActionId, it)
                            } then {
                                Success(systemActionDef.getDescriptionWithOptionSet(ctx, it))
                            }
                        }
                    }
                } else {
                    Success(ctx.str(systemActionDef.descriptionRes))
                }
            }
        }

        ActionType.TAP_COORDINATE -> {
            val x = data.split(',')[0]
            val y = data.split(',')[1]

            extras.getData(ActionEntity.EXTRA_COORDINATE_DESCRIPTION) then {
                Success(
                    ctx.str(
                        resId = R.string.description_tap_coordinate_with_description,
                        formatArgArray = arrayOf(x, y, it)
                    )
                )
            } otherwise {
                Success(
                    ctx.str(
                        resId = R.string.description_tap_coordinate_default,
                        formatArgArray = arrayOf(x, y)
                    )
                )
            }
        }

        ActionType.INTENT -> {
            extras.getData(ActionEntity.EXTRA_INTENT_DESCRIPTION) then { description ->
                extras.getData(ActionEntity.EXTRA_INTENT_TARGET) then { target ->
                    val title = when (IntentTarget.valueOf(target)) {
                        IntentTarget.ACTIVITY ->
                            ctx.str(R.string.action_title_intent_start_activity, description)

                        IntentTarget.BROADCAST_RECEIVER ->
                            ctx.str(R.string.action_title_intent_send_broadcast, description)

                        IntentTarget.SERVICE ->
                            ctx.str(R.string.action_title_intent_start_service, description)
                    }

                    Success(title)
                }
            }
        }

        ActionType.PHONE_CALL -> Success(ctx.str(R.string.description_phone_call, data))

    }
        .then {
            extras.getData(ActionEntity.EXTRA_MULTIPLIER).valueOrNull()?.toIntOrNull()
                ?.let { multiplier ->
                    return@then Success("(${multiplier}x) $it")
                }

            Success(it)
        }
}

/**
 * Get the icon for any Action
 */
fun ActionEntity.getIcon(ctx: Context): Result<Drawable?> = when (type) {
    ActionType.APP -> {
        try {
            Success(ctx.packageManager.getApplicationIcon(data))
        } catch (e: PackageManager.NameNotFoundException) {
            //if the app isn't installed, it can't find the icon for it
            RecoverableError.AppNotFound(data)
        }
    }

    ActionType.APP_SHORTCUT -> extras.getData(ActionEntity.EXTRA_PACKAGE_NAME).then {
        try {
            Success(ctx.packageManager.getApplicationIcon(it))
        } catch (e: PackageManager.NameNotFoundException) {
            RecoverableError.AppNotFound(it)
        }
    } otherwise { Success(null) }

    ActionType.SYSTEM_ACTION -> {
        //convert the string representation of the enum entry into an enum object
        val systemActionId = data

        SystemActionUtils.getSystemActionDef(systemActionId).then { def ->
            Success(null)
            Success(def.iconRes?.let { ctx.drawable(it) })
        }
    }

    else -> Success(null)
}

fun ActionEntity.canBePerformed(ctx: Context, hasRootPermission: Boolean): Result<ActionEntity> {
    //the action has no data
    if (data.isEmpty()) return Error.NoActionData

    if (requiresIME) {
        if (!KeyboardUtils.isCompatibleImeEnabled()) {
            return RecoverableError.NoCompatibleImeEnabled
        }

        if (!KeyboardUtils.isCompatibleImeChosen(ctx)) {
            return RecoverableError.NoCompatibleImeChosen
        }
    }

    when (type) {
        ActionType.APP, ActionType.APP_SHORTCUT -> {
            val packageName: Result<String> =
                if (type == ActionType.APP) {
                    Success(data)
                } else {
                    extras.getData(ActionEntity.EXTRA_PACKAGE_NAME)
                }

            return packageName.then {
                try {
                    val appInfo = ctx.packageManager.getApplicationInfo(it, 0)

                    //if the app is disabled, show an error message because it won't open
                    if (!appInfo.enabled) {
                        return@then RecoverableError.AppDisabled(data)
                    }

                    return@then Success(this)

                } catch (e: Exception) {
                    return@then RecoverableError.AppNotFound(data)
                }
            }.otherwise {
                if (type == ActionType.APP_SHORTCUT) {
                    Success(this)
                } else {
                    it
                }
            }
        }

        ActionType.KEY_EVENT -> {
            val useShell = extras.getData(ActionEntity.EXTRA_KEY_EVENT_USE_SHELL)
                .valueOrNull()
                .toBoolean()

            if (useShell && !hasRootPermission) {
                return RecoverableError.PermissionDenied(Constants.PERMISSION_ROOT)
            }
        }

        ActionType.TAP_COORDINATE -> {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                return Error.SdkVersionTooLow(Build.VERSION_CODES.N)
            }
        }

        ActionType.PHONE_CALL -> {
            if (!PermissionUtils.isPermissionGranted(ctx, Manifest.permission.CALL_PHONE)) {
                return RecoverableError.PermissionDenied(Manifest.permission.CALL_PHONE)
            }
        }

        ActionType.SYSTEM_ACTION -> {
            SystemActionUtils.getSystemActionDef(data).onSuccess { systemActionDef ->

                //If an activity to open doesn't exist, the app crashes.
                if (systemActionDef.id == SystemAction.OPEN_VOICE_ASSISTANT) {
                    val activityExists =
                        Intent(Intent.ACTION_VOICE_COMMAND).resolveActivityInfo(
                            ctx.packageManager,
                            0
                        ) != null

                    if (!activityExists) {
                        return Error.NoVoiceAssistant
                    }
                }

                if (Build.VERSION.SDK_INT < systemActionDef.minApi) {
                    return Error.SdkVersionTooLow(systemActionDef.minApi)
                }

                if (Build.VERSION.SDK_INT > systemActionDef.maxApi) {
                    return Error.SdkVersionTooHigh(systemActionDef.maxApi)
                }

                systemActionDef.permissions.forEach { permission ->
                    if (!PermissionUtils.isPermissionGranted(ctx, permission)) {
                        return RecoverableError.PermissionDenied(permission)
                    }
                }

                for (feature in systemActionDef.features) {
                    if (!ctx.packageManager.hasSystemFeature(feature)) {
                        return Error.FeatureUnavailable(feature)
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (systemActionDef.id == SystemAction.TOGGLE_FLASHLIGHT
                        || systemActionDef.id == SystemAction.ENABLE_FLASHLIGHT
                        || systemActionDef.id == SystemAction.DISABLE_FLASHLIGHT
                    ) {

                        extras.getData(ActionEntity.EXTRA_LENS).onSuccess { lensOptionId ->
                            val sdkLensId = SystemActionOption.OPTION_ID_SDK_ID_MAP[lensOptionId]
                                ?: error("Can't find sdk id for that option id")

                            if (!CameraUtils.hasFlashFacing(sdkLensId)) {

                                when (lensOptionId) {
                                    SystemActionOption.LENS_FRONT -> Error.FrontFlashNotFound
                                    SystemActionOption.LENS_BACK -> Error.BackFlashNotFound
                                }
                            }
                        }
                    }
                }

                if (systemActionDef.id == SystemAction.SWITCH_KEYBOARD) {

                    extras.getData(ActionEntity.EXTRA_IME_ID).onSuccess { imeId ->
                        if (!KeyboardUtils.isImeEnabled(imeId)) {
                            var errorData = imeId

                            extras.getData(ActionEntity.EXTRA_IME_NAME).onSuccess { imeName ->
                                errorData = imeName
                            }

                            return Error.ImeNotFound(errorData)
                        }
                    }
                }
            }
        }
    }

    return Success(this)
}

val ActionEntity.canBeHeldDown: Boolean
    get() {
        val useShell =
            extras.getData(ActionEntity.EXTRA_KEY_EVENT_USE_SHELL).valueOrNull().toBoolean()

        return (type == ActionType.KEY_EVENT && !useShell)
            || (type == ActionType.TAP_COORDINATE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
    }

val ActionEntity.requiresIME: Boolean
    get() {
        val useShell =
            extras.getData(ActionEntity.EXTRA_KEY_EVENT_USE_SHELL).valueOrNull().toBoolean()
        return (type == ActionType.KEY_EVENT && !useShell) ||
            type == ActionType.TEXT_BLOCK ||
            data == SystemAction.MOVE_CURSOR_TO_END
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