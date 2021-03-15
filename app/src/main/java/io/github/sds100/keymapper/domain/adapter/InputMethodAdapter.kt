package io.github.sds100.keymapper.domain.adapter

import kotlinx.coroutines.flow.StateFlow

/**
 * Created by sds100 on 14/02/2021.
 */
interface InputMethodAdapter {
    fun enableCompatibleInputMethods()
    fun chooseLastUsedIncompatibleInputMethod()
    fun chooseCompatibleInputMethod()
    fun showImePickerOutsideApp()

    val chosenInputMethodPackageName: StateFlow<String>
}