package io.github.sds100.keymapper.mappings

import android.media.MediaPlayer
import io.github.sds100.keymapper.data.Keys
import io.github.sds100.keymapper.data.repositories.PreferenceRepository
import io.github.sds100.keymapper.system.media.MediaAdapter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Created by sds100 on 16/04/2021.
 */

class PauseMappingsUseCaseImpl(
    private val preferenceRepository: PreferenceRepository,
    private val mediaAdapter: MediaAdapter
) : PauseMappingsUseCase {

    override val isPaused: Flow<Boolean> =
        preferenceRepository.get(Keys.mappingsPaused).map { it ?: false }

    override fun pause() {
        preferenceRepository.set(Keys.mappingsPaused, true)
        mediaAdapter.stopMedia()
    }

    override fun resume() {
        preferenceRepository.set(Keys.mappingsPaused, false)
    }
}

interface PauseMappingsUseCase {
    val isPaused: Flow<Boolean>
    fun pause()
    fun resume()
}