package io.github.sds100.keymapper.actions

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.view.KeyEvent
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.system.accessibility.IAccessibilityService
import io.github.sds100.keymapper.system.apps.AppShortcutAdapter
import io.github.sds100.keymapper.system.apps.PackageManagerAdapter
import io.github.sds100.keymapper.system.camera.CameraAdapter
import io.github.sds100.keymapper.system.devices.ExternalDevicesAdapter
import io.github.sds100.keymapper.system.display.DisplayAdapter
import io.github.sds100.keymapper.system.display.Orientation
import io.github.sds100.keymapper.system.files.FileAdapter
import io.github.sds100.keymapper.system.files.FileUtils
import io.github.sds100.keymapper.system.inputmethod.InputMethodAdapter
import io.github.sds100.keymapper.system.inputmethod.KeyMapperImeMessenger
import io.github.sds100.keymapper.system.intents.IntentAdapter
import io.github.sds100.keymapper.system.keyevents.InputKeyModel
import io.github.sds100.keymapper.system.phone.PhoneAdapter
import io.github.sds100.keymapper.system.popup.PopupMessageAdapter
import io.github.sds100.keymapper.system.root.SuAdapter
import io.github.sds100.keymapper.system.shell.ShellAdapter
import io.github.sds100.keymapper.system.volume.RingerMode
import io.github.sds100.keymapper.system.volume.VolumeAdapter
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.ui.ResourceProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import splitties.bitflags.withFlag

/**
 * Created by sds100 on 14/02/21.
 */

class PerformActionsUseCaseImpl(
    private val coroutineScope: CoroutineScope,
    private val accessibilityService: IAccessibilityService,
    private val inputMethodAdapter: InputMethodAdapter,
    private val fileAdapter: FileAdapter,
    private val suAdapter: SuAdapter,
    private val shellAdapter: ShellAdapter,
    private val intentAdapter: IntentAdapter,
    private val getActionError: GetActionErrorUseCase,
    private val keyMapperImeMessenger: KeyMapperImeMessenger,
    private val packageManagerAdapter: PackageManagerAdapter,
    private val appShortcutAdapter: AppShortcutAdapter,
    private val popupMessageAdapter: PopupMessageAdapter,
    private val deviceAdapter: ExternalDevicesAdapter,
    private val phoneAdapter: PhoneAdapter,
    private val volumeAdapter: VolumeAdapter,
    private val cameraAdapter: CameraAdapter,
    private val displayAdapter: DisplayAdapter,
    private val resourceProvider: ResourceProvider
) : PerformActionsUseCase {

    override fun perform(
        action: ActionData,
        inputEventType: InputEventType,
        keyMetaState: Int
    ) {
        /**
         * Is null if the action is being performed asynchronously
         */
        val result: Result<*>?

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

            is PhoneCallAction -> {
                result = phoneAdapter.startCall(action.number)
            }

            is EnableDndMode -> {
                result = volumeAdapter.enableDndMode(action.dndMode)
            }
            is ToggleDndMode -> {
                result = volumeAdapter.toggleDndMode(action.dndMode)
            }
            is ChangeRingerModeSystemAction -> {
                result = volumeAdapter.setRingerMode(action.ringerMode)
            }

            is ControlMediaForAppSystemAction.FastForward -> TODO()
            is ControlMediaForAppSystemAction.NextTrack -> TODO()
            is ControlMediaForAppSystemAction.Pause -> TODO()
            is ControlMediaForAppSystemAction.Play -> TODO()
            is ControlMediaForAppSystemAction.PlayPause -> TODO()
            is ControlMediaForAppSystemAction.PreviousTrack -> TODO()
            is ControlMediaForAppSystemAction.Rewind -> TODO()

            is CycleRotationsSystemAction -> {
                result = displayAdapter.disableAutoRotate().then {
                    val currentOrientation = displayAdapter.orientation

                    val index = action.orientations.indexOf(currentOrientation)

                    val nextOrientation = if (index == action.orientations.lastIndex) {
                        action.orientations[0]
                    } else {
                        action.orientations[index + 1]
                    }

                    displayAdapter.setOrientation(nextOrientation)
                }
            }

            is FlashlightSystemAction.Disable -> {
                result = cameraAdapter.disableFlashlight(action.lens)
            }

            is FlashlightSystemAction.Enable -> {
                result = cameraAdapter.enableFlashlight(action.lens)
            }

            is FlashlightSystemAction.Toggle -> {
                result = cameraAdapter.toggleFlashlight(action.lens)
            }

            is SwitchKeyboardSystemAction -> {
                coroutineScope.launch {
                    inputMethodAdapter.chooseIme(
                        action.imeId,
                        fromForeground = false
                    ).onSuccess {
                        val message = resourceProvider.getString(
                            R.string.toast_chose_keyboard,
                            it.label
                        )
                        popupMessageAdapter.showPopupMessage(message)
                    }.showErrorMessageOnFail()
                }

                result = null
            }

            is VolumeSystemAction.Down -> {
                result = volumeAdapter.lowerVolume(showVolumeUi = action.showVolumeUi)
            }
            is VolumeSystemAction.Up -> {
                result = volumeAdapter.raiseVolume(showVolumeUi = action.showVolumeUi)
            }

            is VolumeSystemAction.Mute -> {
                result = volumeAdapter.muteVolume(showVolumeUi = action.showVolumeUi)
            }

            is VolumeSystemAction.Stream.Decrease -> {
                result = volumeAdapter.lowerVolume(
                    stream = action.volumeStream,
                    showVolumeUi = action.showVolumeUi
                )
            }

            is VolumeSystemAction.Stream.Increase -> {
                result = volumeAdapter.raiseVolume(
                    stream = action.volumeStream,
                    showVolumeUi = action.showVolumeUi
                )
            }

            is VolumeSystemAction.ToggleMute -> {
                result = volumeAdapter.toggleMuteVolume(showVolumeUi = action.showVolumeUi)
            }

            is VolumeSystemAction.UnMute -> {
                result = volumeAdapter.unmuteVolume(showVolumeUi = action.showVolumeUi)
            }

            is TapCoordinateAction -> {
                result = accessibilityService.tapScreen(action.x, action.y, inputEventType)
            }

            is TextAction -> {
                keyMapperImeMessenger.inputText(action.text)
                result = Success(Unit)
            }

            is SimpleSystemAction -> {
                when (action.id) {
                    SystemActionId.TOGGLE_WIFI -> TODO()
                    SystemActionId.ENABLE_WIFI -> TODO()
                    SystemActionId.DISABLE_WIFI -> TODO()
                    SystemActionId.TOGGLE_BLUETOOTH -> TODO()
                    SystemActionId.ENABLE_BLUETOOTH -> TODO()
                    SystemActionId.DISABLE_BLUETOOTH -> TODO()
                    SystemActionId.TOGGLE_MOBILE_DATA -> TODO()
                    SystemActionId.ENABLE_MOBILE_DATA -> TODO()
                    SystemActionId.DISABLE_MOBILE_DATA -> TODO()

                    SystemActionId.TOGGLE_AUTO_BRIGHTNESS -> {
                        result = displayAdapter.toggleAutoBrightness()
                    }
                    SystemActionId.DISABLE_AUTO_BRIGHTNESS -> {
                        result = displayAdapter.disableAutoBrightness()
                    }
                    SystemActionId.ENABLE_AUTO_BRIGHTNESS -> {
                        result = displayAdapter.enableAutoBrightness()
                    }
                    SystemActionId.INCREASE_BRIGHTNESS -> {
                        result = displayAdapter.increaseBrightness()
                    }
                    SystemActionId.DECREASE_BRIGHTNESS -> {
                        result = displayAdapter.decreaseBrightness()
                    }

                    SystemActionId.TOGGLE_AUTO_ROTATE -> {
                        result = displayAdapter.toggleAutoRotate()
                    }

                    SystemActionId.ENABLE_AUTO_ROTATE -> {
                        result = displayAdapter.enableAutoRotate()
                    }

                    SystemActionId.DISABLE_AUTO_ROTATE -> {
                        result = displayAdapter.disableAutoRotate()
                    }

                    SystemActionId.PORTRAIT_MODE -> {
                        displayAdapter.disableAutoRotate()
                        result = displayAdapter.setOrientation(Orientation.ORIENTATION_0)
                    }

                    SystemActionId.LANDSCAPE_MODE -> {
                        displayAdapter.disableAutoRotate()
                        result = displayAdapter.setOrientation(Orientation.ORIENTATION_90)
                    }

                    SystemActionId.SWITCH_ORIENTATION -> {
                        if (displayAdapter.orientation == Orientation.ORIENTATION_180
                            || displayAdapter.orientation == Orientation.ORIENTATION_0
                        ) {
                            result = displayAdapter.setOrientation(Orientation.ORIENTATION_90)
                        } else {
                            result = displayAdapter.setOrientation(Orientation.ORIENTATION_0)
                        }
                    }

                    SystemActionId.CYCLE_RINGER_MODE -> {
                        result = when (volumeAdapter.ringerMode) {
                            RingerMode.NORMAL -> volumeAdapter.setRingerMode(RingerMode.VIBRATE)
                            RingerMode.VIBRATE -> volumeAdapter.setRingerMode(RingerMode.SILENT)
                            RingerMode.SILENT -> volumeAdapter.setRingerMode(RingerMode.NORMAL)
                        }
                    }

                    SystemActionId.CYCLE_VIBRATE_RING -> {
                        result = when (volumeAdapter.ringerMode) {
                            RingerMode.NORMAL -> volumeAdapter.setRingerMode(RingerMode.VIBRATE)
                            RingerMode.VIBRATE -> volumeAdapter.setRingerMode(RingerMode.NORMAL)
                            RingerMode.SILENT -> volumeAdapter.setRingerMode(RingerMode.NORMAL)
                        }
                    }

                    SystemActionId.DISABLE_DND_MODE -> {
                        result = volumeAdapter.disableDndMode()
                    }

                    SystemActionId.EXPAND_NOTIFICATION_DRAWER -> {
                        val globalAction = AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS

                        result = accessibilityService.doGlobalAction(globalAction).otherwise {
                            shellAdapter.execute("cmd statusbar expand-notifications")
                        }
                    }

                    SystemActionId.TOGGLE_NOTIFICATION_DRAWER -> {
                        result =
                            if (accessibilityService.rootNode.packageName == "com.android.systemui") {
                                shellAdapter.execute("cmd statusbar collapse")
                            } else {
                                val globalAction = AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS

                                accessibilityService.doGlobalAction(globalAction).otherwise {
                                    shellAdapter.execute("cmd statusbar expand-notifications")
                                }
                            }
                    }

                    SystemActionId.EXPAND_QUICK_SETTINGS -> {
                        val globalAction = AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS

                        result =
                            accessibilityService.doGlobalAction(globalAction).otherwise {
                                shellAdapter.execute("cmd statusbar expand-settings")
                            }
                    }

                    SystemActionId.TOGGLE_QUICK_SETTINGS -> {
                        result =
                            if (accessibilityService.rootNode.packageName == "com.android.systemui") {
                                shellAdapter.execute("cmd statusbar collapse")
                            } else {
                                val globalAction = AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS

                                accessibilityService.doGlobalAction(globalAction).otherwise {
                                    shellAdapter.execute("cmd statusbar expand-settings")
                                }
                            }
                    }

                    SystemActionId.COLLAPSE_STATUS_BAR -> {
                        result = shellAdapter.execute("cmd statusbar collapse")
                    }

                    SystemActionId.PAUSE_MEDIA -> TODO()
                    SystemActionId.PLAY_MEDIA -> TODO()
                    SystemActionId.PLAY_PAUSE_MEDIA -> TODO()
                    SystemActionId.NEXT_TRACK -> TODO()
                    SystemActionId.PREVIOUS_TRACK -> TODO()
                    SystemActionId.FAST_FORWARD -> TODO()
                    SystemActionId.REWIND -> TODO()

                    SystemActionId.GO_BACK -> {
                        result =
                            accessibilityService.doGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                    }
                    SystemActionId.GO_HOME -> {
                        result =
                            accessibilityService.doGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
                    }

                    SystemActionId.OPEN_RECENTS -> {
                        result =
                            accessibilityService.doGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
                    }

                    SystemActionId.TOGGLE_SPLIT_SCREEN -> {
                        result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            accessibilityService.doGlobalAction(AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN)
                        } else {
                            Error.SdkVersionTooLow(minSdk = Build.VERSION_CODES.N)
                        }
                    }

                    SystemActionId.GO_LAST_APP -> {
                        coroutineScope.launch {
                            accessibilityService.doGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
                            delay(100)
                            accessibilityService.doGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
                                .showErrorMessageOnFail()
                        }

                        result = null
                    }

                    SystemActionId.OPEN_MENU -> TODO()
                    SystemActionId.ENABLE_NFC -> TODO()
                    SystemActionId.DISABLE_NFC -> TODO()
                    SystemActionId.TOGGLE_NFC -> TODO()
                    SystemActionId.MOVE_CURSOR_TO_END -> TODO()
                    SystemActionId.TOGGLE_KEYBOARD -> TODO()
                    SystemActionId.SHOW_KEYBOARD -> TODO()
                    SystemActionId.HIDE_KEYBOARD -> TODO()
                    SystemActionId.SHOW_KEYBOARD_PICKER -> TODO()
                    SystemActionId.SHOW_KEYBOARD_PICKER_ROOT -> TODO()
                    SystemActionId.TEXT_CUT -> TODO()
                    SystemActionId.TEXT_COPY -> TODO()
                    SystemActionId.TEXT_PASTE -> TODO()
                    SystemActionId.SELECT_WORD_AT_CURSOR -> TODO()

                    SystemActionId.SWITCH_KEYBOARD -> TODO()

                    SystemActionId.TOGGLE_AIRPLANE_MODE -> TODO()
                    SystemActionId.ENABLE_AIRPLANE_MODE -> TODO()
                    SystemActionId.DISABLE_AIRPLANE_MODE -> TODO()

                    SystemActionId.SCREENSHOT -> {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                            coroutineScope.launch {
                                val picturesFolder = fileAdapter.getPicturesFolder()
                                val screenshotsFolder = "$picturesFolder/Screenshots"
                                val fileDate = FileUtils.createFileDate()

                                suAdapter.execute("mkdir -p $screenshotsFolder; screencap -p $screenshotsFolder/Screenshot_$fileDate.png")
                                    .onSuccess {
                                        popupMessageAdapter.showPopupMessage(
                                            resourceProvider.getString(
                                                R.string.toast_screenshot_taken
                                            )
                                        )
                                    }.showErrorMessageOnFail()
                            }
                            result = null

                        } else {
                            result =
                                accessibilityService.doGlobalAction(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)
                        }
                    }

                    SystemActionId.OPEN_VOICE_ASSISTANT -> TODO()
                    SystemActionId.OPEN_DEVICE_ASSISTANT -> TODO()
                    SystemActionId.OPEN_CAMERA -> TODO()

                    SystemActionId.LOCK_DEVICE -> {
                        result = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                            suAdapter.execute("input keyevent ${KeyEvent.KEYCODE_POWER}")
                        } else {
                            accessibilityService.doGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
                        }
                    }

                    SystemActionId.POWER_ON_OFF_DEVICE -> TODO()
                    SystemActionId.SECURE_LOCK_DEVICE -> TODO()

                    SystemActionId.CONSUME_KEY_EVENT -> {
                        result = Success(Unit)
                    }

                    SystemActionId.OPEN_SETTINGS -> TODO()
                    SystemActionId.SHOW_POWER_MENU -> {
                        result =
                            accessibilityService.doGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG)
                    }

                    else -> throw Exception("Don't know how to perform this action ${action.id}")
                }
            }

            is UrlAction -> TODO()

            CorruptAction -> {
                result = Error.CorruptActionError
            }
        }

        result?.showErrorMessageOnFail()
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

    private fun Result<*>.showErrorMessageOnFail() {
        onFailure {
            popupMessageAdapter.showPopupMessage(it.getFullMessage(resourceProvider))
        }
    }
}

interface PerformActionsUseCase {
    fun perform(
        action: ActionData,
        inputEventType: InputEventType = InputEventType.DOWN_UP,
        keyMetaState: Int = 0
    )

    fun getError(action: ActionData): Error?
}