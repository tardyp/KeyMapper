package io.github.sds100.keymapper.mappings.keymaps

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.sds100.keymapper.constraints.ConstraintUtils
import io.github.sds100.keymapper.actions.ConfigActionsViewModel
import io.github.sds100.keymapper.constraints.ConfigConstraintsViewModel
import io.github.sds100.keymapper.actions.TestActionUseCase
import io.github.sds100.keymapper.mappings.keymaps.trigger.RecordTriggerUseCase
import io.github.sds100.keymapper.onboarding.OnboardingUseCase
import io.github.sds100.keymapper.util.State
import io.github.sds100.keymapper.util.ui.ResourceProvider
import io.github.sds100.keymapper.mappings.keymaps.trigger.ConfigTriggerKeyViewModel
import io.github.sds100.keymapper.mappings.ConfigMappingUiState
import io.github.sds100.keymapper.mappings.ConfigMappingViewModel
import io.github.sds100.keymapper.ui.utils.getJsonSerializable
import io.github.sds100.keymapper.ui.utils.putJsonSerializable
import io.github.sds100.keymapper.util.ifIsData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Created by sds100 on 22/11/20.
 */

class ConfigKeyMapViewModel(
    private val save: SaveKeyMapUseCase,
    private val get: GetKeyMapUseCase,
    private val config: ConfigKeyMapUseCase,
    private val testAction: TestActionUseCase,
    private val onboard: OnboardingUseCase,
    private val recordTrigger: RecordTriggerUseCase,
    private val createKeyMapShortcut: CreateKeyMapShortcutUseCase,
    private val displayMapping: DisplayKeyMapUseCase,
    resourceProvider: ResourceProvider
) : ViewModel(), ConfigMappingViewModel, ResourceProvider by resourceProvider {

    companion object {
        private const val STATE_KEY = "config_keymap"
    }

    val configActionOptionsViewModel =
        ConfigKeyMapActionOptionsViewModel(viewModelScope, config, resourceProvider)

    val configTriggerKeyViewModel =
        ConfigTriggerKeyViewModel(viewModelScope, config, resourceProvider)

    override val configActionsViewModel = ConfigActionsViewModel(
        viewModelScope,
        displayMapping,
        testAction,
        config,
        KeyMapActionUiHelper(displayMapping, resourceProvider),
        resourceProvider
    )

    val configTriggerViewModel = ConfigKeyMapTriggerViewModel(
        viewModelScope,
        onboard,
        config,
        recordTrigger,
        createKeyMapShortcut,
        displayMapping,
        resourceProvider
    )

    override val configConstraintsViewModel = ConfigConstraintsViewModel(
        viewModelScope,
        displayMapping,
        config,
        ConstraintUtils.KEY_MAP_ALLOWED_CONSTRAINTS,
        resourceProvider
    )

    override val state = MutableStateFlow<ConfigMappingUiState>(buildUiState(State.Loading))

    override fun setEnabled(enabled: Boolean) = config.setEnabled(enabled)

    init {
        viewModelScope.launch {
            config.mapping.collectLatest {
                state.value = buildUiState(it)
            }
        }
    }

    override fun save() = config.getMapping().ifIsData {
        save(it)
    }

    override fun saveState(outState: Bundle) {
        config.getMapping().ifIsData {
            outState.putJsonSerializable(STATE_KEY, it)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun restoreState(state: Bundle) {
        val keyMap = state.getJsonSerializable<KeyMap>(STATE_KEY) ?: KeyMap()
        config.setMapping(keyMap)
    }

    fun loadNewKeymap() {
        config.setMapping(KeyMap())
    }

    fun loadKeymap(uid: String) {
        viewModelScope.launch {
            config.setMapping(get(uid)!!)
        }
    }

    private fun buildUiState(configState: State<KeyMap>): ConfigKeymapUiState {
        return when (configState) {
            is State.Data -> ConfigKeymapUiState(configState.data.isEnabled)
            is State.Loading -> ConfigKeymapUiState(isEnabled = false)
        }
    }

    class Factory(
        private val save: SaveKeyMapUseCase,
        private val get: GetKeyMapUseCase,
        private val config: ConfigKeyMapUseCase,
        private val testAction: TestActionUseCase,
        private val onboard: OnboardingUseCase,
        private val recordTrigger: RecordTriggerUseCase,
        private val createKeyMapShortcut: CreateKeyMapShortcutUseCase,
        private val displayMapping: DisplayKeyMapUseCase,
        private val resourceProvider: ResourceProvider
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) =
            ConfigKeyMapViewModel(
                save,
                get,
                config,
                testAction,
                onboard,
                recordTrigger,
                createKeyMapShortcut,
                displayMapping,
                resourceProvider
            ) as T
    }
}

data class ConfigKeymapUiState(
    override val isEnabled: Boolean
) : ConfigMappingUiState