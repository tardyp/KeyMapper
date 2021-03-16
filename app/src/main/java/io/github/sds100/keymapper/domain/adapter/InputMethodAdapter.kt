package io.github.sds100.keymapper.domain.adapter

import io.github.sds100.keymapper.util.result.Result
import kotlinx.coroutines.flow.StateFlow

/**
 * Created by sds100 on 14/02/2021.
 */
interface InputMethodAdapter {
    fun enableCompatibleInputMethods()
    fun chooseLastUsedIncompatibleInputMethod()
    fun chooseCompatibleInputMethod()
    fun showImePickerOutsideApp()
    fun getLabel(imeId: String): Result<String>

    val chosenImePackageName: StateFlow<String?>
}