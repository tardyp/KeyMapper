package io.github.sds100.keymapper.domain.mappings.keymap

import io.github.sds100.keymapper.data.repository.KeymapRepository

/**
 * Created by sds100 on 08/03/2021.
 */

class SaveKeymapUseCaseImpl(private val keymapRepository: KeymapRepository) : SaveKeymapUseCase {
    override fun invoke(keymap: KeyMap) {
        if (keymap.dbId == KeyMap.NEW_ID) {
            keymapRepository.insertKeymap(KeyMapEntityMapper.toEntity(keymap.copy(dbId = 0)))
        } else {
            keymapRepository.updateKeymap(KeyMapEntityMapper.toEntity(keymap))
        }
    }
}

interface SaveKeymapUseCase {
    operator fun invoke(keymap: KeyMap)
}