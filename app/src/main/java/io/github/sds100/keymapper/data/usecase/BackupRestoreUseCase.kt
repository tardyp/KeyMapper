package io.github.sds100.keymapper.data.usecase

import androidx.lifecycle.LiveData
import io.github.sds100.keymapper.data.model.KeyMap
import io.github.sds100.keymapper.util.BackupRequest

/**
 * Created by sds100 on 06/11/20.
 */
interface BackupRestoreUseCase {
    val requestAutomaticBackup: LiveData<BackupRequest<List<KeyMap>>>
    suspend fun getKeymaps(): List<KeyMap>

    fun restore(dbVersion: Int, keymapListJson: List<String>)
}