package io.github.sds100.keymapper.domain.usecases

import io.github.sds100.keymapper.domain.preferences.Keys
import io.github.sds100.keymapper.domain.repositories.PreferenceRepository
import io.github.sds100.keymapper.domain.utils.PrefDelegate

/**
 * Created by sds100 on 14/02/21.
 */
internal class BackupRestoreUseCase(
    preferenceRepository: PreferenceRepository
) : IBackupRestoreUseCase, PreferenceRepository by preferenceRepository {
    override val automaticBackupLocation by PrefDelegate(Keys.automaticBackupLocation, "")
}

interface IBackupRestoreUseCase {
    val automaticBackupLocation: String
}