package io.github.sds100.keymapper.ui.mappings.keymap

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.data.viewmodel.ActionListViewModel
import io.github.sds100.keymapper.data.viewmodel.ConstraintListViewModel
import io.github.sds100.keymapper.domain.actions.ActionData
import io.github.sds100.keymapper.domain.actions.ConfigActionsUseCase
import io.github.sds100.keymapper.domain.actions.GetActionErrorUseCase
import io.github.sds100.keymapper.domain.actions.TestActionUseCase
import io.github.sds100.keymapper.domain.constraints.ConfigConstraintsUseCase
import io.github.sds100.keymapper.domain.constraints.GetConstraintErrorUseCase
import io.github.sds100.keymapper.domain.devices.ShowDeviceInfoUseCase
import io.github.sds100.keymapper.domain.mappings.keymap.*
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.ConfigKeymapTriggerUseCase
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.RecordTriggerUseCase
import io.github.sds100.keymapper.domain.usecases.OnboardingUseCase
import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.domain.utils.ifIsData
import io.github.sds100.keymapper.domain.utils.mapData
import io.github.sds100.keymapper.framework.adapters.LauncherShortcutAdapter
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.service.MyAccessibilityService
import io.github.sds100.keymapper.ui.actions.ActionUiHelper
import io.github.sds100.keymapper.ui.constraints.ConstraintUiHelper
import io.github.sds100.keymapper.ui.mappings.common.ConfigMappingUiState
import io.github.sds100.keymapper.ui.mappings.common.ConfigMappingViewModel
import io.github.sds100.keymapper.ui.utils.getJsonSerializable
import io.github.sds100.keymapper.ui.utils.putJsonSerializable
import io.github.sds100.keymapper.util.result.RecoverableError
import io.github.sds100.keymapper.util.result.onSuccess
import io.github.sds100.keymapper.util.result.valueOrNull
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Created by sds100 on 22/11/20.
 */

class ConfigKeymapViewModel(
    private val save: SaveKeymapUseCase,
    private val get: GetKeymapUseCase,
    private val configUseCase: ConfigKeymapUseCase,
    private val configActions: ConfigActionsUseCase<KeymapAction>,
    configTrigger: ConfigKeymapTriggerUseCase,
    configConstraints: ConfigConstraintsUseCase,
    getActionError: GetActionErrorUseCase,
    getConstraintError: GetConstraintErrorUseCase,
    testAction: TestActionUseCase,
    onboardingUseCase: OnboardingUseCase,
    recordTriggerUseCase: RecordTriggerUseCase,
    showDeviceInfoUseCase: ShowDeviceInfoUseCase,
    private val actionUiHelper: ActionUiHelper<KeymapAction>,
    constraintUiHelper: ConstraintUiHelper,
    private val launcherShortcutAdapter: LauncherShortcutAdapter,
    resourceProvider: ResourceProvider
) : ViewModel(), ConfigMappingViewModel, ResourceProvider by resourceProvider {

    companion object {
        const val NEW_KEYMAP_ID = -1L

        private const val STATE_KEY = "config_keymap"
    }

    val actionListViewModel = ActionListViewModel(
        viewModelScope,
        configActions,
        getActionError,
        testAction,
        actionUiHelper,
        resourceProvider
    )

    val triggerViewModel = TriggerViewModel(
        viewModelScope,
        onboardingUseCase,
        configTrigger,
        TriggerKeyListItemMapper(resourceProvider),
        recordTriggerUseCase,
        showDeviceInfoUseCase,
        launcherShortcutAdapter,
        resourceProvider
    )

    val constraintListViewModel = ConstraintListViewModel(
        viewModelScope,
        configConstraints,
        constraintUiHelper,
        getConstraintError,
        resourceProvider
    )

    private val _createLauncherShortcutLabel = MutableSharedFlow<Unit>()
    val createLauncherShortcutLabel = _createLauncherShortcutLabel.asSharedFlow()

    override val state = MutableStateFlow<ConfigMappingUiState>(buildUiState(State.Loading()))

    override fun setEnabled(enabled: Boolean) = configUseCase.setEnabled(enabled)

    override val fixError = MutableSharedFlow<RecoverableError>()
    override val enableAccessibilityServicePrompt = MutableSharedFlow<Unit>()

    private val rebuildUiState = MutableSharedFlow<Unit>()

    init {
        viewModelScope.launch {
            combine(rebuildUiState, configUseCase.state) { _, configState ->
                buildUiState(configState)
            }.collectLatest {
                state.value = it
            }
        }

        viewModelScope.launch {
            merge(actionListViewModel.fixError, constraintListViewModel.fixError).collectLatest {
                fixError.emit(it)
            }
        }

        viewModelScope.launch {
            merge(
                actionListViewModel.enableAccessibilityServicePrompt,
                triggerViewModel.enableAccessibilityServicePrompt
            ).collectLatest {
                enableAccessibilityServicePrompt.emit(it)
            }
        }

        runBlocking { rebuildUiState.emit(Unit) } //build the initial state on init
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

    fun createLauncherShortcut() {
        viewModelScope.launch {
            configActions.actionList.firstOrNull()?.ifIsData { actionList ->
                if (actionList.size == 1) {
                    actionUiHelper.getTitle(actionList[0].data).onSuccess {
                        createLauncherShortcut(it)
                    }
                } else {
                    _createLauncherShortcutLabel.emit(Unit)
                }
            }
        }
    }

    fun createLauncherShortcut(label: String) {
        if (!launcherShortcutAdapter.isSupported) return

        viewModelScope.launch {
            val keymapUid = configUseCase.state.firstOrNull()?.mapData { it.uid }

            if (keymapUid !is State.Data) return@launch

            configActions.actionList.firstOrNull()?.ifIsData { actionList ->
                val icon = if (actionList.size == 1) {
                    actionUiHelper.getIcon(actionList[0].data).valueOrNull()?.drawable
                        ?: getDrawable(R.mipmap.ic_launcher_round)
                } else {
                    getDrawable(R.mipmap.ic_launcher_round)
                }

                val intent = Intent().apply {
                    action = MyAccessibilityService.ACTION_TRIGGER_KEYMAP_BY_UID
                    putExtra(MyAccessibilityService.EXTRA_KEYMAP_UID, keymapUid.data)
                }

                launcherShortcutAdapter.create(
                    icon = icon,
                    label = label,
                    intent = intent
                )
            }
        }
    }

    override fun rebuildUiState() {
        runBlocking { rebuildUiState.emit(Unit) }
        actionListViewModel.rebuildUiState()
        constraintListViewModel.rebuildUiState()
        triggerViewModel.rebuildUiState()
    }

    override fun addAction(actionData: ActionData) = actionListViewModel.addAction(actionData)

    private fun buildUiState(configState: State<ConfigKeymapState>): ConfigKeymapUiState {
        return when (configState) {
            is State.Data -> ConfigKeymapUiState(configState.data.isEnabled)
            is State.Loading -> ConfigKeymapUiState(isEnabled = false)
        }
    }

    class Factory(
        private val saveKeymap: SaveKeymapUseCase,
        private val getKeymap: GetKeymapUseCase,
        private val useCase: ConfigKeymapUseCase,
        private val configActionsUseCase: ConfigActionsUseCase<KeymapAction>,
        private val configTriggerUseCase: ConfigKeymapTriggerUseCase,
        private val configConstraints: ConfigConstraintsUseCase,
        private val getActionError: GetActionErrorUseCase,
        private val getConstraintError: GetConstraintErrorUseCase,
        private val testAction: TestActionUseCase,
        private val onboardingUseCase: OnboardingUseCase,
        private val recordTriggerUseCase: RecordTriggerUseCase,
        private val showDeviceInfoUseCase: ShowDeviceInfoUseCase,
        private val actionUiHelper: ActionUiHelper<KeymapAction>,
        private val constraintUiHelper: ConstraintUiHelper,
        private val launcherShortcutAdapter: LauncherShortcutAdapter,
        private val resourceProvider: ResourceProvider
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) =
            ConfigKeymapViewModel(
                saveKeymap,
                getKeymap,
                useCase,
                configActionsUseCase,
                configTriggerUseCase,
                configConstraints,
                getActionError,
                getConstraintError,
                testAction,
                onboardingUseCase,
                recordTriggerUseCase,
                showDeviceInfoUseCase,
                actionUiHelper,
                constraintUiHelper,
                launcherShortcutAdapter,
                resourceProvider
            ) as T
    }
}

data class ConfigKeymapUiState(
    override val isEnabled: Boolean
) : ConfigMappingUiState