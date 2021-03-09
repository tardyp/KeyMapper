package io.github.sds100.keymapper.domain.mappings.keymap

import io.github.sds100.keymapper.data.repository.KeymapRepository
import io.github.sds100.keymapper.domain.adapter.ExternalDeviceAdapter
import io.github.sds100.keymapper.domain.models.KeyMap
import io.github.sds100.keymapper.domain.models.KeyMapEntityMapper

/**
 * Created by sds100 on 08/03/2021.
 */

class GetKeymapUseCaseImpl(
    private val keymapRepository: KeymapRepository,
    private val deviceAdapter: ExternalDeviceAdapter
) : GetKeymapUseCase {

    override suspend operator fun invoke(id: Long): KeyMap {
        return KeyMapEntityMapper.fromEntity(keymapRepository.getKeymap(id), deviceAdapter)
    }
}

interface GetKeymapUseCase {
    suspend operator fun invoke(id: Long): KeyMap
}