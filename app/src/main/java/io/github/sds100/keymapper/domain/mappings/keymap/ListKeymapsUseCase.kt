package io.github.sds100.keymapper.domain.mappings.keymap

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Created by sds100 on 18/03/2021.
 */
class ListKeymapsUseCaseImpl(
    private val repository: KeymapRepository
) : ListKeymapsUseCase {
    override val keymapList = repository.keymapList.map { list ->
        list.map { KeyMapEntityMapper.fromEntity(it) }
    }
}

interface ListKeymapsUseCase {
    val keymapList: Flow<List<KeyMap>>
}