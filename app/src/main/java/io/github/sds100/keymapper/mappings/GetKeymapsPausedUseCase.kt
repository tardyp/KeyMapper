package io.github.sds100.keymapper.mappings

import io.github.sds100.keymapper.data.Keys
import io.github.sds100.keymapper.data.repositories.PreferenceRepository
import io.github.sds100.keymapper.util.FlowPrefDelegate

/**
 * Created by sds100 on 14/02/2021.
 */
//TODO delete
class GetKeymapsPausedUseCase(private val preferenceRepository: PreferenceRepository) :
    PreferenceRepository by preferenceRepository {
    private val keymapsPaused by FlowPrefDelegate(Keys.mappingsPaused, false)

    operator fun invoke() = keymapsPaused
}