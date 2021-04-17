package io.github.sds100.keymapper.inputmethod

import io.github.sds100.keymapper.domain.adapter.InputMethodAdapter

/**
 * Created by sds100 on 16/04/2021.
 */

class ShowInputMethodPickerUseCaseImpl(
    private val inputMethodAdapter: InputMethodAdapter
) : ShowInputMethodPickerUseCase {
    override fun show(fromForeground: Boolean) {
        inputMethodAdapter.showImePicker(fromForeground = fromForeground)
    }
}

interface ShowInputMethodPickerUseCase {
    fun show(fromForeground: Boolean)
}