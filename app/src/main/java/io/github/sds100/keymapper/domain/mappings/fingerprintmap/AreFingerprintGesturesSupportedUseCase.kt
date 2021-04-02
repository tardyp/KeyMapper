package io.github.sds100.keymapper.domain.mappings.fingerprintmap

import android.os.Build
import io.github.sds100.keymapper.domain.preferences.Keys
import io.github.sds100.keymapper.domain.repositories.PreferenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Created by sds100 on 02/04/2021.
 */
class AreFingerprintGesturesSupportedUseCaseImpl(
    preferenceRepository: PreferenceRepository
) : AreFingerprintGesturesSupportedUseCase {
    override val isSupported: Flow<Boolean> =
        preferenceRepository.get(Keys.fingerprintGesturesAvailable).map {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return@map false

            it ?: false
        }
}

interface AreFingerprintGesturesSupportedUseCase {
    val isSupported: Flow<Boolean>
}