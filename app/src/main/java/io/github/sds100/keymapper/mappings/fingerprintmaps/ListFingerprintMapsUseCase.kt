package io.github.sds100.keymapper.mappings.fingerprintmaps

import android.os.Build
import io.github.sds100.keymapper.data.repository.FingerprintMapRepository
import io.github.sds100.keymapper.domain.BackupManager
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.FingerprintMapEntityMapper
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.FingerprintMapId
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.FingerprintMapIdEntityMapper
import io.github.sds100.keymapper.domain.preferences.Keys
import io.github.sds100.keymapper.domain.repositories.PreferenceRepository
import io.github.sds100.keymapper.mappings.DisplaySimpleMappingUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * Created by sds100 on 16/04/2021.
 */

class ListFingerprintMapsUseCaseImpl(
    private val fingerprintMapRepository: FingerprintMapRepository,
    private val backupManager: BackupManager,
    private val preferenceRepository: PreferenceRepository,
    displaySimpleMappingUseCase: DisplaySimpleMappingUseCase
) : ListFingerprintMapsUseCase, DisplaySimpleMappingUseCase by displaySimpleMappingUseCase {

    override val fingerprintMaps: Flow<FingerprintMapGroup> =
        fingerprintMapRepository.fingerprintMaps
            .map { entityGroup ->
                FingerprintMapGroup(
                    swipeDown = FingerprintMapEntityMapper.fromEntity(entityGroup.swipeDown),
                    swipeUp = FingerprintMapEntityMapper.fromEntity(entityGroup.swipeUp),
                    swipeLeft = FingerprintMapEntityMapper.fromEntity(entityGroup.swipeLeft),
                    swipeRight = FingerprintMapEntityMapper.fromEntity(entityGroup.swipeRight),
                )
            }.flowOn(Dispatchers.Default)

    override val showFingerprintMaps: Flow<Boolean> =
        preferenceRepository.get(Keys.fingerprintGesturesAvailable).map {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return@map false

            it ?: false
        }

    override fun resetFingerprintMaps() {
        fingerprintMapRepository.reset()
    }

    override fun enableFingerprintMap(id: FingerprintMapId) {
        val entityId = FingerprintMapIdEntityMapper.toEntity(id)
        fingerprintMapRepository.enableFingerprintMap(entityId)
    }

    override fun disableFingerprintMap(id: FingerprintMapId) {
        val entityId = FingerprintMapIdEntityMapper.toEntity(id)
        fingerprintMapRepository.disableFingerprintMap(entityId)
    }

    override fun backupFingerprintMaps(uri: String) {
        backupManager.backupFingerprintMaps(uri)
    }
}

interface ListFingerprintMapsUseCase : DisplaySimpleMappingUseCase {
    val fingerprintMaps: Flow<FingerprintMapGroup>
    val showFingerprintMaps: Flow<Boolean>

    fun enableFingerprintMap(id: FingerprintMapId)
    fun disableFingerprintMap(id: FingerprintMapId)
    fun resetFingerprintMaps()
    fun backupFingerprintMaps(uri: String)
}