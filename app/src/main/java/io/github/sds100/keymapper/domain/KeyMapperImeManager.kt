package io.github.sds100.keymapper.domain

import io.github.sds100.keymapper.domain.adapter.InputMethodAdapter

/**
 * Created by sds100 on 16/03/2021.
 */

class KeyMapperImeManager(val inputMethodAdapter: InputMethodAdapter) {
    fun enableCompatibleInputMethods() {

    }

    fun chooseCompatibleInputMethod(fromForeground: Boolean) {}
    fun chooseLastUsedIncompatibleInputMethod(fromForeground: Boolean) {}
    fun toggleCompatibleInputMethod() {}

    fun isCompatibleImeChosen(): Boolean {TODO()}
    fun isCompatibleImeEnabled(): Boolean {TODO()}
}