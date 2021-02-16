package io.github.sds100.keymapper.domain.usecases

import io.github.sds100.keymapper.domain.preferences.Keys
import io.github.sds100.keymapper.domain.preferences.PreferenceDefaults
import io.github.sds100.keymapper.domain.repositories.PreferenceRepository
import io.github.sds100.keymapper.domain.utils.FlowPrefDelegate
import kotlinx.coroutines.flow.Flow

/**
 * Created by sds100 on 16/02/2021.
 */

internal class DetectFingerprintMapsUseCaseImpl(preferenceRepository: PreferenceRepository) :
    PreferenceRepository by preferenceRepository, DetectFingerprintMapsUseCase {

    override val forceVibrate by FlowPrefDelegate(Keys.forceVibrate, false)

    override val defaultDoublePressDelay by FlowPrefDelegate(
        Keys.defaultDoublePressDelay,
        PreferenceDefaults.DOUBLE_PRESS_DELAY
    )
    override val defaultVibrateDuration by FlowPrefDelegate(
        Keys.defaultVibrateDuration,
        PreferenceDefaults.VIBRATION_DURATION
    )
    override val defaultRepeatDelay by FlowPrefDelegate(
        Keys.defaultRepeatDelay,
        PreferenceDefaults.REPEAT_DELAY
    )
    override val defaultRepeatRate by FlowPrefDelegate(
        Keys.defaultRepeatRate,
        PreferenceDefaults.REPEAT_RATE
    )
}

interface DetectFingerprintMapsUseCase {
    val forceVibrate: Flow<Boolean>
    val defaultVibrateDuration: Flow<Int>
    val defaultDoublePressDelay: Flow<Int>
    val defaultRepeatDelay: Flow<Int>
    val defaultRepeatRate: Flow<Int>
}