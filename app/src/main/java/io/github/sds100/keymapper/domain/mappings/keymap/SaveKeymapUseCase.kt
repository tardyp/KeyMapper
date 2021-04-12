package io.github.sds100.keymapper.domain.mappings.keymap

/**
 * Created by sds100 on 08/03/2021.
 */

class SaveKeymapUseCaseImpl(private val keyMapRepository: KeyMapRepository) : SaveKeymapUseCase {
    override fun invoke(keymap: KeyMap) {

        if (keymap.dbId == null) {
            keyMapRepository.insert(KeyMapEntityMapper.toEntity(keymap, 0))
        } else {
            keyMapRepository.update(KeyMapEntityMapper.toEntity(keymap, keymap.dbId))
        }
    }
}

interface SaveKeymapUseCase {
    operator fun invoke(keymap: KeyMap)
}