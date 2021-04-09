package io.github.sds100.keymapper.ui.mappings.keymap

import android.os.Build
import android.view.KeyEvent
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.data.model.options.TriggerKeyOptions
import io.github.sds100.keymapper.domain.mappings.keymap.ConfigKeyMapUseCase
import io.github.sds100.keymapper.domain.mappings.keymap.KeyMap
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.*
import io.github.sds100.keymapper.domain.usecases.OnboardingUseCase
import io.github.sds100.keymapper.domain.utils.ClickType
import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.domain.utils.mapData
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.mappings.keymaps.DisplayKeyMapUseCase
import io.github.sds100.keymapper.mappings.keymaps.KeyMapTriggerError
import io.github.sds100.keymapper.permissions.Permission
import io.github.sds100.keymapper.ui.*
import io.github.sds100.keymapper.ui.dialogs.RequestUserResponse
import io.github.sds100.keymapper.ui.fragment.keymap.ChooseTriggerKeyDeviceModel
import io.github.sds100.keymapper.ui.fragment.keymap.TriggerKeyListItem
import io.github.sds100.keymapper.ui.shortcuts.CreateKeyMapShortcutUseCase
import io.github.sds100.keymapper.util.KeyEventUtils
import io.github.sds100.keymapper.util.ViewPopulated
import io.github.sds100.keymapper.util.result.FixableError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Created by sds100 on 24/11/20.
 */

//TODO rename as ConfigKeymapTriggerViewModel
class TriggerViewModel(
    private val coroutineScope: CoroutineScope,
    private val onboarding: OnboardingUseCase,
    private val config: ConfigKeyMapUseCase,
    private val recordTrigger: RecordTriggerUseCase,
    private val createKeyMapShortcut: CreateKeyMapShortcutUseCase,
    private val displayKeyMap: DisplayKeyMapUseCase,
    resourceProvider: ResourceProvider
) : ResourceProvider by resourceProvider, UserResponseViewModel by UserResponseViewModelImpl() {

    private companion object {
        const val KEY_ENABLE_ACCESSIBILITY_SERVICE_DIALOG = "enable_accessibility_service"
    }

    val optionsViewModel = TriggerOptionsViewModel(
        coroutineScope,
        onboarding,
        config,
        createKeyMapShortcut,
        resourceProvider
    )

    //TODO dialog that prompts the user to restart the accessibility service if failing to record a trigger too many times
    //TODO dialogs
    private val _showEnableCapsLockKeyboardLayoutPrompt = MutableSharedFlow<Unit>()
    val showEnableCapsLockKeyboardLayoutPrompt =
        _showEnableCapsLockKeyboardLayoutPrompt.asSharedFlow()

    fun approvedParallelTriggerOrderExplanation() {
        onboarding.shownParallelTriggerOrderExplanation = true
    }

    fun approvedSequenceTriggerExplanation() {
        onboarding.shownSequenceTriggerExplanation = true
    }

    private val _showChooseDeviceDialog = MutableSharedFlow<ChooseTriggerKeyDeviceModel>()
    val showChooseDeviceDialog = _showChooseDeviceDialog.asSharedFlow()

    private val _fixError = MutableSharedFlow<FixableError>()
    val fixError = _fixError.asSharedFlow()

    private val _state = MutableStateFlow(
        UiBuilder(State.Loading, RecordTriggerState.Stopped).build()
    )
    val state = _state.asStateFlow()


    init {
        recordTrigger.onRecordKey.onEach {

            if (it.keyCode == KeyEvent.KEYCODE_CAPS_LOCK) {
                _showEnableCapsLockKeyboardLayoutPrompt.emit(Unit)
            }

            config.addTriggerKey(it.keyCode, it.device)
        }.launchIn(coroutineScope)

        //TODO dialogs
        coroutineScope.launch {
            combine(
                config.mapping,
                recordTrigger.state
            ) { configState, recordTriggerState ->
                UiBuilder(configState.mapData { it.trigger }, recordTriggerState)
            }.collectLatest {
                _state.value = it.build()
            }
        }
    }

    fun setParallelTriggerModeChecked(checked: Boolean) {
        if (checked) {
            config.setParallelTriggerMode()
        }
    }

    fun setSequenceTriggerModeChecked(checked: Boolean) {
        if (checked) {
            config.setSequenceTriggerMode()
        }
    }

    fun setShortPressButtonChecked(checked: Boolean) {
        if (checked) {
            config.setParallelTriggerShortPress()
        }
    }

    fun setLongPressButtonChecked(checked: Boolean) {
        if (checked) {
            config.setParallelTriggerLongPress()
        }
    }

    fun setTriggerKeyDevice(uid: String, device: TriggerKeyDevice) =
        config.setTriggerKeyDevice(uid, device)

    fun onRemoveKeyClick(uid: String) = config.removeTriggerKey(uid)
    fun onMoveTriggerKey(fromIndex: Int, toIndex: Int) = config.moveTriggerKey(fromIndex, toIndex)

    fun onTriggerKeyOptionsClick(id: String) {
//        TODO()
    }

    fun setTriggerKeyOptions(options: TriggerKeyOptions) {
//        TODO()
    }

    fun onChooseDeviceClick(keyUid: String) {
        coroutineScope.launch {
            val devices = config.getAvailableTriggerKeyDevices()

            _showChooseDeviceDialog.emit(ChooseTriggerKeyDeviceModel(keyUid, devices))
        }
    }

    fun onRecordTriggerButtonClick() {
        coroutineScope.launch {
            val recordTriggerState = recordTrigger.state.firstOrNull()

            when (recordTriggerState) {
                is RecordTriggerState.CountingDown -> recordTrigger.stopRecording()
                RecordTriggerState.Stopped -> {
                    val recordResult = recordTrigger.startRecording()

                    if (recordResult is FixableError.AccessibilityServiceDisabled) {

                        val snackBar = RequestUserResponse.SnackBar(
                            title = getString(R.string.dialog_message_enable_accessibility_service_to_record_trigger),
                            actionText = getString(R.string.pos_turn_on)
                        )

                        val response =
                            getUserResponse(KEY_ENABLE_ACCESSIBILITY_SERVICE_DIALOG, snackBar)
                        if (response != null) {
                            _fixError.emit(recordResult)
                        }
                    }
                }
            }
        }
    }

    fun stopRecordingTrigger() = recordTrigger.stopRecording()

    fun fixError(listItemId: String) {
        coroutineScope.launch {
            when (KeyMapTriggerError.valueOf(listItemId)) {
                KeyMapTriggerError.DND_ACCESS_DENIED -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val error =
                        FixableError.PermissionDenied(Permission.ACCESS_NOTIFICATION_POLICY)
                    _fixError.emit(error)
                }
            }
        }
    }

    fun createListItems(trigger: KeyMapTrigger): List<TriggerKeyListItem> =
        trigger.keys.mapIndexed { index, key ->
            val extraInfo = buildString {
                append(getDeviceName(key.device))

                if (!key.consumeKeyEvent) {
                    val midDot = getString(R.string.middot)
                    append(" $midDot ${getString(R.string.flag_dont_override_default_action)}")
                }
            }

            val clickTypeString = when (key.clickType) {
                ClickType.SHORT_PRESS -> null
                ClickType.LONG_PRESS -> getString(R.string.clicktype_long_press)
                ClickType.DOUBLE_PRESS -> getString(R.string.clicktype_double_press)
            }

            val linkDrawable = when {
                trigger.mode is TriggerMode.Parallel && index < trigger.keys.lastIndex -> TriggerKeyLinkType.PLUS
                trigger.mode is TriggerMode.Sequence && index < trigger.keys.lastIndex -> TriggerKeyLinkType.ARROW
                else -> TriggerKeyLinkType.HIDDEN
            }

            TriggerKeyListItem(
                id = key.uid,
                keyCode = key.keyCode,
                name = KeyEventUtils.keycodeToString(key.keyCode),
                clickTypeString = clickTypeString,
                extraInfo = extraInfo,
                linkType = linkDrawable,
                isDragDropEnabled = trigger.keys.size > 1
            )
        }

    private fun getDeviceName(device: TriggerKeyDevice): String =
        when (device) {
            is TriggerKeyDevice.Internal -> getString(R.string.this_device)
            is TriggerKeyDevice.Any -> getString(R.string.any_device)
            is TriggerKeyDevice.External -> device.name
        }

    /**
     * Use this object to create the ui state instead of a function because it allows one to combine
     * multiple flows and then only finish collecting the *latest* combination of those flows.
     */
    private inner class UiBuilder(
        private val triggerState: State<KeyMapTrigger>,
        private val recordTriggerState: RecordTriggerState
    ) {
        val recordTriggerButtonText by lazy {
            when (recordTriggerState) {
                is RecordTriggerState.CountingDown -> getString(
                    R.string.button_recording_trigger_countdown,
                    recordTriggerState.timeLeft
                )
                RecordTriggerState.Stopped -> getString(R.string.button_record_trigger)
            }
        }

        val errorListItems: List<TextListItem.Error> by lazy {
            if (triggerState !is State.Data) return@lazy emptyList()

            displayKeyMap.getTriggerError(triggerState.data).map { error ->
                when (error) {
                    KeyMapTriggerError.DND_ACCESS_DENIED -> TextListItem.Error(
                        id = error.toString(),
                        text = getString(R.string.trigger_error_dnd_access_denied),
                    )
                }
            }
        }

        fun build(): TriggerUiState = when (triggerState) {
            is State.Data -> loadedState(triggerState.data)
            is State.Loading -> loadingState()
        }

        private fun loadedState(trigger: KeyMapTrigger) = TriggerUiState(
            triggerKeyListItems = createListItems(trigger).createListState(),

            recordTriggerButtonText = recordTriggerButtonText,

            clickTypeRadioButtonsVisible = trigger.mode is TriggerMode.Parallel,

            shortPressButtonChecked =
            trigger.mode is TriggerMode.Parallel && trigger.mode.clickType == ClickType.SHORT_PRESS,
            longPressButtonChecked =
            trigger.mode is TriggerMode.Parallel && trigger.mode.clickType == ClickType.LONG_PRESS,

            triggerModeButtonsEnabled = trigger.keys.size > 1,
            parallelTriggerModeButtonChecked = trigger.mode is TriggerMode.Parallel,
            sequenceTriggerModeButtonChecked = trigger.mode is TriggerMode.Sequence,

            showSequenceTriggerExplanation = trigger.mode is TriggerMode.Sequence
                && !onboarding.shownSequenceTriggerExplanation,

            showParallelTriggerOrderExplanation = trigger.mode is TriggerMode.Parallel
                && trigger.keys.size > 1
                && !onboarding.shownParallelTriggerOrderExplanation,

            errorListItems = errorListItems
        )

        private fun loadingState() = TriggerUiState(
            triggerKeyListItems = ListUiState.Empty,
            recordTriggerButtonText = recordTriggerButtonText,

            clickTypeRadioButtonsVisible = false,

            shortPressButtonChecked = false,
            longPressButtonChecked = false,

            triggerModeButtonsEnabled = false,

            parallelTriggerModeButtonChecked = false,
            sequenceTriggerModeButtonChecked = false,

            showSequenceTriggerExplanation = false,
            showParallelTriggerOrderExplanation = false,

            errorListItems = errorListItems
        )
    }
}

data class TriggerUiState(
    val triggerKeyListItems: ListUiState<TriggerKeyListItem>,
    val recordTriggerButtonText: String,

    val clickTypeRadioButtonsVisible: Boolean,
    val shortPressButtonChecked: Boolean,
    val longPressButtonChecked: Boolean,

    val triggerModeButtonsEnabled: Boolean,
    val parallelTriggerModeButtonChecked: Boolean,
    val sequenceTriggerModeButtonChecked: Boolean,

    val showSequenceTriggerExplanation: Boolean,
    val showParallelTriggerOrderExplanation: Boolean,

    val errorListItems: List<TextListItem.Error>
) : ViewPopulated()