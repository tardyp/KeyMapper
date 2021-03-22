package io.github.sds100.keymapper.ui.mappings.fingerprintmap

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.sds100.keymapper.data.viewmodel.ActionListViewModel
import io.github.sds100.keymapper.data.viewmodel.ConstraintListViewModel
import io.github.sds100.keymapper.domain.actions.ActionData
import io.github.sds100.keymapper.domain.actions.ConfigActionsUseCase
import io.github.sds100.keymapper.domain.actions.GetActionErrorUseCase
import io.github.sds100.keymapper.domain.actions.TestActionUseCase
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.*
import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.domain.utils.ifIsData
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.ui.actions.ActionUiHelper
import io.github.sds100.keymapper.ui.constraints.ConstraintUiHelper
import io.github.sds100.keymapper.ui.mappings.common.ConfigMappingUiState
import io.github.sds100.keymapper.ui.mappings.common.ConfigMappingViewModel
import io.github.sds100.keymapper.ui.utils.getJsonSerializable
import io.github.sds100.keymapper.util.result.RecoverableError
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Created by sds100 on 08/11/20.
 */

class ConfigFingerprintMapViewModel(
    private val save: SaveFingerprintMapUseCase,
    private val get: GetFingerprintMapUseCase,
    private val configUseCase: ConfigFingerprintMapUseCase,
    configActions: ConfigActionsUseCase<FingerprintMapAction>,
    getActionError: GetActionErrorUseCase,
    testAction: TestActionUseCase,
    actionUiHelper: ActionUiHelper<FingerprintMapAction>,
    constraintUiHelper: ConstraintUiHelper,
    resourceProvider: ResourceProvider
) : ViewModel(), ConfigMappingViewModel {

    companion object {
        private const val STATE_KEY = "config_fingerprint_map"
    }

    val actionListViewModel = ActionListViewModel(
        viewModelScope,
        configActions,
        getActionError,
        testAction,
        actionUiHelper,
        resourceProvider
    )

    val constraintListViewModel: ConstraintListViewModel = TODO()

    override val state = MutableStateFlow<ConfigMappingUiState>(buildUiState(State.Loading))

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
            merge(actionListViewModel.enableAccessibilityServicePrompt).collectLatest {
                enableAccessibilityServicePrompt.emit(it)
            }
        }

        runBlocking { rebuildUiState.emit(Unit) } //build the initial state on init
    }

    override fun save() = configUseCase.getFingerprintMap().ifIsData { save(it) }

    override fun saveState(outState: Bundle) {
        configUseCase.getFingerprintMap().ifIsData {
            outState.putString(STATE_KEY, Json.encodeToString(it))
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun restoreState(state: Bundle) {
        state.getJsonSerializable<FingerprintMap>(STATE_KEY)?.let {
            configUseCase.setFingerprintMap(it)
        }
    }

    fun loadFingerprintMap(id: FingerprintMapId) {
        viewModelScope.launch {
            configUseCase.setFingerprintMap(get(id))
        }
    }

    override fun rebuildUiState() {
        actionListViewModel.rebuildUiState()
        constraintListViewModel.rebuildUiState()
        //TODO rebuild options viewmodel
    }

    override fun addAction(actionData: ActionData) = actionListViewModel.addAction(actionData)

    private fun buildUiState(configState: State<ConfigFingerprintMapState>): ConfigFingerprintMapUiState {
        return when (configState) {
            is State.Data -> ConfigFingerprintMapUiState(configState.data.isEnabled)
            is State.Loading -> ConfigFingerprintMapUiState(isEnabled = false)
        }
    }

    class Factory(
        private val save: SaveFingerprintMapUseCase,
        private val get: GetFingerprintMapUseCase,
        private val config: ConfigFingerprintMapUseCase,
        private val configActions: ConfigActionsUseCase<FingerprintMapAction>,
        private val getActionError: GetActionErrorUseCase,
        private val testAction: TestActionUseCase,
        private val actionUiHelper: ActionUiHelper<FingerprintMapAction>,
        private val constraintUiHelper: ConstraintUiHelper,
        private val resourceProvider: ResourceProvider
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) =
            ConfigFingerprintMapViewModel(
                save,
                get,
                config,
                configActions,
                getActionError,
                testAction,
                actionUiHelper,
                constraintUiHelper,
                resourceProvider
            ) as T
    }
}

data class ConfigFingerprintMapUiState(
    override val isEnabled: Boolean
) : ConfigMappingUiState