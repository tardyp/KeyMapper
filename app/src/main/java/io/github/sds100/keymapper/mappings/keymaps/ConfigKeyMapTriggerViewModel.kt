package io.github.sds100.keymapper.mappings.keymaps

import android.os.Build
import android.view.KeyEvent
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.onboarding.OnboardingUseCase
import io.github.sds100.keymapper.mappings.ClickType
import io.github.sds100.keymapper.util.ui.ResourceProvider
import io.github.sds100.keymapper.mappings.keymaps.trigger.KeyMapTriggerError
import io.github.sds100.keymapper.mappings.keymaps.trigger.*
import io.github.sds100.keymapper.system.permissions.Permission
import io.github.sds100.keymapper.ui.*
import io.github.sds100.keymapper.util.ui.PopupUi
import io.github.sds100.keymapper.mappings.keymaps.trigger.TriggerKeyListItem
import io.github.sds100.keymapper.system.keyevents.KeyEventUtils
import io.github.sds100.keymapper.util.State
import io.github.sds100.keymapper.util.ViewPopulated
import io.github.sds100.keymapper.util.mapData
import io.github.sds100.keymapper.util.result.FixableError
import io.github.sds100.keymapper.util.ui.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Created by sds100 on 24/11/20.
 */

//TODO rename as ConfigKeymapTriggerViewModel
class ConfigKeyMapTriggerViewModel(
    private val coroutineScope: CoroutineScope,
    private val onboarding: OnboardingUseCase,
    private val config: ConfigKeyMapUseCase,
    private val recordTrigger: RecordTriggerUseCase,
    private val createKeyMapShortcut: CreateKeyMapShortcutUseCase,
    private val displayKeyMap: DisplayKeyMapUseCase,
    resourceProvider: ResourceProvider
) : ResourceProvider by resourceProvider, PopupViewModel by PopupViewModelImpl() {

    private companion object {
        const val KEY_ENABLE_ACCESSIBILITY_SERVICE_DIALOG = "enable_accessibility_service"
    }

    val optionsViewModel = ConfigKeyMapTriggerOptionsViewModel(
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

    private val _openEditOptions = MutableSharedFlow<String>()

    /**
     * value is the uid of the action
     */
    val openEditOptions = _openEditOptions.asSharedFlow()

    private val _state = MutableStateFlow(
        UiBuilder(State.Loading, RecordTriggerState.Stopped).build()
    )
    val state = _state.asStateFlow()

    init {
        val rebuildUiState = MutableSharedFlow<UiBuilder>()

        coroutineScope.launch {
            rebuildUiState.collectLatest { uiBuilder ->
                _state.value = uiBuilder.build()
            }
        }

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
                rebuildUiState.emit(it)
            }
        }

        coroutineScope.launch {
            displayKeyMap.invalidateErrors.collectLatest {
                val configState = config.mapping.firstOrNull() ?: return@collectLatest
                val recordTriggerState = recordTrigger.state.firstOrNull() ?: return@collectLatest

                rebuildUiState.emit(
                    UiBuilder(configState.mapData { it.trigger }, recordTriggerState)
                )
            }
        }
    }

    fun onClickTypeRadioButtonCheckedChange(buttonId: Int) {
        when (buttonId) {
            R.id.radioButtonShortPress -> config.setTriggerShortPress()
            R.id.radioButtonLongPress -> config.setTriggerLongPress()
            R.id.radioButtonDoublePress -> config.setTriggerDoublePress()
        }
    }

    fun onTriggerModeRadioButtonCheckedChange(buttonId: Int) {
        when (buttonId) {
            R.id.radioButtonParallel -> config.setParallelTriggerMode()
            R.id.radioButtonSequence -> config.setSequenceTriggerMode()
        }
    }

    fun onRemoveKeyClick(uid: String) = config.removeTriggerKey(uid)
    fun onMoveTriggerKey(fromIndex: Int, toIndex: Int) = config.moveTriggerKey(fromIndex, toIndex)

    fun onTriggerKeyOptionsClick(id: String) {
        runBlocking { _openEditOptions.emit(id) }
    }

    fun onChooseDeviceClick(keyUid: String) {
        coroutineScope.launch {
            val idAny = "any"
            val idInternal = "this_device"
            val devices = config.getAvailableTriggerKeyDevices()

            val listItems = devices.map {
                when (it) {
                    TriggerKeyDevice.Any -> idAny to getString(R.string.any_device)
                    is TriggerKeyDevice.External -> it.descriptor to it.name
                    TriggerKeyDevice.Internal -> idInternal to getString(R.string.this_device)
                }
            }

            val response = showPopup(
                "pick_trigger_key_device",
                PopupUi.SingleChoice(listItems)
            ) ?: return@launch

            val selectedTriggerKeyDevice = when (response.item) {
                idAny -> TriggerKeyDevice.Any
                idInternal -> TriggerKeyDevice.Internal
                else -> devices.single { it is TriggerKeyDevice.External && it.descriptor == response.item }
            }

            config.setTriggerKeyDevice(keyUid, selectedTriggerKeyDevice)
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

                        val snackBar = PopupUi.SnackBar(
                            title = getString(R.string.dialog_message_enable_accessibility_service_to_record_trigger),
                            actionText = getString(R.string.pos_turn_on)
                        )

                        val response =
                            showPopup(KEY_ENABLE_ACCESSIBILITY_SERVICE_DIALOG, snackBar)

                        if (response != null) {
                            displayKeyMap.fixError(FixableError.AccessibilityServiceDisabled)
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
                    displayKeyMap.fixError(error)
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

            displayKeyMap.getTriggerErrors(triggerState.data).map { error ->
                when (error) {
                    KeyMapTriggerError.DND_ACCESS_DENIED -> TextListItem.Error(
                        id = error.toString(),
                        text = getString(R.string.trigger_error_dnd_access_denied),
                    )
                }
            }
        }

        val checkedClickTypeRadioButton: Int by lazy {
            if (triggerState !is State.Data) return@lazy R.id.radioButtonShortPress

            val clickType: ClickType? = when {
                triggerState.data.mode is TriggerMode.Parallel -> triggerState.data.mode.clickType
                triggerState.data.keys.size == 1 -> triggerState.data.keys[0].clickType
                else -> null
            }

            when (clickType) {
                ClickType.SHORT_PRESS -> R.id.radioButtonShortPress
                ClickType.LONG_PRESS -> R.id.radioButtonLongPress
                ClickType.DOUBLE_PRESS -> R.id.radioButtonDoublePress
                null -> R.id.radioButtonShortPress
            }
        }

        val checkedTriggerModeRadioButton: Int by lazy {
            if (triggerState !is State.Data) return@lazy R.id.radioButtonUndefined

            when (triggerState.data.mode) {
                is TriggerMode.Parallel -> R.id.radioButtonParallel
                TriggerMode.Sequence -> R.id.radioButtonSequence
                TriggerMode.Undefined -> R.id.radioButtonUndefined
            }
        }

        fun build(): KeyMapTriggerUiState = when (triggerState) {
            is State.Data -> loadedState(triggerState.data)
            is State.Loading -> loadingState()
        }

        private fun loadedState(trigger: KeyMapTrigger) = KeyMapTriggerUiState(
            triggerKeyListItems = createListItems(trigger).createListState(),

            recordTriggerButtonText = recordTriggerButtonText,

            clickTypeRadioButtonsVisible = trigger.mode is TriggerMode.Parallel || trigger.keys.size == 1,
            checkedClickTypeRadioButton = checkedClickTypeRadioButton,

            doublePressButtonVisible = trigger.keys.size == 1,

            triggerModeButtonsEnabled = trigger.keys.size > 1,

            checkedTriggerModeRadioButton = checkedTriggerModeRadioButton,

            showSequenceTriggerExplanation = trigger.mode is TriggerMode.Sequence
                && !onboarding.shownSequenceTriggerExplanation,

            showParallelTriggerOrderExplanation = trigger.mode is TriggerMode.Parallel
                && trigger.keys.size > 1
                && !onboarding.shownParallelTriggerOrderExplanation,

            errorListItems = errorListItems
        )

        private fun loadingState() = KeyMapTriggerUiState(
            triggerKeyListItems = ListUiState.Empty,
            recordTriggerButtonText = recordTriggerButtonText,

            clickTypeRadioButtonsVisible = false,

            checkedClickTypeRadioButton = R.id.radioButtonShortPress,
            doublePressButtonVisible = false,

            triggerModeButtonsEnabled = false,

            checkedTriggerModeRadioButton = R.id.radioButtonUndefined,

            showSequenceTriggerExplanation = false,
            showParallelTriggerOrderExplanation = false,

            errorListItems = errorListItems
        )
    }
}

data class KeyMapTriggerUiState(
    val triggerKeyListItems: ListUiState<TriggerKeyListItem>,
    val recordTriggerButtonText: String,

    val clickTypeRadioButtonsVisible: Boolean,
    val checkedClickTypeRadioButton: Int,
    val doublePressButtonVisible: Boolean,

    val triggerModeButtonsEnabled: Boolean,
    val checkedTriggerModeRadioButton: Int,

    val showSequenceTriggerExplanation: Boolean,
    val showParallelTriggerOrderExplanation: Boolean,

    val errorListItems: List<TextListItem.Error>
) : ViewPopulated()