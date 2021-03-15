package io.github.sds100.keymapper.domain.mappings.fingerprintmap

import io.github.sds100.keymapper.data.repository.FingerprintMapRepository

/**
 * Created by sds100 on 08/03/2021.
 */

class GetFingerprintMapUseCaseImpl(
    private val repository: FingerprintMapRepository,
) : GetFingerprintMapUseCase {

    override suspend operator fun invoke(id: FingerprintMapId): FingerprintMap {
        return FingerprintMapEntityMapper.fromEntity(id, repository.get(id))
    }
}

interface GetFingerprintMapUseCase {
    suspend operator fun invoke(id: FingerprintMapId): FingerprintMap
}