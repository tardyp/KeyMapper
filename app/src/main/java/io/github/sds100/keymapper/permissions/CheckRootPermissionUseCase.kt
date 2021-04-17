package io.github.sds100.keymapper.permissions

import io.github.sds100.keymapper.domain.preferences.Keys
import io.github.sds100.keymapper.domain.repositories.PreferenceRepository
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