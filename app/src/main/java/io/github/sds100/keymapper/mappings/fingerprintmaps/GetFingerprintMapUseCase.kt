package io.github.sds100.keymapper.mappings.fingerprintmaps

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Created by sds100 on 08/03/2021.
 */

class GetFingerprintMapUseCaseImpl(
    repository: FingerprintMapRepository,
) : GetFingerprintMapUseCase {

    override val swipeDown = repository.fingerprintMaps.map {
        FingerprintMapEntityMapper.fromEntity(
            it.swipeDown
        )
    }
    override val swipeUp = repository.fingerprintMaps.map { FingerprintMapEntityMapper.fromEntity(it.swipeUp) }
    override val swipeLeft = repository.fingerprintMaps.map {
        FingerprintMapEntityMapper.fromEntity(
            it.swipeLeft
        )
    }
    override val swipeRight =
        repository.fingerprintMaps.map { FingerprintMapEntityMapper.fromEntity(it.swipeRight) }
}

interface GetFingerprintMapUseCase {
    val swipeDown: Flow<FingerprintMap>
    val swipeUp: Flow<FingerprintMap>
    val swipeLeft: Flow<FingerprintMap>
    val swipeRight: Flow<FingerprintMap>
}