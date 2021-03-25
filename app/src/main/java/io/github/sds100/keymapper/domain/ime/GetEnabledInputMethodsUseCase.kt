package io.github.sds100.keymapper.domain.ime

import io.github.sds100.keymapper.domain.adapter.InputMethodAdapter

/**
 * Created by sds100 on 23/03/2021.
 */

class GetEnabledInputMethodsUseCaseImpl(
    private val adapter: InputMethodAdapter
) : GetEnabledInputMethodsUseCase {
    override fun invoke(): List<ImeInfo> {
        return adapter.getEnabledInputMethods()
    }
}

interface GetEnabledInputMethodsUseCase {
    operator fun invoke(): List<ImeInfo>
}