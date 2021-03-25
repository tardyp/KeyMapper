package io.github.sds100.keymapper.domain.mappings.keymap

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Created by sds100 on 18/03/2021.
 */
class ListKeymapsUseCaseImpl(
    private val repository: KeymapRepository
) : ListKeymapsUseCase {
    override val keymapList = repository.keymapList.map { list ->
        withContext(Dispatchers.Default) {
            list.map { KeyMapEntityMapper.fromEntity(it) }
        }
    }
}

interface ListKeymapsUseCase {
    val keymapList: Flow<List<KeyMap>>
}