package io.github.sds100.keymapper.domain.mappings.keymap

/**
 * Created by sds100 on 20/03/2021.
 */

class DeleteKeymapsUseCaseImpl(
    private val repository: KeyMapRepository,
) : DeleteKeymapsUseCase {

    override operator fun invoke(vararg uid: String) {
        repository.delete(*uid)
    }
}

interface DeleteKeymapsUseCase {
    operator fun invoke(vararg uid: String)
}