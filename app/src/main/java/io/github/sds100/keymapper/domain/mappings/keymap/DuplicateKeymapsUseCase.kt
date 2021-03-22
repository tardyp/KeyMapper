package io.github.sds100.keymapper.domain.mappings.keymap

/**
 * Created by sds100 on 22/03/2021.
 */

class DuplicateKeymapsUseCaseImpl(
    private val repository: KeymapRepository
) : DuplicateKeymapsUseCase {
    override fun invoke(vararg id: Long) = repository.duplicate(*id)
}

interface DuplicateKeymapsUseCase {
    operator fun invoke(vararg id: Long)
}