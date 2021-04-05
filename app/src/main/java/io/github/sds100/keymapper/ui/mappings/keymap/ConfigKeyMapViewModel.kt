package io.github.sds100.keymapper.ui.mappings.keymap

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.sds100.keymapper.data.viewmodel.ActionListViewModel
import io.github.sds100.keymapper.data.viewmodel.ConfigConstraintsViewModel
import io.github.sds100.keymapper.domain.actions.ActionData
import io.github.sds100.keymapper.domain.actions.TestActionUseCase
import io.github.sds100.keymapper.domain.mappings.keymap.ConfigKeyMapUseCase
import io.github.sds100.keymapper.domain.mappings.keymap.GetKeymapUseCase
import io.github.sds100.keymapper.domain.mappings.keymap.KeyMap
import io.github.sds100.keymapper.domain.mappings.keymap.SaveKeymapUseCase
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.RecordTriggerUseCase
import io.github.sds100.keymapper.domain.usecases.OnboardingUseCase
import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.domain.utils.ifIsData
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.mappings.keymaps.DisplayKeyMapUseCase
import io.github.sds100.keymapper.ui.mappings.common.ConfigMappingUiState
import io.github.sds100.keymapper.ui.mappings.common.ConfigMappingViewModel
import io.github.sds100.keymapper.ui.shortcuts.CreateKeyMapShortcutUseCase
import io.github.sds100.keymapper.ui.utils.getJsonSerializable
import io.github.sds100.keymapper.ui.utils.putJsonSerializable
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Created by sds100 on 22/11/20.
 */

/*
TODO
- move classes in data, domain and ui into individual feature packages
- dont have individual use cases for modifying key maps. Create functions in ConfigKeymap object
 */

class ConfigKeyMapViewModel(
    private val save: SaveKeymapUseCase,
    private val get: GetKeymapUseCase,
    private val config: ConfigKeyMapUseCase,
    private val testAction: TestActionUseCase,
    private val onboard: OnboardingUseCase,
    private val recordTrigger: RecordTriggerUseCase,
    private val createKeyMapShortcut: CreateKeyMapShortcutUseCase,
    private val displayMapping: DisplayKeyMapUseCase,
    resourceProvider: ResourceProvider
) : ViewModel(), ConfigMappingViewModel,
    ResourceProvider by resourceProvider {

    companion object {
        private const val STATE_KEY = "config_keymap"
    }

    val actionListViewModel = ActionListViewModel(
        viewModelScope,
        displayMapping,
        testAction,
        config,
        KeyMapActionUiHelper(displayMapping, resourceProvider),
        resourceProvider
    )

    val triggerViewModel = TriggerViewModel(
        viewModelScope,
        onboard,
        config,
        recordTrigger,
        createKeyMapShortcut,
        displayMapping,
        resourceProvider
    )

    val constraintListViewModel = ConfigConstraintsViewModel(
        viewModelScope,
        displayMapping,
        config,
        resourceProvider
    )

    override val state = MutableStateFlow<ConfigMappingUiState>(buildUiState(State.Loading))

    override fun setEnabled(enabled: Boolean) = config.setEnabled(enabled)

    override val fixError = merge(
        triggerViewModel.fixError,
        actionListViewModel.fixError,
        constraintListViewModel.fixError
    )

    private val rebuildUiState = MutableSharedFlow<Unit>()

    init {
        viewModelScope.launch {
            combine(rebuildUiState, config.mapping) { _, configState ->
                buildUiState(configState)
            }.collectLatest {
                state.value = it
            }
        }

        runBlocking { rebuildUiState.emit(Unit) } //build the initial state on init
    }

    override fun save() = config.getMapping().ifIsData {
        viewModelScope.launch {
            save(it)
        }
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

    override fun rebuildUiState() {
        runBlocking { rebuildUiState.emit(Unit) }
        actionListViewModel.rebuildUiState()
        constraintListViewModel.rebuildUiState()
        triggerViewModel.rebuildUiState()
    }

    override fun addAction(actionData: ActionData) = config.addAction(actionData)

    private fun buildUiState(configState: State<KeyMap>): ConfigKeymapUiState {
        return when (configState) {
            is State.Data -> ConfigKeymapUiState(configState.data.isEnabled)
            is State.Loading -> ConfigKeymapUiState(isEnabled = false)
        }
    }

    class Factory(
        private val save: SaveKeymapUseCase,
        private val get: GetKeymapUseCase,
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