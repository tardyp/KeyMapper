package io.github.sds100.keymapper.mappings.keymaps


/**
 * Created by sds100 on 08/03/2021.
 */

class GetKeyMapUseCaseImpl(
    private val repository: KeyMapRepository,
) : GetKeyMapUseCase {

    override suspend operator fun invoke(uid: String): KeyMap? {
        return repository.get(uid)?.let { KeyMapEntityMapper.fromEntity(it) }
    }
}

interface GetKeyMapUseCase {
    suspend operator fun invoke(uid: String): KeyMap?
}