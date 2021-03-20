package io.github.sds100.keymapper.ui.mappings.common

import android.os.Bundle
import io.github.sds100.keymapper.domain.actions.ActionData
import io.github.sds100.keymapper.util.result.RecoverableError
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Created by sds100 on 17/01/21.
 */

interface ConfigMappingViewModel {
    val state: StateFlow<ConfigMappingUiState>
    fun setEnabled(enabled: Boolean)

    val fixError: SharedFlow<RecoverableError>
    val enableAccessibilityServicePrompt: SharedFlow<Unit>

    fun addAction(actionData: ActionData)

    fun rebuildUiState()

    fun save()
    fun saveState(outState: Bundle)
    fun restoreState(state: Bundle)
}

interface ConfigMappingUiState {
    val isEnabled: Boolean
}