package io.github.sds100.keymapper.data.repository

import androidx.lifecycle.LiveData
import io.github.sds100.keymapper.data.model.FingerprintMapEntity
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.FingerprintMapId
import io.github.sds100.keymapper.mappings.fingerprintmaps.FingerprintMapEntityGroup
import io.github.sds100.keymapper.util.BackupRequest
import kotlinx.coroutines.flow.Flow

/**
 * Created by sds100 on 24/01/21.
 */
interface FingerprintMapRepository {
    val requestAutomaticBackup: LiveData<BackupRequest<Map<String, FingerprintMapEntity>>>

    val fingerprintMaps: Flow<FingerprintMapEntityGroup>

    fun enableFingerprintMap(id: String)
    fun disableFingerprintMap(id: String)
    fun enableAll()
    fun disableAll()

    fun update(id: String, fingerprintMap: FingerprintMapEntity)

    fun restore(id: String, fingerprintMapJson: String)

    fun reset()
}