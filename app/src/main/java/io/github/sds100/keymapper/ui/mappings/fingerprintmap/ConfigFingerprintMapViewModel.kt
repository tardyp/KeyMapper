package io.github.sds100.keymapper.ui.mappings.fingerprintmap

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.sds100.keymapper.constraints.ConstraintUtils
import io.github.sds100.keymapper.data.viewmodel.ConfigActionsViewModel
import io.github.sds100.keymapper.data.viewmodel.ConfigConstraintsViewModel
import io.github.sds100.keymapper.domain.actions.TestActionUseCase
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.*
import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.domain.utils.ifIsData
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.mappings.DisplaySimpleMappingUseCase
import io.github.sds100.keymapper.mappings.fingerprintmaps.ConfigFingerprintMapActionOptionsViewModel
import io.github.sds100.keymapper.mappings.fingerprintmaps.ConfigFingerprintMapOptionsViewModel
import io.github.sds100.keymapper.ui.UserResponseViewModel
import io.github.sds100.keymapper.ui.UserResponseViewModelImpl
import io.github.sds100.keymapper.ui.mappings.common.ConfigMappingUiState
import io.github.sds100.keymapper.ui.mappings.common.ConfigMappingViewModel
import io.github.sds100.keymapper.ui.utils.getJsonSerializable
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
    private val config: ConfigFingerprintMapUseCase,
    private val testAction: TestActionUseCase,
    private val display: DisplaySimpleMappingUseCase,
    resourceProvider: ResourceProvider
) : ViewModel(), ConfigMappingViewModel, UserResponseViewModel by UserResponseViewModelImpl() {

    companion object {
        private const val STATE_FINGERPRINT_MAP = "config_fingerprint_map"
        private const val STATE_KEY_ID = "config_fingerprint_map_id"
    }

    val configActionOptionsViewModel =
        ConfigFingerprintMapActionOptionsViewModel(viewModelScope, config, resourceProvider)

    val configOptionsViewModel =
        ConfigFingerprintMapOptionsViewModel(viewModelScope, config, resourceProvider)

    override val configActionsViewModel = ConfigActionsViewModel(
        viewModelScope,
        display,
        testAction,
        config,
        FingerprintMapActionUiHelper(display, resourceProvider),
        resourceProvider
    )

    override val configConstraintsViewModel = ConfigConstraintsViewModel(
        viewModelScope,
        display,
        config,
        ConstraintUtils.FINGERPRINT_MAP_ALLOWED_CONSTRAINTS,
        resourceProvider
    )

    override val state = MutableStateFlow<ConfigMappingUiState>(buildUiState(State.Loading))

    private val rebuildUiState = MutableSharedFlow<Unit>()

    private lateinit var id: FingerprintMapId

    init {
        viewModelScope.launch {
            combine(rebuildUiState, config.mapping) { _, mapping ->
                buildUiState(mapping)
            }.collectLatest {
                state.value = it
            }
        }

        runBlocking { rebuildUiState.emit(Unit) } //build the initial state on init
    }

    override fun setEnabled(enabled: Boolean) = config.setEnabled(enabled)

    override fun save() = config.getMapping().ifIsData { save(id, it) }

    override fun saveState(outState: Bundle) {
        config.getMapping().ifIsData {
            outState.putString(STATE_FINGERPRINT_MAP, Json.encodeToString(it))
            outState.putString(STATE_KEY_ID, Json.encodeToString(id))
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun restoreState(state: Bundle) {
        state.getJsonSerializable<FingerprintMap>(STATE_FINGERPRINT_MAP)?.let {
            config.setMapping(it)
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

            this@ConfigFingerprintMapViewModel.id = id
            config.setMapping(map)
        }
    }

    private fun buildUiState(configState: State<FingerprintMap>): ConfigFingerprintMapUiState {
        return when (configState) {
            is State.Data -> ConfigFingerprintMapUiState(configState.data.isEnabled)
            is State.Loading -> ConfigFingerprintMapUiState(isEnabled = false)
        }
    }

    class Factory(
        private val save: SaveFingerprintMapUseCase,
        private val get: GetFingerprintMapUseCase,
        private val config: ConfigFingerprintMapUseCase,
        private val testAction: TestActionUseCase,
        private val display: DisplaySimpleMappingUseCase,
        private val resourceProvider: ResourceProvider
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) =
            ConfigFingerprintMapViewModel(
                save,
                get,
                config,
                testAction,
                display,
                resourceProvider
            ) as T
    }
}

data class ConfigFingerprintMapUiState(
    override val isEnabled: Boolean
) : ConfigMappingUiState