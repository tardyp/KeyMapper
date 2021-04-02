package io.github.sds100.keymapper.domain.usecases

import io.github.sds100.keymapper.domain.preferences.Keys
import io.github.sds100.keymapper.domain.preferences.PreferenceDefaults
import io.github.sds100.keymapper.domain.repositories.PreferenceRepository
import io.github.sds100.keymapper.domain.utils.FlowPrefDelegate
import kotlinx.coroutines.flow.Flow

/**
 * Created by sds100 on 16/02/2021.
 */

internal class DetectKeymapsUseCaseImpl(preferenceRepository: PreferenceRepository) :
    PreferenceRepository by preferenceRepository, DetectKeymapsUseCase {

    override val forceVibrate by FlowPrefDelegate(Keys.forceVibrate, false)

    override val defaultLongPressDelay by FlowPrefDelegate(
        Keys.defaultLongPressDelay,
        PreferenceDefaults.LONG_PRESS_DELAY
    )
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
    override val defaultSequenceTriggerTimeout by FlowPrefDelegate(
        Keys.defaultSequenceTriggerTimeout,
        PreferenceDefaults.SEQUENCE_TRIGGER_TIMEOUT
    )

    override val defaultHoldDownDuration by FlowPrefDelegate(
        Keys.defaultHoldDownDuration,
        PreferenceDefaults.HOLD_DOWN_DURATION
    )
}

//TODO move to get settings use case
interface DetectKeymapsUseCase {
    val forceVibrate: Flow<Boolean>
    val defaultVibrateDuration: Flow<Int>
    val defaultLongPressDelay: Flow<Int>
    val defaultDoublePressDelay: Flow<Int>
    val defaultRepeatDelay: Flow<Int>
    val defaultRepeatRate: Flow<Int>
    val defaultSequenceTriggerTimeout: Flow<Int>
    val defaultHoldDownDuration: Flow<Int>
}