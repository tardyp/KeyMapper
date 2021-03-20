package io.github.sds100.keymapper.domain.mappings.keymap


/**
 * Created by sds100 on 08/03/2021.
 */

class GetKeymapUseCaseImpl(
    private val repository: KeymapRepository,
) : GetKeymapUseCase {

    override suspend operator fun invoke(id: Long): KeyMap {
        return KeyMapEntityMapper.fromEntity(repository.get(id))
    }
}

interface GetKeymapUseCase {
    suspend operator fun invoke(id: Long): KeyMap
}