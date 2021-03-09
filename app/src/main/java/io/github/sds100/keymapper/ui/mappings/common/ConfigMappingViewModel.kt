package io.github.sds100.keymapper.ui.mappings.common

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.github.sds100.keymapper.data.viewmodel.ActionListViewModel
import io.github.sds100.keymapper.util.result.RecoverableError

/**
 * Created by sds100 on 17/01/21.
 */

interface ConfigMappingViewModel {
    val actionListViewModel: ActionListViewModel<*>
    val isEnabled: MutableLiveData<Boolean>
    val fixError: LiveData<RecoverableError>
    val enableAccessibilityServicePrompt: LiveData<Unit>

    fun save()
    fun saveState(outState: Bundle)
    fun restoreState(state: Bundle)
}