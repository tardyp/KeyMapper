package io.github.sds100.keymapper.util.delegate

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.admin.DevicePolicyManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Context.DEVICE_POLICY_SERVICE
import android.content.Intent
import android.graphics.Path
import android.hardware.camera2.CameraCharacteristics
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.view.KeyEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.webkit.URLUtil
import androidx.core.os.bundleOf
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.lifecycle.Lifecycle
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.data.model.ActionEntity
import io.github.sds100.keymapper.data.model.SystemActionOption
import io.github.sds100.keymapper.data.model.getData
import io.github.sds100.keymapper.domain.usecases.PerformActionsUseCase
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.result.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import splitties.bitflags.hasFlag
import splitties.bitflags.withFlag
import splitties.toast.longToast
import splitties.toast.toast


/**
 * Created by sds100 on 25/11/2018.
 */

class ActionPerformerDelegate(
    context: Context,
    iAccessibilityService: IAccessibilityService,
    private val performActionsUseCase: PerformActionsUseCase,
    lifecycle: Lifecycle
) : IAccessibilityService by iAccessibilityService {

    companion object {
        private const val OVERFLOW_MENU_CONTENT_DESCRIPTION = "More options"
    }

    private val ctx = context.applicationContext
    private lateinit var flashlightController: FlashlightController
    private val suProcessDelegate = SuProcessDelegate()

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flashlightController = FlashlightController()
            lifecycle.addObserver(flashlightController)
        }

        lifecycle.addObserver(suProcessDelegate)
    }

    fun performAction(
        action: ActionEntity,
        chosenImePackageName: String?,
        currentPackageName: String?
    ) = performAction(
        PerformAction(action),
        chosenImePackageName,
        currentPackageName
    )

    fun performAction(
        performActionModel: PerformAction,
        chosenImePackageName: String?,
        currentPackageName: String?
    ) {
        val (action, additionalMetaState, keyEventAction) = performActionModel

        ctx.apply {
            when (action.type) {
                ActionEntity.Type.APP -> {
                    val packageName = action.data

                    val leanbackIntent =
                        packageManager.getLeanbackLaunchIntentForPackage(packageName)

                    val normalIntent = packageManager.getLaunchIntentForPackage(packageName)

                    val intent = leanbackIntent ?: normalIntent

                    //intent = null if the app doesn't exist
                    if (intent != null) {
                        startActivity(intent)
                    } else {
                        try {
                            val appInfo = ctx.packageManager.getApplicationInfo(packageName, 0)

                            //if the app is disabled, show an error message because it won't open
                            if (!appInfo.enabled) {
                                FixableError.AppDisabled(packageName)
                            }

                            Success(packageName)

                        } catch (e: Exception) {
                            FixableError.AppNotFound(packageName)
                        }.onFailure {
                            toast(it.getFullMessage(this))
                        }
                    }
                }

                ActionEntity.Type.APP_SHORTCUT -> {
                    val intent = Intent.parseUri(action.data, 0)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                    try {
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        toast(R.string.error_shortcut_not_found)
                    } catch (e: SecurityException) {
                        toast(R.string.error_keymapper_doesnt_have_permission_app_shortcut)
                    } catch (e: Exception) {
                        toast(R.string.error_opening_app_shortcut)
                    }
                }

                ActionEntity.Type.TEXT_BLOCK -> chosenImePackageName?.let {
                    KeyboardUtils.inputTextFromImeService(ctx, it, action.data)
                }

                ActionEntity.Type.URL -> {
                    val guessedUrl = URLUtil.guessUrl(action.data)
                    val uri: Uri = Uri.parse(guessedUrl)

                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                    if (intent.resolveActivity(packageManager) != null) {
                        startActivity(intent)
                    } else {
                        toast(R.string.error_no_app_found_to_open_url)
                    }
                }

                ActionEntity.Type.SYSTEM_ACTION -> performSystemAction(
                    action,
                    chosenImePackageName,
                    currentPackageName
                )

                ActionEntity.Type.TAP_COORDINATE -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        val x = action.data.split(',')[0]
                        val y = action.data.split(',')[1]

                        val duration = 1L //ms

                        val path = Path().apply {
                            moveTo(x.toFloat(), y.toFloat())
                        }

                        val strokeDescription =
                            if (action.flags.hasFlag(ActionEntity.ACTION_FLAG_HOLD_DOWN)
                                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                            ) {

                                when (keyEventAction) {
                                    InputEventType.DOWN -> GestureDescription.StrokeDescription(
                                        path,
                                        0,
                                        duration,
                                        true
                                    )
                                    InputEventType.UP -> GestureDescription.StrokeDescription(
                                        path,
                                        59999,
                                        duration,
                                        false
                                    )
                                    else -> null
                                }

                            } else {
                                GestureDescription.StrokeDescription(path, 0, duration)
                            }

                        strokeDescription?.let {
                            val gestureDescription = GestureDescription.Builder().apply {
                                addStroke(it)
                            }.build()

                            dispatchGesture(gestureDescription, null, null)
                        }
                    }
                }

                ActionEntity.Type.KEY_EVENT -> {
                    val useShell = action.extras.getData(ActionEntity.EXTRA_KEY_EVENT_USE_SHELL)
                        .valueOrNull()
                        .toBoolean()

                    if (useShell) {
                        val keyCode = action.data
                        suProcessDelegate.runCommand("input keyevent $keyCode")
                    }

                    val deviceId = action.extras
                        .getData(ActionEntity.EXTRA_KEY_EVENT_DEVICE_DESCRIPTOR)
                        .handle(
                            onSuccess = { InputDeviceUtils.getDeviceIdFromDescriptor(it) },
                            onError = { 0 }
                        )

                    chosenImePackageName?.let {
                        KeyboardUtils.inputKeyEventFromImeService(
                            ctx,
                            it,
                            keyCode = action.data.toInt(),
                            metaState = additionalMetaState.withFlag(
                                action.extras.getData(ActionEntity.EXTRA_KEY_EVENT_META_STATE)
                                    .valueOrNull()
                                    ?.toInt()
                                    ?: 0
                            ),
                            keyEventAction = keyEventAction,
                            deviceId = deviceId ?: 0
                        )
                    }
                }

                ActionEntity.Type.INTENT -> {
                    val intent = Intent.parseUri(action.data, 0)

                    try {
                        action.extras.getData(ActionEntity.EXTRA_INTENT_TARGET).onSuccess {
                            when (IntentTarget.valueOf(it)) {
                                IntentTarget.ACTIVITY -> {
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                }
                                IntentTarget.BROADCAST_RECEIVER -> sendBroadcast(intent)
                                IntentTarget.SERVICE -> startService(intent)
                            }
                        }
                    } catch (e: Exception) {
                        longToast(e.message!!)
                    }
                }

                ActionEntity.Type.PHONE_CALL -> {
                    Intent(Intent.ACTION_CALL).apply {
                        data = Uri.parse("tel:0987654321")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(this)
                    }
                }
            }
        }
    }

    fun performSystemAction(
        id: String,
        chosenImePackageName: String?,
        currentPackageName: String?
    ) = performSystemAction(
        ActionEntity(ActionEntity.Type.SYSTEM_ACTION, id),
        chosenImePackageName,
        currentPackageName
    )

    private fun performSystemAction(
        action: ActionEntity,
        chosenImePackageName: String?,
        currentPackageName: String?
    ) {

        val id = action.data

        fun getSdkValueForOption(onSuccess: (sdkOptionValue: Int) -> Unit) {
            val extraId = SystemActionOption.getExtraIdForOption(id)

            action.extras.getData(extraId).onSuccess { option ->
                val sdkOptionValue = SystemActionOption.OPTION_ID_SDK_ID_MAP[option]

                if (sdkOptionValue != null) {
                    onSuccess(sdkOptionValue)
                }
            }
        }

        fun getSdkValuesForOptionSet(onSuccess: (values: List<Int>) -> Unit) {
            val extraId = SystemActionOption.getExtraIdForOption(id)

            action.extras.getData(extraId).onSuccess { data ->
                val optionIds = data.split(',')

                val sdkValues = optionIds.map { SystemActionOption.OPTION_ID_SDK_ID_MAP[it] }

                if (sdkValues.all { it != null }) {
                    onSuccess(sdkValues.map { it!! })
                }
            }
        }

        val showVolumeUi = action.flags.hasFlag(ActionEntity.ACTION_FLAG_SHOW_VOLUME_UI)

        ctx.apply {
            when (id) {
                OldSystemAction.ENABLE_WIFI -> NetworkUtils.changeWifiStatePreQ(
                    this,
                    StateChange.ENABLE
                )
                OldSystemAction.DISABLE_WIFI -> NetworkUtils.changeWifiStatePreQ(
                    this,
                    StateChange.DISABLE
                )
                OldSystemAction.TOGGLE_WIFI -> NetworkUtils.changeWifiStatePreQ(
                    this,
                    StateChange.TOGGLE
                )

                OldSystemAction.TOGGLE_WIFI_ROOT -> NetworkUtils.toggleWifiRoot()
                OldSystemAction.ENABLE_WIFI_ROOT -> NetworkUtils.enableWifiRoot()
                OldSystemAction.DISABLE_WIFI_ROOT -> NetworkUtils.disableWifiRoot()

                OldSystemAction.TOGGLE_BLUETOOTH -> BluetoothUtils.changeBluetoothState(StateChange.TOGGLE)
                OldSystemAction.ENABLE_BLUETOOTH -> BluetoothUtils.changeBluetoothState(StateChange.ENABLE)
                OldSystemAction.DISABLE_BLUETOOTH -> BluetoothUtils.changeBluetoothState(StateChange.DISABLE)

                OldSystemAction.TOGGLE_MOBILE_DATA -> NetworkUtils.toggleMobileData(this)
                OldSystemAction.ENABLE_MOBILE_DATA -> NetworkUtils.enableMobileData()
                OldSystemAction.DISABLE_MOBILE_DATA -> NetworkUtils.disableMobileData()

                OldSystemAction.TOGGLE_AUTO_BRIGHTNESS -> BrightnessUtils.toggleAutoBrightness(this)
                OldSystemAction.ENABLE_AUTO_BRIGHTNESS ->
                    BrightnessUtils.setBrightnessMode(
                        this,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                    )

                OldSystemAction.DISABLE_AUTO_BRIGHTNESS ->
                    BrightnessUtils.setBrightnessMode(
                        this,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                    )

                OldSystemAction.INCREASE_BRIGHTNESS -> BrightnessUtils.increaseBrightness(this)
                OldSystemAction.DECREASE_BRIGHTNESS -> BrightnessUtils.decreaseBrightness(this)

                OldSystemAction.TOGGLE_AUTO_ROTATE -> ScreenRotationUtils.toggleAutoRotate(this)
                OldSystemAction.ENABLE_AUTO_ROTATE -> ScreenRotationUtils.enableAutoRotate(this)
                OldSystemAction.DISABLE_AUTO_ROTATE -> ScreenRotationUtils.disableAutoRotate(this)
                OldSystemAction.PORTRAIT_MODE -> ScreenRotationUtils.forcePortraitMode(this)
                OldSystemAction.LANDSCAPE_MODE -> ScreenRotationUtils.forceLandscapeMode(this)
                OldSystemAction.SWITCH_ORIENTATION -> ScreenRotationUtils.switchOrientation(this)

                OldSystemAction.CYCLE_ROTATIONS -> getSdkValuesForOptionSet {
                    ScreenRotationUtils.cycleRotations(this, it)
                }

                OldSystemAction.VOLUME_UP -> AudioUtils.adjustVolume(
                    this,
                    AudioManager.ADJUST_RAISE,
                    showVolumeUi
                )
                OldSystemAction.VOLUME_DOWN -> AudioUtils.adjustVolume(
                    this,
                    AudioManager.ADJUST_LOWER,
                    showVolumeUi
                )

                //the volume UI should always be shown for this action
                OldSystemAction.VOLUME_SHOW_DIALOG -> AudioUtils.adjustVolume(
                    this,
                    AudioManager.ADJUST_SAME,
                    true
                )

                OldSystemAction.VOLUME_DECREASE_STREAM -> getSdkValueForOption { stream ->
                    AudioUtils.adjustSpecificStream(
                        this,
                        AudioManager.ADJUST_LOWER,
                        showVolumeUi,
                        stream
                    )
                }

                OldSystemAction.VOLUME_INCREASE_STREAM -> getSdkValueForOption { stream ->
                    AudioUtils.adjustSpecificStream(
                        this,
                        AudioManager.ADJUST_RAISE,
                        showVolumeUi,
                        stream
                    )
                }

                OldSystemAction.CYCLE_VIBRATE_RING -> AudioUtils.cycleBetweenVibrateAndRing(this)
                OldSystemAction.CYCLE_RINGER_MODE -> AudioUtils.cycleThroughAllRingerModes(this)

                OldSystemAction.CHANGE_RINGER_MODE -> getSdkValueForOption { ringerMode ->
                    AudioUtils.changeRingerMode(this, ringerMode)
                }

                OldSystemAction.EXPAND_NOTIFICATION_DRAWER -> StatusBarUtils.expandNotificationDrawer()
                OldSystemAction.TOGGLE_NOTIFICATION_DRAWER ->
                    currentPackageName?.let { StatusBarUtils.toggleNotificationDrawer(it) }
                OldSystemAction.EXPAND_QUICK_SETTINGS -> StatusBarUtils.expandQuickSettings()
                OldSystemAction.TOGGLE_QUICK_SETTINGS_DRAWER ->
                    currentPackageName?.let { StatusBarUtils.toggleQuickSettingsDrawer(it) }
                OldSystemAction.COLLAPSE_STATUS_BAR -> StatusBarUtils.collapseStatusBar()

                OldSystemAction.ENABLE_NFC -> NfcUtils.enable()
                OldSystemAction.DISABLE_NFC -> NfcUtils.disable()
                OldSystemAction.TOGGLE_NFC -> NfcUtils.toggle(this)

                OldSystemAction.GO_BACK -> performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                OldSystemAction.GO_HOME -> performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
                OldSystemAction.OPEN_RECENTS -> performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
                OldSystemAction.OPEN_MENU -> {
                    if (performActionsUseCase.hasRootPermission) {

                        suProcessDelegate.runCommand("input keyevent ${KeyEvent.KEYCODE_MENU}\n")
                    } else {
                        rootNode.findNodeRecursively {
                            it.contentDescription == OVERFLOW_MENU_CONTENT_DESCRIPTION
                        }?.let {
                            it.performAction(AccessibilityNodeInfoCompat.ACTION_CLICK)
                            it.recycle()
                        }
                    }
                }

                OldSystemAction.GO_LAST_APP -> {
                    runBlocking {
                        performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)

                        delay(100)
                        performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
                    }
                }

                OldSystemAction.OPEN_VOICE_ASSISTANT -> {
                    val intent =
                        Intent(Intent.ACTION_VOICE_COMMAND).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }

                OldSystemAction.OPEN_DEVICE_ASSISTANT -> {
                    Intent(Intent.ACTION_ASSIST).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(this)
                    }
                }

                OldSystemAction.OPEN_CAMERA -> {
                    val intent =
                        Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }

                OldSystemAction.LOCK_DEVICE_ROOT ->
                    suProcessDelegate.runCommand("input keyevent ${KeyEvent.KEYCODE_POWER}")

                OldSystemAction.SHOW_KEYBOARD_PICKER, OldSystemAction.SHOW_KEYBOARD_PICKER_ROOT ->
                    KeyboardUtils.showInputMethodPickerDialogOutsideApp(ctx)

                OldSystemAction.SECURE_LOCK_DEVICE -> {
                    val dpm = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
                    dpm.lockNow()
                }

                OldSystemAction.POWER_ON_OFF_DEVICE -> {
                    suProcessDelegate.runCommand("input keyevent ${KeyEvent.KEYCODE_POWER}")
                }

                OldSystemAction.MOVE_CURSOR_TO_END -> chosenImePackageName?.let {
                    KeyboardUtils.inputKeyEventFromImeService(
                        ctx,
                        it,
                        keyCode = KeyEvent.KEYCODE_MOVE_END,
                        metaState = KeyEvent.META_CTRL_ON,
                        deviceId = 0
                    )
                }

                OldSystemAction.OPEN_SETTINGS -> {
                    Intent(Settings.ACTION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(this)
                    }
                }

                OldSystemAction.SWITCH_KEYBOARD -> {
                    action.extras.getData(ActionEntity.EXTRA_IME_ID).onSuccess {
                        KeyboardUtils.switchIme(this, it)
                    }
                }

                OldSystemAction.TOGGLE_AIRPLANE_MODE ->
                    AirplaneModeUtils.toggleAirplaneMode(
                        this,
                        performActionsUseCase.hasRootPermission
                    )

                OldSystemAction.ENABLE_AIRPLANE_MODE ->
                    AirplaneModeUtils.enableAirplaneMode(performActionsUseCase.hasRootPermission)

                OldSystemAction.DISABLE_AIRPLANE_MODE ->
                    AirplaneModeUtils.disableAirplaneMode(performActionsUseCase.hasRootPermission)

                OldSystemAction.SCREENSHOT_ROOT -> ScreenshotUtils.takeScreenshotRoot()

                else -> {
                    when (id) {
                        OldSystemAction.TEXT_CUT ->
                            rootNode.performActionOnFocusedNode(AccessibilityNodeInfo.ACTION_CUT)

                        OldSystemAction.TEXT_COPY ->
                            rootNode.performActionOnFocusedNode(AccessibilityNodeInfo.ACTION_COPY)

                        OldSystemAction.TEXT_PASTE ->
                            rootNode.performActionOnFocusedNode(AccessibilityNodeInfo.ACTION_PASTE)

                        OldSystemAction.SELECT_WORD_AT_CURSOR -> {
                            rootNode.focusedNode {
                                it ?: return@focusedNode

                                //it is the cursor position if they both return the same value
                                if (it.textSelectionStart == it.textSelectionEnd) {
                                    val cursorPosition = it.textSelectionStart

                                    val wordBoundary =
                                        it.text.toString().getWordBoundaries(cursorPosition)
                                            ?: return@focusedNode

                                    val bundle = bundleOf(
                                        AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT
                                            to wordBoundary.first,

                                        //The index of the cursor is the index of the last char in the word + 1
                                        AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT
                                            to wordBoundary.second + 1
                                    )

                                    it.performAction(
                                        AccessibilityNodeInfo.ACTION_SET_SELECTION,
                                        bundle
                                    )
                                }
                            }
                        }
                    }

                    when (id) {
                        OldSystemAction.PAUSE_MEDIA -> MediaUtils.pauseMediaPlayback(this)
                        OldSystemAction.PLAY_MEDIA -> MediaUtils.playMedia(this)
                        OldSystemAction.PLAY_PAUSE_MEDIA -> MediaUtils.playPauseMediaPlayback(
                            this
                        )
                        OldSystemAction.NEXT_TRACK -> MediaUtils.nextTrack(this)
                        OldSystemAction.PREVIOUS_TRACK -> MediaUtils.previousTrack(this)
                        OldSystemAction.FAST_FORWARD -> MediaUtils.fastForward(this)
                        OldSystemAction.REWIND -> MediaUtils.rewind(this)
                    }

                    when (id) {
                        OldSystemAction.SHOW_POWER_MENU ->
                            performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG)

                        OldSystemAction.PLAY_MEDIA_PACKAGE -> {
                            action.extras.getData(ActionEntity.EXTRA_PACKAGE_NAME).onSuccess {
                                MediaUtils.playMediaForPackage(ctx, it)
                            }
                        }
                        OldSystemAction.PLAY_PAUSE_MEDIA_PACKAGE -> {
                            action.extras.getData(ActionEntity.EXTRA_PACKAGE_NAME).onSuccess {
                                MediaUtils.playPauseMediaPlaybackForPackage(ctx, it)
                            }
                        }
                        OldSystemAction.PAUSE_MEDIA_PACKAGE -> {
                            action.extras.getData(ActionEntity.EXTRA_PACKAGE_NAME).onSuccess {
                                MediaUtils.pauseMediaForPackage(ctx, it)
                            }
                        }
                        OldSystemAction.NEXT_TRACK_PACKAGE -> {
                            action.extras.getData(ActionEntity.EXTRA_PACKAGE_NAME).onSuccess {
                                MediaUtils.nextTrackForPackage(ctx, it)
                            }
                        }
                        OldSystemAction.PREVIOUS_TRACK_PACKAGE -> {
                            action.extras.getData(ActionEntity.EXTRA_PACKAGE_NAME).onSuccess {
                                MediaUtils.previousTrackForPackage(ctx, it)
                            }
                        }
                        OldSystemAction.FAST_FORWARD_PACKAGE -> {
                            action.extras.getData(ActionEntity.EXTRA_PACKAGE_NAME).onSuccess {
                                MediaUtils.fastForwardForPackage(ctx, it)
                            }
                        }
                        OldSystemAction.REWIND_PACKAGE -> {
                            action.extras.getData(ActionEntity.EXTRA_PACKAGE_NAME).onSuccess {
                                MediaUtils.rewindForPackage(ctx, it)
                            }
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        var lensFacing = CameraCharacteristics.LENS_FACING_BACK

                        action.extras.getData(ActionEntity.EXTRA_LENS).onSuccess {
                            val sdkLensFacing = SystemActionOption.OPTION_ID_SDK_ID_MAP[it]!!

                            lensFacing = sdkLensFacing
                        }

                        when (id) {
                            OldSystemAction.VOLUME_UNMUTE -> AudioUtils.adjustVolume(
                                this,
                                AudioManager.ADJUST_UNMUTE,
                                showVolumeUi
                            )

                            OldSystemAction.VOLUME_MUTE -> AudioUtils.adjustVolume(
                                this,
                                AudioManager.ADJUST_MUTE,
                                showVolumeUi
                            )

                            OldSystemAction.VOLUME_TOGGLE_MUTE ->
                                AudioUtils.adjustVolume(
                                    this,
                                    AudioManager.ADJUST_TOGGLE_MUTE,
                                    showVolumeUi
                                )

                            OldSystemAction.TOGGLE_FLASHLIGHT -> flashlightController.toggleFlashlight(
                                lensFacing
                            )
                            OldSystemAction.ENABLE_FLASHLIGHT -> flashlightController.setFlashlightMode(
                                true,
                                lensFacing
                            )
                            OldSystemAction.DISABLE_FLASHLIGHT -> flashlightController.setFlashlightMode(
                                false,
                                lensFacing
                            )

                            OldSystemAction.TOGGLE_DND_MODE,
                            OldSystemAction.ENABLE_DND_MODE -> {
                                action.extras.getData(ActionEntity.EXTRA_DND_MODE).onSuccess {
                                    val mode = SystemActionOption.OPTION_ID_SDK_ID_MAP[it]
                                        ?: return@onSuccess

                                    when (id) {
                                        OldSystemAction.TOGGLE_DND_MODE -> AudioUtils.toggleDndMode(
                                            mode
                                        )
                                        OldSystemAction.ENABLE_DND_MODE -> AudioUtils.enableDndMode(
                                            mode
                                        )
                                    }
                                }
                            }

                            OldSystemAction.DISABLE_DND_MODE -> AudioUtils.disableDnd()
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        when (id) {
                            OldSystemAction.TOGGLE_KEYBOARD -> keyboardController?.toggle(this)
                            OldSystemAction.SHOW_KEYBOARD -> keyboardController?.show(this)
                            OldSystemAction.HIDE_KEYBOARD -> keyboardController?.hide(this)

                            OldSystemAction.TOGGLE_SPLIT_SCREEN ->
                                performGlobalAction(AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN)
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        when (id) {
                            OldSystemAction.SCREENSHOT ->
                                performGlobalAction(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)

                            OldSystemAction.LOCK_DEVICE ->
                                performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
                        }
                    }
                }
            }
        }
    }
}