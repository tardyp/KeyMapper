package io.github.sds100.keymapper.domain.usecases

import io.github.sds100.keymapper.domain.preferences.Keys
import io.github.sds100.keymapper.domain.repositories.PreferenceRepository
import io.github.sds100.keymapper.domain.utils.PrefDelegate

/**
 * Created by sds100 on 14/02/21.
 */

internal class PerformActionsUseCaseImpl(
    preferenceRepository: PreferenceRepository
) : PreferenceRepository by preferenceRepository, PerformActionsUseCase {
    override val hasRootPermission by PrefDelegate(Keys.hasRootPermission, false)
}

interface PerformActionsUseCase {
    val hasRootPermission: Boolean
}