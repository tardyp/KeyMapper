package io.github.sds100.keymapper.ui.mappings.common

import android.os.Bundle
import io.github.sds100.keymapper.domain.actions.ActionData
import io.github.sds100.keymapper.util.result.FixableError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Created by sds100 on 17/01/21.
 */

interface ConfigMappingViewModel {
    val state: StateFlow<ConfigMappingUiState>
    fun setEnabled(enabled: Boolean)

    val fixError: Flow<FixableError>

    fun addAction(actionData: ActionData)

    fun save()
    fun saveState(outState: Bundle)
    fun restoreState(state: Bundle)
}

interface ConfigMappingUiState {
    val isEnabled: Boolean
}