package io.github.sds100.keymapper.domain.usecases

import io.github.sds100.keymapper.domain.preferences.Keys
import io.github.sds100.keymapper.domain.repositories.PreferenceRepository
import io.github.sds100.keymapper.util.firstBlocking

/**
 * Created by sds100 on 14/02/21.
 */
//TODO rename as ToggleMappingsUseCase
class ControlKeymapsPausedState(private val preferenceRepository: PreferenceRepository) :
    PreferenceRepository by preferenceRepository {

    private val getKeymapsPausedUseCase = GetKeymapsPausedUseCase(preferenceRepository)

    val keymapsPaused = getKeymapsPausedUseCase()
    fun pauseKeymaps() = run { preferenceRepository.set(Keys.mappingsPaused, true) }
    fun resumeKeymaps() = run { preferenceRepository.set(Keys.mappingsPaused, false) }

    fun toggleKeymaps() {
        if (keymapsPaused.firstBlocking()) {
            resumeKeymaps()
        } else {
            pauseKeymaps()
        }
    }
}