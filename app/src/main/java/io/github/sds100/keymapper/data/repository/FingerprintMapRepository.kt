package io.github.sds100.keymapper.data.repository

import androidx.lifecycle.LiveData
import io.github.sds100.keymapper.data.model.FingerprintMapEntity
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.FingerprintMapId
import io.github.sds100.keymapper.util.BackupRequest
import kotlinx.coroutines.flow.Flow

/**
 * Created by sds100 on 24/01/21.
 */
interface FingerprintMapRepository {
    val requestAutomaticBackup: LiveData<BackupRequest<Map<String, FingerprintMapEntity>>>

    //TODO remove these and replace with get()
    val swipeDown: Flow<FingerprintMapEntity>
    val swipeUp: Flow<FingerprintMapEntity>
    val swipeLeft: Flow<FingerprintMapEntity>
    val swipeRight: Flow<FingerprintMapEntity>

    suspend fun get(id: FingerprintMapId): FingerprintMapEntity

    val fingerprintGestureMaps: Flow<Map<String, FingerprintMapEntity>>
    val fingerprintGesturesAvailable: Flow<Boolean?>

    fun setFingerprintGesturesAvailable(available: Boolean)
    fun restore(id: FingerprintMapId, fingerprintMapJson: String)

    fun updateGesture(
        id: FingerprintMapId,
        block: (old: FingerprintMapEntity) -> FingerprintMapEntity
    )

    fun reset()
}