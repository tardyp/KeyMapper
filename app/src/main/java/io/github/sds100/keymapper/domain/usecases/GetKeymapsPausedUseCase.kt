package io.github.sds100.keymapper.domain.usecases

import io.github.sds100.keymapper.domain.preferences.Keys
import io.github.sds100.keymapper.domain.repositories.PreferenceRepository
import io.github.sds100.keymapper.domain.utils.FlowPrefDelegate

/**
 * Created by sds100 on 14/02/2021.
 */
//TODO delete
class GetKeymapsPausedUseCase(private val preferenceRepository: PreferenceRepository) :
    PreferenceRepository by preferenceRepository {
    private val keymapsPaused by FlowPrefDelegate(Keys.mappingsPaused, false)

    operator fun invoke() = keymapsPaused
}