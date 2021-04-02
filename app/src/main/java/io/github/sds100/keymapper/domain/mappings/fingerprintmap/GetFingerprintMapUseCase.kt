package io.github.sds100.keymapper.domain.mappings.fingerprintmap

import io.github.sds100.keymapper.data.repository.FingerprintMapRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Created by sds100 on 08/03/2021.
 */

class GetFingerprintMapUseCaseImpl(
    repository: FingerprintMapRepository,
) : GetFingerprintMapUseCase {

    override val swipeDown = repository.swipeDown.map { FingerprintMapEntityMapper.fromEntity(it) }
    override val swipeUp = repository.swipeUp.map { FingerprintMapEntityMapper.fromEntity(it) }
    override val swipeLeft = repository.swipeLeft.map { FingerprintMapEntityMapper.fromEntity(it) }
    override val swipeRight =
        repository.swipeRight.map { FingerprintMapEntityMapper.fromEntity(it) }
}

interface GetFingerprintMapUseCase {
    val swipeDown: Flow<FingerprintMap>
    val swipeUp: Flow<FingerprintMap>
    val swipeLeft: Flow<FingerprintMap>
    val swipeRight: Flow<FingerprintMap>
}