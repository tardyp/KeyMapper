package io.github.sds100.keymapper.domain.mappings.keymap

import io.github.sds100.keymapper.data.repository.KeymapRepository

/**
 * Created by sds100 on 08/03/2021.
 */

class GetKeymapUseCaseImpl(
    private val keymapRepository: KeymapRepository,
) : GetKeymapUseCase {

    override suspend operator fun invoke(id: Long): KeyMap {
        return KeyMapEntityMapper.fromEntity(keymapRepository.getKeymap(id))
    }
}

interface GetKeymapUseCase {
    suspend operator fun invoke(id: Long): KeyMap
}