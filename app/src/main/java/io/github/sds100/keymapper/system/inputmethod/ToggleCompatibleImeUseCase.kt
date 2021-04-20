package io.github.sds100.keymapper.system.inputmethod

import kotlinx.coroutines.flow.Flow

/**
 * Created by sds100 on 16/04/2021.
 */

class ToggleCompatibleImeUseCaseImpl(
    private val inputMethodAdapter: InputMethodAdapter
) : ToggleCompatibleImeUseCase {
    private val keyMapperImeHelper = KeyMapperImeHelper(inputMethodAdapter)

    override val canWork: Flow<Boolean> = inputMethodAdapter.canChangeImeWithoutUserInput
    override fun toggle() {
        keyMapperImeHelper.toggleCompatibleInputMethod()
    }
}

interface ToggleCompatibleImeUseCase {
    val canWork: Flow<Boolean>

    fun toggle()
}