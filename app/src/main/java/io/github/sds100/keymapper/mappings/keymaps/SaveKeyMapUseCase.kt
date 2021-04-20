package io.github.sds100.keymapper.mappings.keymaps

/**
 * Created by sds100 on 08/03/2021.
 */

class SaveKeyMapUseCaseImpl(private val keyMapRepository: KeyMapRepository) : SaveKeyMapUseCase {
    override fun invoke(keymap: KeyMap) {

        if (keymap.dbId == null) {
            keyMapRepository.insert(KeyMapEntityMapper.toEntity(keymap, 0))
        } else {
            keyMapRepository.update(KeyMapEntityMapper.toEntity(keymap, keymap.dbId))
        }
    }
}

interface SaveKeyMapUseCase {
    operator fun invoke(keymap: KeyMap)
}