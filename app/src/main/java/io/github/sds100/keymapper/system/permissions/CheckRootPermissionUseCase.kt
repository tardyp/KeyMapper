package io.github.sds100.keymapper.system.permissions

import io.github.sds100.keymapper.data.Keys
import io.github.sds100.keymapper.data.repositories.PreferenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Created by sds100 on 16/04/2021.
 */

class CheckRootPermissionUseCaseImpl(
    preferenceRepository: PreferenceRepository
) : CheckRootPermissionUseCase {
    override val isGranted: Flow<Boolean> =
        preferenceRepository.get(Keys.hasRootPermission).map { it ?: false }
}

interface CheckRootPermissionUseCase {
    val isGranted: Flow<Boolean>
}