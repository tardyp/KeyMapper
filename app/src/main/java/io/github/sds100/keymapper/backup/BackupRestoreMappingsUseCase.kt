package io.github.sds100.keymapper.backup

import io.github.sds100.keymapper.data.repository.FingerprintMapRepository
import io.github.sds100.keymapper.domain.BackupManager
import io.github.sds100.keymapper.domain.mappings.keymap.KeyMapRepository
import io.github.sds100.keymapper.util.result.Result
import kotlinx.coroutines.flow.Flow

/**
 * Created by sds100 on 16/04/2021.
 */

class BackupRestoreMappingsUseCaseImpl(
    private val keyMapRepository: KeyMapRepository,
    private val fingerprintMapRepository: FingerprintMapRepository,
    private val backupManager: BackupManager,
): BackupRestoreMappingsUseCase{

    override fun enableAllMappings() {
        keyMapRepository.enableAll()
        fingerprintMapRepository.enableAll()
    }

    override fun disableAllMappings() {
        keyMapRepository.disableAll()
        fingerprintMapRepository.disableAll()
    }

    override val onBackupResult: Flow<Result<*>> = backupManager.onBackupResult
    override val onRestoreResult: Flow<Result<*>> = backupManager.onRestoreResult
    override val onAutomaticBackupResult: Flow<Result<*>> = backupManager.onAutomaticBackupResult

    override fun backupAllMappings(uri: String) {
        backupManager.backupMappings(uri)
    }

    override fun restoreMappings(uri: String) {
        backupManager.restoreMappings(uri)
    }
}

interface BackupRestoreMappingsUseCase {
    val onBackupResult: Flow<Result<*>>
    val onRestoreResult: Flow<Result<*>>
    val onAutomaticBackupResult: Flow<Result<*>>

    fun enableAllMappings()
    fun disableAllMappings()
    fun backupAllMappings(uri: String)
    fun restoreMappings(uri: String)
}