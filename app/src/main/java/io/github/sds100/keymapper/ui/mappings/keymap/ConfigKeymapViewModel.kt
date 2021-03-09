package io.github.sds100.keymapper.ui.mappings.keymap

import android.os.Bundle
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.lifecycle.*
import com.hadilq.liveevent.LiveEvent
import io.github.sds100.keymapper.data.model.ConstraintEntity
import io.github.sds100.keymapper.data.viewmodel.ActionListViewModel
import io.github.sds100.keymapper.data.viewmodel.ConstraintListViewModel
import io.github.sds100.keymapper.domain.actions.ConfigActionsUseCase
import io.github.sds100.keymapper.domain.actions.GetActionErrorUseCase
import io.github.sds100.keymapper.domain.actions.TestActionUseCase
import io.github.sds100.keymapper.domain.devices.ShowDeviceInfoUseCase
import io.github.sds100.keymapper.domain.mappings.keymap.ConfigKeymapTriggerUseCase
import io.github.sds100.keymapper.domain.mappings.keymap.ConfigKeymapUseCase
import io.github.sds100.keymapper.domain.mappings.keymap.GetKeymapUseCase
import io.github.sds100.keymapper.domain.mappings.keymap.SaveKeymapUseCase
import io.github.sds100.keymapper.domain.models.KeyMap
import io.github.sds100.keymapper.domain.models.KeymapActionOptions
import io.github.sds100.keymapper.domain.trigger.RecordTriggerUseCase
import io.github.sds100.keymapper.domain.usecases.OnboardingUseCase
import io.github.sds100.keymapper.ui.actions.ActionListItemMapper
import io.github.sds100.keymapper.ui.mappings.common.ConfigMappingViewModel
import io.github.sds100.keymapper.util.ifIsData
import io.github.sds100.keymapper.util.result.RecoverableError
import kotlinx.coroutines.launch

/**
 * Created by sds100 on 22/11/20.
 */

class ConfigKeymapViewModel(
    private val saveKeymapUseCase: SaveKeymapUseCase,
    private val getKeymap: GetKeymapUseCase,
    private val useCase: ConfigKeymapUseCase,
    configActionsUseCase: ConfigActionsUseCase<KeymapActionOptions>,
    configTriggerUseCase: ConfigKeymapTriggerUseCase,
    getActionError: GetActionErrorUseCase,
    testAction: TestActionUseCase,
    onboardingUseCase: OnboardingUseCase,
    recordTriggerUseCase: RecordTriggerUseCase,
    showDeviceInfoUseCase: ShowDeviceInfoUseCase,
    actionListItemMapper: ActionListItemMapper<KeymapActionOptions>,
    triggerKeyListItemMapper: TriggerKeyListItemMapper,
) : ViewModel(), ConfigMappingViewModel, BaseObservable() {
    companion object {
        const val NEW_KEYMAP_ID = -1L

        private const val STATE_KEY = "config_keymap"
    }

    override val actionListViewModel = ActionListViewModel(
        viewModelScope,
        configActionsUseCase,
        getActionError,
        testAction,
        actionListItemMapper
    )

    val triggerViewModel = TriggerViewModel(
        viewModelScope,
        onboardingUseCase,
        configTriggerUseCase,
        triggerKeyListItemMapper,
        recordTriggerUseCase,
        showDeviceInfoUseCase,
        useCase
    )

    private val supportedConstraints =
        ConstraintEntity.COMMON_SUPPORTED_CONSTRAINTS.toMutableList().apply {
            add(ConstraintEntity.SCREEN_ON)
            add(ConstraintEntity.SCREEN_OFF)
        }.toList()

    val constraintListViewModel = ConstraintListViewModel(viewModelScope, supportedConstraints)

    override val isEnabled = MutableLiveData(useCase.isEnabled.value)

    var enabled: Boolean = true
        @Bindable get() = useCase.isEnabled.value
        set(value) {
        notifyProper
        }

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

    override fun save() = useCase.save()

    override fun saveState(outState: Bundle) {
        useCase.getKeymap().ifIsData {
            outState.putParcelable(STATE_KEY, it)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun restoreState(state: Bundle) {
        state.getParcelable<KeyMap>(STATE_KEY)?.let {
            useCase.setKeymap(it)
        } ?: useCase.loadBlankKeymap()
    }

    fun loadKeymap(id: Long) {
        viewModelScope.launch {
            when (id) {
                NEW_KEYMAP_ID -> useCase.loadBlankKeymap()
                else -> useCase.setKeymap(getKeymap(id))
            }
        }
    }

    class Factory(
        private val useCase: ConfigKeymapUseCase,
        private val configActionsUseCase: ConfigActionsUseCase<KeymapActionOptions>,
        private val configTriggerUseCase: ConfigKeymapTriggerUseCase,
        private val getActionError: GetActionErrorUseCase,
        private val testAction: TestActionUseCase,
        private val onboardingUseCase: OnboardingUseCase,
        private val actionListItemMapper: ActionListItemMapper<KeymapActionOptions>,
        private val triggerKeyListItemMapper: TriggerKeyListItemMapper,
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) =
            ConfigKeymapViewModel(
                useCase,
                configActionsUseCase,
                configTriggerUseCase,
                getActionError,
                testAction,
                onboardingUseCase,
                actionListItemMapper,
                triggerKeyListItemMapper
            ) as T
    }
}