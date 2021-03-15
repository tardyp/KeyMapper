package io.github.sds100.keymapper.ui.mappings.fingerprintmap

import android.os.Bundle
import androidx.lifecycle.*
import com.hadilq.liveevent.LiveEvent
import io.github.sds100.keymapper.data.model.ConstraintEntity
import io.github.sds100.keymapper.data.viewmodel.ActionListViewModel
import io.github.sds100.keymapper.data.viewmodel.ConstraintListViewModel
import io.github.sds100.keymapper.domain.actions.ConfigActionsUseCase
import io.github.sds100.keymapper.domain.actions.GetActionErrorUseCase
import io.github.sds100.keymapper.domain.actions.TestActionUseCase
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.*
import io.github.sds100.keymapper.ui.actions.ActionListItemMapper
import io.github.sds100.keymapper.ui.mappings.common.ConfigMappingViewModel
import io.github.sds100.keymapper.ui.utils.getJsonSerializable
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.result.RecoverableError
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
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
    actionListItemMapper: ActionListItemMapper<FingerprintMapAction>,
) : ViewModel(), ConfigMappingViewModel {

    companion object {
        private const val STATE_KEY = "config_fingerprint_map"
    }

    private val dataState = MutableLiveData<ConfigFingerprintMapState?>()

    //TODO hide UI elements if loading
    private val _viewState = MutableLiveData<ViewState>(ViewLoading())
    override val viewState: LiveData<ViewState> = _viewState

    override val actionListViewModel = ActionListViewModel(
        viewModelScope,
        configActions,
        getActionError,
        testAction,
        actionListItemMapper
    )

    val constraintListViewModel =
        ConstraintListViewModel(viewModelScope, ConstraintEntity.COMMON_SUPPORTED_CONSTRAINTS)

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
    }

    override val enableAccessibilityServicePrompt: LiveData<Unit> =
        _enableAccessibilityServicePrompt

    init {
        configUseCase.state.onEach {
            if (it is Data<ConfigFingerprintMapState>) {
                dataState.value = it.data
                _viewState.value = ViewPopulated()
            } else {
                dataState.value = null
                _viewState.value = ViewLoading()
            }
        }.launchIn(viewModelScope)
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

    class Factory(
        private val save: SaveFingerprintMapUseCase,
        private val get: GetFingerprintMapUseCase,
        private val config: ConfigFingerprintMapUseCase,
        private val configActions: ConfigActionsUseCase<FingerprintMapAction>,
        private val getActionError: GetActionErrorUseCase,
        private val testAction: TestActionUseCase,
        private val actionListItemMapper: ActionListItemMapper<FingerprintMapAction>,
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
                actionListItemMapper
            ) as T
    }
}