package io.github.sds100.keymapper.domain.mappings.keymap


/**
 * Created by sds100 on 08/03/2021.
 */

class GetKeymapUseCaseImpl(
    private val repository: KeyMapRepository,
) : GetKeymapUseCase {

    override suspend operator fun invoke(uid: String): KeyMap? {
        return repository.get(uid)?.let { KeyMapEntityMapper.fromEntity(it) }
    }
}

interface GetKeymapUseCase {
    suspend operator fun invoke(uid: String): KeyMap?
}