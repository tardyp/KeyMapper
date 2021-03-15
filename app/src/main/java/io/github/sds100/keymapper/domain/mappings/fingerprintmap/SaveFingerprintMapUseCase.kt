package io.github.sds100.keymapper.domain.mappings.fingerprintmap

import io.github.sds100.keymapper.data.repository.FingerprintMapRepository

/**
 * Created by sds100 on 08/03/2021.
 */

class SaveFingerprintMapUseCaseImpl(
    private val repository: FingerprintMapRepository
) : SaveFingerprintMapUseCase {
    override fun invoke(fingerprintMap: FingerprintMap) =
        repository.updateGesture(fingerprintMap.id) {
            FingerprintMapEntityMapper.toEntity(
                fingerprintMap
            )
        }
}

interface SaveFingerprintMapUseCase {
    operator fun invoke(fingerprintMap: FingerprintMap)
}