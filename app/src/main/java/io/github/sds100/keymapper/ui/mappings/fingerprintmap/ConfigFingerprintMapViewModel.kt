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
import io.github.sds100.keymapper.domain.constraints.ConfigConstraintsUseCase
import io.github.sds100.keymapper.domain.constraints.GetConstraintErrorUseCase
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.*
import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.domain.utils.ifIsData
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.ui.DialogViewModel
import io.github.sds100.keymapper.ui.DialogViewModelImpl
import io.github.sds100.keymapper.ui.actions.ActionUiHelper
import io.github.sds100.keymapper.ui.constraints.ConstraintUiHelper
import io.github.sds100.keymapper.ui.mappings.common.ConfigMappingUiState
import io.github.sds100.keymapper.ui.mappings.common.ConfigMappingViewModel
import io.github.sds100.keymapper.ui.utils.getJsonSerializable
import kotlinx.coroutines.CoroutineScope
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
    configConstraints: ConfigConstraintsUseCase,
    getActionError: GetActionErrorUseCase,
    getConstraintError: GetConstraintErrorUseCase,
    testAction: TestActionUseCase,
    actionUiHelper: ActionUiHelper<FingerprintMapAction>,
    constraintUiHelper: ConstraintUiHelper,
    resourceProvider: ResourceProvider
) : ViewModel(), ConfigMappingViewModel, DialogViewModel by DialogViewModelImpl() {

    companion object {
        private const val STATE_KEY_MAP = "config_fingerprint_map"
        private const val STATE_KEY_ID = "config_fingerprint_map_id"
    }

    val actionListViewModel = ActionListViewModel(
        viewModelScope,
        configActions,
        getActionError,
        testAction,
        actionUiHelper,
        resourceProvider
    )

    val constraintListViewModel = ConstraintListViewModel(
        viewModelScope,
        configConstraints,
        constraintUiHelper,
        getConstraintError,
        resourceProvider
    )

    override val state = MutableStateFlow<ConfigMappingUiState>(buildUiState(State.Loading))

    override fun setEnabled(enabled: Boolean) = configUseCase.setEnabled(enabled)

    override val fixError = merge(actionListViewModel.fixError, constraintListViewModel.fixError)

    private val rebuildUiState = MutableSharedFlow<Unit>()

    private lateinit var id: FingerprintMapId

    init {
        viewModelScope.launch {
            combine(rebuildUiState, configUseCase.state) { _, configState ->
                buildUiState(configState)
            }.collectLatest {
                state.value = it
            }
        }

        runBlocking { rebuildUiState.emit(Unit) } //build the initial state on init
    }

    override fun save() = configUseCase.getFingerprintMap().ifIsData { save.invoke(id, it) }

    override fun saveState(outState: Bundle) {
        configUseCase.getFingerprintMap().ifIsData {
            outState.putString(STATE_KEY_MAP, Json.encodeToString(it))
            outState.putString(STATE_KEY_ID, Json.encodeToString(id))
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun restoreState(state: Bundle) {
        state.getJsonSerializable<FingerprintMap>(STATE_KEY_MAP)?.let {
            configUseCase.setFingerprintMap(it)
        }

        state.getJsonSerializable<FingerprintMapId>(STATE_KEY_ID)?.let {
            id = it
        }
    }

    fun loadFingerprintMap(id: FingerprintMapId) {
        viewModelScope.launch {
            val map = when (id) {
                FingerprintMapId.SWIPE_DOWN -> get.swipeDown
                FingerprintMapId.SWIPE_UP -> get.swipeUp
                FingerprintMapId.SWIPE_LEFT -> get.swipeLeft
                FingerprintMapId.SWIPE_RIGHT -> get.swipeRight
            }.first()

            configUseCase.setFingerprintMap(map)
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
        private val configUseCase: ConfigFingerprintMapUseCase,
        private val configActions: ConfigActionsUseCase<FingerprintMapAction>,
        private val configConstraints: ConfigConstraintsUseCase,
        private val getActionError: GetActionErrorUseCase,
        private val getConstraintError: GetConstraintErrorUseCase,
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
                configUseCase,
                configActions,
                configConstraints,
                getActionError,
                getConstraintError,
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