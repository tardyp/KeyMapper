package io.github.sds100.keymapper.actions

import io.github.sds100.keymapper.system.accessibility.IAccessibilityService
import io.github.sds100.keymapper.system.apps.AppShortcutAdapter
import io.github.sds100.keymapper.system.apps.PackageManagerAdapter
import io.github.sds100.keymapper.system.devices.ExternalDevicesAdapter
import io.github.sds100.keymapper.system.inputmethod.InputMethodAdapter
import io.github.sds100.keymapper.system.inputmethod.KeyMapperImeMessenger
import io.github.sds100.keymapper.system.intents.IntentAdapter
import io.github.sds100.keymapper.system.keyevents.InputKeyModel
import io.github.sds100.keymapper.system.popup.PopupMessageAdapter
import io.github.sds100.keymapper.system.root.SuAdapter
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.ui.ResourceProvider
import splitties.bitflags.withFlag

/**
 * Created by sds100 on 14/02/21.
 */

class PerformActionsUseCaseImpl(
    private val accessibilityService: IAccessibilityService,
    private val inputMethodAdapter: InputMethodAdapter,
    private val suAdapter: SuAdapter,
    private val intentAdapter: IntentAdapter,
    private val getActionError: GetActionErrorUseCase,
    private val keyMapperImeMessenger: KeyMapperImeMessenger,
    private val packageManagerAdapter: PackageManagerAdapter,
    private val appShortcutAdapter: AppShortcutAdapter,
    private val popupMessageAdapter: PopupMessageAdapter,
    private val deviceAdapter: ExternalDevicesAdapter,
    private val resourceProvider: ResourceProvider
) : PerformActionsUseCase {

    override fun perform(
        action: ActionData,
        inputEventType: InputEventType,
        keyMetaState: Int
    ) {
        val result: Result<*>

        when (action) {
            is OpenAppAction -> {
                result = packageManagerAdapter.openApp(action.packageName)
            }
            is OpenAppShortcutAction -> {
                result = appShortcutAdapter.launchShortcut(action.uri)
            }
            is IntentAction -> {
                result = intentAdapter.send(action.target, action.uri)
            }

            is KeyEventAction -> {

                if (action.useShell) {
                    result = suAdapter.execute("input keyevent ${action.keyCode}")
                } else {
                    val deviceId: Int = getDeviceIdForKeyEventAction(action)

                    keyMapperImeMessenger.inputKeyEvent(
                        InputKeyModel(
                            keyCode = action.keyCode,
                            inputType = inputEventType,
                            metaState = keyMetaState.withFlag(action.metaState),
                            deviceId = deviceId
                        )
                    )

                    result = Success(Unit)
                }
            }

            is PhoneCallAction -> TODO()
            is ChangeDndModeSystemAction.Disable -> TODO()
            is ChangeDndModeSystemAction.Enable -> TODO()
            is ChangeDndModeSystemAction.Toggle -> TODO()
            is ChangeRingerModeSystemAction -> TODO()
            is ControlMediaForAppSystemAction.FastForward -> TODO()
            is ControlMediaForAppSystemAction.NextTrack -> TODO()
            is ControlMediaForAppSystemAction.Pause -> TODO()
            is ControlMediaForAppSystemAction.Play -> TODO()
            is ControlMediaForAppSystemAction.PlayPause -> TODO()
            is ControlMediaForAppSystemAction.PreviousTrack -> TODO()
            is ControlMediaForAppSystemAction.Rewind -> TODO()
            is CycleRotationsSystemAction -> TODO()
            is FlashlightSystemAction.Disable -> TODO()
            is FlashlightSystemAction.Enable -> TODO()
            is FlashlightSystemAction.Toggle -> TODO()
            is SimpleSystemAction -> TODO()
            is SwitchKeyboardSystemAction -> TODO()
            is VolumeSystemAction.Down -> TODO()
            is VolumeSystemAction.Mute -> TODO()
            is VolumeSystemAction.Stream.Decrease -> TODO()
            is VolumeSystemAction.Stream.Increase -> TODO()
            is VolumeSystemAction.ToggleMute -> TODO()
            is VolumeSystemAction.UnMute -> TODO()
            is VolumeSystemAction.Up -> TODO()
            is TapCoordinateAction -> TODO()
            is TextAction -> {
                keyMapperImeMessenger.inputText(action.text)
                result = Success(Unit)
            }

            is UrlAction -> TODO()
            CorruptAction -> TODO()
        }

        result.onFailure {
            popupMessageAdapter.showPopupMessage(it.getFullMessage(resourceProvider))
        }
    }

    override fun getError(action: ActionData): Error? {
        return getActionError.getError(action)
    }

    private fun getDeviceIdForKeyEventAction(action: KeyEventAction): Int {
        if (action.device?.descriptor == null) {
            return -1
        }

        val inputDevices = deviceAdapter.inputDevices.value.dataOrNull() ?: return -1

        val devicesWithSameDescriptor =
            inputDevices.filter { it.descriptor == action.device.descriptor }

        if (devicesWithSameDescriptor.isEmpty()) {
            return -1
        }

        if (devicesWithSameDescriptor.size == 1) {
            return devicesWithSameDescriptor[0].id
        }

        /*
        if there are multiple devices use the device that supports the key
        code. if none do then use the first one
         */
        val device = devicesWithSameDescriptor.singleOrNull {
            deviceAdapter.deviceHasKey(it.id, action.keyCode)
        } ?: devicesWithSameDescriptor[0]

        return device.id
    }
}

interface PerformActionsUseCase {
    fun perform(
        actionData: ActionData,
        inputEventType: InputEventType = InputEventType.DOWN_UP,
        keyMetaState: Int = 0
    )

    fun getError(action: ActionData): Error?
}