package io.github.sds100.keymapper.data.usecase

import androidx.lifecycle.LiveData
import io.github.sds100.keymapper.data.model.KeyMapEntity
import io.github.sds100.keymapper.util.BackupRequest

/**
 * Created by sds100 on 06/11/20.
 */
interface BackupRestoreUseCase {
    val requestAutomaticBackup: LiveData<BackupRequest<List<KeyMapEntity>>>
    suspend fun getKeymaps(): List<KeyMapEntity>

    fun restore(dbVersion: Int, keymapListJson: List<String>)
}