package io.github.sds100.keymapper.ui.mappings.keymap

import android.Manifest
import android.os.Build
import android.view.KeyEvent
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.data.model.options.TriggerKeyOptions
import io.github.sds100.keymapper.domain.devices.ShowDeviceInfoUseCase
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.*
import io.github.sds100.keymapper.domain.permissions.IsDoNotDisturbAccessGrantedUseCase
import io.github.sds100.keymapper.domain.usecases.OnboardingUseCase
import io.github.sds100.keymapper.domain.utils.ClickType
import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.ui.*
import io.github.sds100.keymapper.ui.fragment.keymap.ChooseTriggerKeyDeviceModel
import io.github.sds100.keymapper.ui.fragment.keymap.TriggerKeyListItem
import io.github.sds100.keymapper.ui.shortcuts.IsRequestShortcutSupported
import io.github.sds100.keymapper.util.ViewPopulated
import io.github.sds100.keymapper.util.requiresDndAccessToImitate
import io.github.sds100.keymapper.util.result.RecoverableError
import io.github.sds100.keymapper.util.result.onFailure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Created by sds100 on 24/11/20.
 */

class TriggerViewModel(
    private val coroutineScope: CoroutineScope,
    private val onboardingUseCase: OnboardingUseCase,
    private val useCase: ConfigKeymapTriggerUseCase,
    private val listItemMapper: TriggerKeyListItemMapper,
    private val recordTrigger: RecordTriggerUseCase,
    private val showDeviceInfoUseCase: ShowDeviceInfoUseCase,
    private val areShortcutsSupported: IsRequestShortcutSupported,
    private val isDndAccessGranted: IsDoNotDisturbAccessGrantedUseCase,
    resourceProvider: ResourceProvider
) : ResourceProvider by resourceProvider {

    private companion object {
        const val ID_DND_ACCESS_ERROR = "id_dnd_access_status"
    }

    val optionsViewModel = TriggerOptionsViewModel(
        coroutineScope,
        onboardingUseCase,
        useCase,
        areShortcutsSupported,
        resourceProvider,
    )

    private val _enableAccessibilityServicePrompt = MutableSharedFlow<Unit>()
    val enableAccessibilityServicePrompt = _enableAccessibilityServicePrompt.asSharedFlow()

    //TODO dialog that prompts the user to restart the accessibility service if failing to record a trigger too many times
    //TODO dialogs
    private val _showEnableCapsLockKeyboardLayoutPrompt = MutableSharedFlow<Unit>()
    val showEnableCapsLockKeyboardLayoutPrompt =
        _showEnableCapsLockKeyboardLayoutPrompt.asSharedFlow()

    fun approvedParallelTriggerOrderExplanation() {
        onboardingUseCase.shownParallelTriggerOrderExplanation = true
    }

    fun approvedSequenceTriggerExplanation() {
        onboardingUseCase.shownSequenceTriggerExplanation = true
    }

    private val _showChooseDeviceDialog = MutableSharedFlow<ChooseTriggerKeyDeviceModel>()
    val showChooseDeviceDialog = _showChooseDeviceDialog.asSharedFlow()

    private val _fixError = MutableSharedFlow<RecoverableError>()
    val fixError = _fixError.asSharedFlow()

    private val _state = MutableStateFlow(
        UiBuilder(State.Loading, RecordTriggerState.Stopped).build()
    )
    val state = _state.asStateFlow()

    private val rebuildUiState = MutableSharedFlow<Unit>()

    init {
        recordTrigger.onRecordKey.onEach {

            if (it.keyCode == KeyEvent.KEYCODE_CAPS_LOCK) {
                _showEnableCapsLockKeyboardLayoutPrompt.emit(Unit)
            }

            useCase.addTriggerKey(it.keyCode, it.device)
        }.launchIn(coroutineScope)

        //TODO dialogs
        coroutineScope.launch {
            combine(
                rebuildUiState,
                useCase.state,
                recordTrigger.state
            ) { _, configState, recordTriggerState ->
                UiBuilder(configState, recordTriggerState)
            }.collectLatest {
                _state.value = it.build()
            }
        }
    }

    fun setParallelTriggerModeChecked(checked: Boolean) {
        if (checked) {
            useCase.setParallelTriggerMode()
        }
    }

    fun setSequenceTriggerModeChecked(checked: Boolean) {
        if (checked) {
            useCase.setSequenceTriggerMode()
        }
    }

    fun setShortPressButtonChecked(checked: Boolean) {
        if (checked) {
            useCase.setParallelTriggerShortPress()
        }
    }

    fun setLongPressButtonChecked(checked: Boolean) {
        if (checked) {
            useCase.setParallelTriggerLongPress()
        }
    }

    fun setTriggerKeyDevice(uid: String, device: TriggerKeyDevice) =
        useCase.setTriggerKeyDevice(uid, device)

    fun onRemoveKeyClick(uid: String) = useCase.removeTriggerKey(uid)
    fun onMoveTriggerKey(fromIndex: Int, toIndex: Int) = useCase.moveTriggerKey(fromIndex, toIndex)

    fun onTriggerKeyOptionsClick(id: String) {
//        TODO()
    }

    fun setTriggerKeyOptions(options: TriggerKeyOptions) {
//        TODO()
    }

    fun onChooseDeviceClick(keyUid: String) {
        coroutineScope.launch {
            val externalDevices = showDeviceInfoUseCase.getAll().map {
                TriggerKeyDevice.External(it.descriptor, it.name)
            }

            val devices = sequence {
                yield(TriggerKeyDevice.Internal)
                yield(TriggerKeyDevice.Any)
                yieldAll(externalDevices)
            }.toList()

            _showChooseDeviceDialog.emit(ChooseTriggerKeyDeviceModel(keyUid, devices))
        }
    }

    fun onRecordTriggerButtonClick() {
        when (recordTrigger.state.value) {
            is RecordTriggerState.CountingDown -> recordTrigger.stopRecording()
            RecordTriggerState.Stopped -> recordTrigger.startRecording().onFailure {
                runBlocking { _enableAccessibilityServicePrompt.emit(Unit) }
            }
        }
    }

    fun stopRecordingTrigger() = recordTrigger.stopRecording()

    fun rebuildUiState() {
        runBlocking { rebuildUiState.emit(Unit) }
    }

    fun fixError(listItemId: String) {
        coroutineScope.launch {
            when (listItemId) {
                ID_DND_ACCESS_ERROR -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val error =
                        RecoverableError.PermissionDenied(Manifest.permission.ACCESS_NOTIFICATION_POLICY)
                    _fixError.emit(error)
                }
            }
        }
    }

    /**
     * Use this object to create the ui state instead of a function because it allows one to combine
     * multiple flows and then only finish collecting the *latest* combination of those flows.
     */
    private inner class UiBuilder(
        private val configState: State<ConfigKeymapTriggerState>,
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
            sequence {
                if (configState !is State.Data) return@sequence

                if (configState.data.keys.any { it.requiresDndAccessToImitate }) {
                    if (!isDndAccessGranted()) {
                        yield(
                            TextListItem.Error(
                                id = ID_DND_ACCESS_ERROR,
                                text = getString(R.string.trigger_error_dnd_access_denied),
                            )
                        )
                    }
                }
            }.toList()
        }

        fun build(): TriggerUiState = when (configState) {
            is State.Data -> loadedState(configState.data)
            is State.Loading -> loadingState()
        }

        private fun loadedState(config: ConfigKeymapTriggerState) = TriggerUiState(
            triggerKeyListItems =
            listItemMapper.map(config.keys, config.mode).createListState(),

            recordTriggerButtonText = recordTriggerButtonText,

            clickTypeRadioButtonsVisible = config.mode is TriggerMode.Parallel,

            shortPressButtonChecked =
            config.mode is TriggerMode.Parallel && config.mode.clickType == ClickType.SHORT_PRESS,
            longPressButtonChecked =
            config.mode is TriggerMode.Parallel && config.mode.clickType == ClickType.LONG_PRESS,

            triggerModeButtonsEnabled = config.keys.size > 1,
            parallelTriggerModeButtonChecked = config.mode is TriggerMode.Parallel,
            sequenceTriggerModeButtonChecked = config.mode is TriggerMode.Sequence,

            showSequenceTriggerExplanation = config.mode is TriggerMode.Sequence
                && !onboardingUseCase.shownSequenceTriggerExplanation,

            showParallelTriggerOrderExplanation = config.mode is TriggerMode.Parallel
                && config.keys.size > 1
                && !onboardingUseCase.shownParallelTriggerOrderExplanation,

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