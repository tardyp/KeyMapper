package io.github.sds100.keymapper.domain.adapter

/**
 * Created by sds100 on 14/02/2021.
 */
interface KeyboardAdapter {
    fun chooseLastUsedIncompatibleInputMethod()
    fun chooseCompatibleInputMethod()
    fun showImePickerOutsideApp()
}