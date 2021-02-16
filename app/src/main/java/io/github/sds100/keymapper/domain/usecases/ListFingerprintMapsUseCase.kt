package io.github.sds100.keymapper.domain.usecases

import io.github.sds100.keymapper.domain.preferences.Keys
import io.github.sds100.keymapper.domain.repositories.PreferenceRepository
import io.github.sds100.keymapper.domain.utils.PrefDelegate

/**
 * Created by sds100 on 15/02/2021.
 */
class ListFingerprintMapsUseCase(preferenceRepository: PreferenceRepository) :
    PreferenceRepository by preferenceRepository {

    val hasRootPermission by PrefDelegate(Keys.hasRootPermission, false)
}