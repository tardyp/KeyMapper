package io.github.sds100.keymapper.ui.mappings.keymap

import android.view.KeyEvent
import androidx.lifecycle.LiveData
import com.hadilq.liveevent.LiveEvent
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.data.model.options.TriggerKeyOptions
import io.github.sds100.keymapper.domain.devices.ShowDeviceInfoUseCase
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.*
import io.github.sds100.keymapper.domain.usecases.OnboardingUseCase
import io.github.sds100.keymapper.domain.utils.ClickType
import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.ui.ListState
import io.github.sds100.keymapper.ui.UiStateProducer
import io.github.sds100.keymapper.ui.createListState
import io.github.sds100.keymapper.ui.fragment.keymap.ChooseTriggerKeyDeviceModel
import io.github.sds100.keymapper.ui.fragment.keymap.TriggerKeyListItemModel
import io.github.sds100.keymapper.util.ViewPopulated
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
    resourceProvider: ResourceProvider
) : ResourceProvider by resourceProvider, UiStateProducer<TriggerUiState> {

    val optionsViewModel = TriggerOptionsViewModel(
        onboardingUseCase,
        useCase
    )

    private val _enableAccessibilityServicePrompt = LiveEvent<Unit>()
    val enableAccessibilityServicePrompt: LiveData<Unit> = _enableAccessibilityServicePrompt

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

    override val state = MutableStateFlow(
        UiBuilder(State.Loading(), RecordTriggerState.Stopped).build()
    )

    fun setParallelTriggerMode() = useCase.setParallelTriggerMode()
    fun setSequenceTriggerMode() = useCase.setSequenceTriggerMode()
    fun setUndefinedTriggerMode() = useCase.setUndefinedTriggerMode()

    private val rebuildUiState = MutableSharedFlow<Unit>()

    init {
        recordTrigger.onRecordKey.onEach {

            if (it.keyCode == KeyEvent.KEYCODE_CAPS_LOCK) {
                _showEnableCapsLockKeyboardLayoutPrompt.emit(Unit)
            }

            useCase.addTriggerKey(it.keyCode, it.device)
        }.launchIn(coroutineScope)

        coroutineScope.launch {
            combine(
                rebuildUiState,
                useCase.state,
                recordTrigger.state
            ) { _, configState, recordTriggerState ->
                UiBuilder(configState, recordTriggerState)
            }.collectLatest {
                state.value = it.build()
            }
        }
    }

    fun setParallelTriggerClickType(clickType: ClickType) =
        useCase.setParallelTriggerClickType(clickType)

    fun setTriggerKeyDevice(uid: String, device: TriggerKeyDevice) =
        useCase.setTriggerKeyDevice(uid, device)

    fun onRemoveKeyClick(uid: String) = useCase.removeTriggerKey(uid)
    fun onMoveTriggerKey(fromIndex: Int, toIndex: Int) = useCase.moveTriggerKey(fromIndex, toIndex)

    fun onTriggerKeyOptionsClick(id: String) {
        TODO()
    }

    fun setTriggerKeyOptions(options: TriggerKeyOptions) {
        TODO()
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
                _enableAccessibilityServicePrompt.value = Unit
            }
        }
    }

    fun stopRecordingTrigger() = recordTrigger.stopRecording()

    override fun rebuildUiState() {
        runBlocking { rebuildUiState.emit(Unit) }
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

        fun build(): TriggerUiState = when (configState) {
            is State.Data -> loadedState(configState.data)
            is State.Loading -> loadingState()
        }

        private fun loadedState(config: ConfigKeymapTriggerState) = TriggerUiState(
            triggerKeyListModels =
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
                && !onboardingUseCase.shownParallelTriggerOrderExplanation
        )

        private fun loadingState() = TriggerUiState(
            triggerKeyListModels = ListState.Empty(),
            recordTriggerButtonText = recordTriggerButtonText,

            clickTypeRadioButtonsVisible = false,

            shortPressButtonChecked = false,
            longPressButtonChecked = false,

            triggerModeButtonsEnabled = false,

            parallelTriggerModeButtonChecked = false,
            sequenceTriggerModeButtonChecked = false,

            showSequenceTriggerExplanation = false,
            showParallelTriggerOrderExplanation = false
        )
    }
}

data class TriggerUiState(
    val triggerKeyListModels: ListState<TriggerKeyListItemModel>,
    val recordTriggerButtonText: String,

    val clickTypeRadioButtonsVisible: Boolean,
    val shortPressButtonChecked: Boolean,
    val longPressButtonChecked: Boolean,

    val triggerModeButtonsEnabled: Boolean,
    val parallelTriggerModeButtonChecked: Boolean,
    val sequenceTriggerModeButtonChecked: Boolean,

    val showSequenceTriggerExplanation: Boolean,
    val showParallelTriggerOrderExplanation: Boolean
) : ViewPopulated()