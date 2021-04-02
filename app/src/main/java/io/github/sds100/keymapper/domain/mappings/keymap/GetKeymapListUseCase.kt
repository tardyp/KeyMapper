package io.github.sds100.keymapper.domain.mappings.keymap

import io.github.sds100.keymapper.domain.utils.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * Created by sds100 on 18/03/2021.
 */
class GetKeymapListUseCaseImpl(
    repository: KeymapRepository
) : GetKeymapListUseCase {
    override val keymapList = repository.keymapList
        .filter { it is State.Data }
        .map { state ->
            require(state is State.Data)
            state.data.map { KeyMapEntityMapper.fromEntity(it) }
        }
        .flowOn(Dispatchers.Default)
}

interface GetKeymapListUseCase {
    val keymapList: Flow<List<KeyMap>>
}