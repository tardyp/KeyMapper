package io.github.sds100.keymapper.ui.mappings.keymap

import android.view.KeyEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.hadilq.liveevent.LiveEvent
import io.github.sds100.keymapper.data.model.options.TriggerKeyOptions
import io.github.sds100.keymapper.domain.devices.ShowDeviceInfoUseCase
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.*
import io.github.sds100.keymapper.domain.usecases.OnboardingUseCase
import io.github.sds100.keymapper.domain.utils.ClickType
import io.github.sds100.keymapper.ui.fragment.keymap.ChooseTriggerKeyDeviceModel
import io.github.sds100.keymapper.ui.fragment.keymap.TriggerKeyListItemModel
import io.github.sds100.keymapper.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Created by sds100 on 24/11/20.
 */

class TriggerViewModel(
    private val coroutineScope: CoroutineScope,
    private val onboardingUseCase: OnboardingUseCase,
    private val useCase: ConfigKeymapTriggerUseCase,
    private val listItemMapper: TriggerKeyListItemMapper,
    private val recordTrigger: RecordTriggerUseCase,
    private val showDeviceInfoUseCase: ShowDeviceInfoUseCase
    ) {

    val optionsViewModel = TriggerOptionsViewModel(
        onboardingUseCase,
        useCase
    )

    private val _enableAccessibilityServicePrompt = LiveEvent<Unit>()
    val enableAccessibilityServicePrompt: LiveData<Unit> = _enableAccessibilityServicePrompt

    private val _showParallelTriggerOrderExplanation = MutableStateFlow(false)
    val showParallelTriggerOrderExplanation = _showParallelTriggerOrderExplanation.asStateFlow()

    fun approvedParallelTriggerOrderExplanation() {
        _showParallelTriggerOrderExplanation.value = false
        onboardingUseCase.shownParallelTriggerOrderExplanation = true
    }

    private val _showSequenceTriggerExplanation = MutableStateFlow(false)
    val showSequenceTriggerExplanation = _showSequenceTriggerExplanation.asStateFlow()

    private val _showEnableCapsLockKeyboardLayoutPrompt = MutableSharedFlow<Unit>()
    val showEnableCapsLockKeyboardLayoutPrompt =
        _showEnableCapsLockKeyboardLayoutPrompt.asSharedFlow()

    fun approvedSequenceTriggerExplanation() {
        _showSequenceTriggerExplanation.value = false
        onboardingUseCase.shownSequenceTriggerExplanation = true
    }

    private val dataState = MutableLiveData<ConfigKeymapTriggerState?>()

    //TODO hide UI elements if loading
    private val _viewState = MutableLiveData<ViewState>(ViewLoading())
    val viewState: LiveData<ViewState> = _viewState

    val triggerInParallel = dataState.map { it?.mode == TriggerMode.PARALLEL }
    fun setParallelTriggerMode() = useCase.setMode(TriggerMode.PARALLEL)

    val triggerInSequence = dataState.map { it?.mode == TriggerMode.SEQUENCE }
    fun setSequenceTriggerMode() = useCase.setMode(TriggerMode.SEQUENCE)

    val triggerModeUndefined = dataState.map { it?.mode == TriggerMode.UNDEFINED }
    fun setUndefinedTriggerMode() = useCase.setMode(TriggerMode.UNDEFINED)

    val isParallelTriggerClickTypeShortPress = dataState.map {
        it ?: return@map false

        if (!it.keys.isNullOrEmpty()) {
            it.keys[0].clickType == ClickType.SHORT_PRESS
        } else {
            false
        }
    }

    val isParallelTriggerClickTypeLongPress = dataState.map {
        it ?: return@map false

        if (!it.keys.isNullOrEmpty()) {
            it.keys[0].clickType == ClickType.LONG_PRESS
        } else {
            false
        }
    }

    private val _showChooseDeviceDialog = MutableSharedFlow<ChooseTriggerKeyDeviceModel>()
    val showChooseDeviceDialog = _showChooseDeviceDialog.asSharedFlow()

    private val _modelList = MutableLiveData<DataState<List<TriggerKeyListItemModel>>>()
    val modelList: LiveData<DataState<List<TriggerKeyListItemModel>>> = _modelList

    val triggerKeyCount = modelList.map {
        when (it) {
            is Data -> it.data.size
            else -> 0
        }
    }

    val recordTriggerTimeLeft = MutableLiveData(0)
    val recordingTrigger = MutableLiveData(false)

    /**
     * The number of times the user has attempted to record a trigger.
     */
    private var recordingTriggerCount = 0

    /**
     * Whether the user has successfully recorded a trigger.
     */
    private var successfullyRecordedTrigger = false

    init {

        useCase.state.onEach {
            if (it is Data) {
                dataState.value = it.data
                onStateChange(it.data)
                _viewState.value = ViewPopulated()
            } else {
                dataState.value = null
                _viewState.postValue(ViewLoading())
            }
        }.launchIn(coroutineScope)

        recordTrigger.onRecordKey.onEach {

            if (it.keyCode == KeyEvent.KEYCODE_CAPS_LOCK) {
                _showEnableCapsLockKeyboardLayoutPrompt.emit(Unit)
            }

            useCase.addTriggerKey(it.keyCode, it.device)
        }.launchIn(coroutineScope)
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

    fun recordTrigger() = recordTrigger.record()

    fun stopRecording() = recordTrigger.stopRecording()

    fun rebuildModels() = dataState.value?.let {
        rebuildModels(it.mode, it.keys)
    }

    private fun onStateChange(state: ConfigKeymapTriggerState) {
        /* when the user first chooses to make parallel a trigger,
           show a dialog informing them that the order in which they
           list the keys is the order in which they will need to be held down.
           */
        if (state.mode == TriggerMode.PARALLEL
            && state.keys.size > 1
            && !onboardingUseCase.shownParallelTriggerOrderExplanation
        ) {
            _showParallelTriggerOrderExplanation.value = true
        }

        if (state.mode == TriggerMode.SEQUENCE
            && state.keys.size > 1
            && !onboardingUseCase.shownSequenceTriggerExplanation
        ) {
            _showSequenceTriggerExplanation.value = true
        }

        rebuildModels(state.mode, state.keys)
    }

    private fun rebuildModels(mode: TriggerMode, keys: List<TriggerKey>) {
        coroutineScope.launch {

            _modelList.value = Loading()

            _modelList.value = with(listItemMapper.map(keys, mode)) {
                when {
                    isEmpty() -> Empty()
                    else -> Data(this)
                }
            }
        }
    }
}