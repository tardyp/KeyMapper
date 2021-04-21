package io.github.sds100.keymapper.actions

import io.github.sds100.keymapper.system.accessibility.IAccessibilityService
import io.github.sds100.keymapper.system.apps.AppShortcutAdapter
import io.github.sds100.keymapper.system.apps.PackageManagerAdapter
import io.github.sds100.keymapper.system.inputmethod.InputMethodAdapter
import io.github.sds100.keymapper.system.inputmethod.KeyMapperImeMessenger
import io.github.sds100.keymapper.system.intents.IntentAdapter
import io.github.sds100.keymapper.system.popup.PopupMessageAdapter
import io.github.sds100.keymapper.system.root.SuAdapter
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.ui.ResourceProvider
import timber.log.Timber

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
    private val resourceProvider: ResourceProvider
) : PerformActionsUseCase {

    override fun perform(
        action: ActionData,
        inputEventType: InputEventType,
        keyMetaState: Int
    ) {
        val result: Result<*> = when (action) {
            is OpenAppAction -> packageManagerAdapter.openApp(action.packageName)
            is OpenAppShortcutAction -> appShortcutAdapter.launchShortcut(action.uri)
            is IntentAction -> intentAdapter.send(action.target, action.uri)
            is KeyEventAction -> TODO()


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
            is TextAction -> TODO()
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
}

interface PerformActionsUseCase {
    fun perform(
        actionData: ActionData,
        inputEventType: InputEventType = InputEventType.DOWN_UP,
        keyMetaState: Int = 0
    )

    fun getError(action: ActionData): Error?
}