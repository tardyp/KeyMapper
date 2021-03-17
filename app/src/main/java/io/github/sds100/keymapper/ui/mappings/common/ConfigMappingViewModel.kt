package io.github.sds100.keymapper.ui.mappings.common

import android.os.Bundle
import androidx.lifecycle.LiveData
import io.github.sds100.keymapper.data.viewmodel.ActionListViewModel
import io.github.sds100.keymapper.domain.actions.ActionData
import io.github.sds100.keymapper.util.ViewState
import io.github.sds100.keymapper.util.result.RecoverableError

/**
 * Created by sds100 on 17/01/21.
 */

interface ConfigMappingViewModel {
    val viewState: LiveData<ViewState>
    val actionListViewModel: ActionListViewModel<*>

    val isEnabled: LiveData<Boolean>
    fun setEnabled(enabled: Boolean)

    val fixError: LiveData<RecoverableError>
    val enableAccessibilityServicePrompt: LiveData<Unit>

    fun addAction(actionData: ActionData)

    fun save()
    fun saveState(outState: Bundle)
    fun restoreState(state: Bundle)
}