package io.github.sds100.keymapper.ui.mappings.keymap

import android.os.Bundle
import androidx.lifecycle.*
import com.hadilq.liveevent.LiveEvent
import io.github.sds100.keymapper.data.model.ConstraintEntity
import io.github.sds100.keymapper.data.viewmodel.ActionListViewModel
import io.github.sds100.keymapper.data.viewmodel.ConstraintListViewModel
import io.github.sds100.keymapper.domain.actions.ConfigActionsUseCase
import io.github.sds100.keymapper.domain.actions.GetActionErrorUseCase
import io.github.sds100.keymapper.domain.actions.TestActionUseCase
import io.github.sds100.keymapper.domain.devices.ShowDeviceInfoUseCase
import io.github.sds100.keymapper.domain.mappings.keymap.*
import io.github.sds100.keymapper.domain.trigger.RecordTriggerUseCase
import io.github.sds100.keymapper.domain.usecases.OnboardingUseCase
import io.github.sds100.keymapper.ui.actions.ActionListItemMapper
import io.github.sds100.keymapper.ui.mappings.common.ConfigMappingViewModel
import io.github.sds100.keymapper.ui.utils.getJsonSerializable
import io.github.sds100.keymapper.ui.utils.putJsonSerializable
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.result.RecoverableError
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Created by sds100 on 22/11/20.
 */

class ConfigKeymapViewModel(
    private val save: SaveKeymapUseCase,
    private val get: GetKeymapUseCase,
    private val configUseCase: ConfigKeymapUseCase,
    configActions: ConfigActionsUseCase<KeymapAction>,
    configTrigger: ConfigKeymapTriggerUseCase,
    getActionError: GetActionErrorUseCase,
    testAction: TestActionUseCase,
    onboardingUseCase: OnboardingUseCase,
    recordTriggerUseCase: RecordTriggerUseCase,
    showDeviceInfoUseCase: ShowDeviceInfoUseCase,
    actionListItemMapper: ActionListItemMapper<KeymapAction>,
    triggerKeyListItemMapper: TriggerKeyListItemMapper,
) : ViewModel(), ConfigMappingViewModel {

    companion object {
        const val NEW_KEYMAP_ID = -1L

        private const val STATE_KEY = "config_keymap"
    }

    override val actionListViewModel = ActionListViewModel(
        viewModelScope,
        configActions,
        getActionError,
        testAction,
        actionListItemMapper
    )

    val triggerViewModel = TriggerViewModel(
        viewModelScope,
        onboardingUseCase,
        configTrigger,
        triggerKeyListItemMapper,
        recordTriggerUseCase,
        showDeviceInfoUseCase)

    private val dataState = MutableLiveData<ConfigKeymapState?>()

    //TODO hide UI elements if loading
    private val _viewState = MutableLiveData<ViewState>(ViewLoading())
    override val viewState: LiveData<ViewState> = _viewState

    private val supportedConstraints =
        ConstraintEntity.COMMON_SUPPORTED_CONSTRAINTS.toMutableList().apply {
            add(ConstraintEntity.SCREEN_ON)
            add(ConstraintEntity.SCREEN_OFF)
        }.toList()

    val constraintListViewModel = ConstraintListViewModel(viewModelScope, supportedConstraints)

    override val isEnabled = dataState.map { it?.isEnabled ?: false }
    override fun setEnabled(enabled: Boolean) = configUseCase.setEnabled(enabled)

    private val _fixError = LiveEvent<RecoverableError>().apply {
        addSource(actionListViewModel.fixError) {
            this.value = it
        }
    }
    override val fixError: LiveData<RecoverableError> = _fixError

    private val _enableAccessibilityServicePrompt = LiveEvent<Unit>().apply {
        addSource(actionListViewModel.enableAccessibilityServicePrompt) {
            this.value = it
        }

        addSource(triggerViewModel.enableAccessibilityServicePrompt) {
            this.value = it
        }
    }
    override val enableAccessibilityServicePrompt: LiveData<Unit> =
        _enableAccessibilityServicePrompt

    init {
        configUseCase.state.onEach {
            if (it is Data<ConfigKeymapState>) {
                dataState.value = it.data
                _viewState.value = ViewPopulated()
            } else {
                dataState.value = null
                _viewState.value = ViewLoading()
            }
        }.launchIn(viewModelScope)
    }

    override fun save() = configUseCase.getKeymap().ifIsData { save(it) }

    override fun saveState(outState: Bundle) {
        configUseCase.getKeymap().ifIsData {
            outState.putJsonSerializable(STATE_KEY, it)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun restoreState(state: Bundle) {
        state.getJsonSerializable<KeyMap>(STATE_KEY)?.let {
            configUseCase.setKeymap(it)
        } ?: configUseCase.loadBlankKeymap()
    }

    fun loadKeymap(id: Long) {
        viewModelScope.launch {
            when (id) {
                NEW_KEYMAP_ID -> configUseCase.loadBlankKeymap()
                else -> configUseCase.setKeymap(get(id))
            }
        }
    }

    class Factory(
        private val saveKeymap: SaveKeymapUseCase,
        private val getKeymap: GetKeymapUseCase,
        private val useCase: ConfigKeymapUseCase,
        private val configActionsUseCase: ConfigActionsUseCase<KeymapAction>,
        private val configTriggerUseCase: ConfigKeymapTriggerUseCase,
        private val getActionError: GetActionErrorUseCase,
        private val testAction: TestActionUseCase,
        private val onboardingUseCase: OnboardingUseCase,
        private val recordTriggerUseCase: RecordTriggerUseCase,
        private val showDeviceInfoUseCase: ShowDeviceInfoUseCase,
        private val actionListItemMapper: ActionListItemMapper<KeymapAction>,
        private val triggerKeyListItemMapper: TriggerKeyListItemMapper,
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) =
            ConfigKeymapViewModel(
                saveKeymap,
                getKeymap,
                useCase,
                configActionsUseCase,
                configTriggerUseCase,
                getActionError,
                testAction,
                onboardingUseCase,
                recordTriggerUseCase,
                showDeviceInfoUseCase,
                actionListItemMapper,
                triggerKeyListItemMapper
            ) as T
    }
}