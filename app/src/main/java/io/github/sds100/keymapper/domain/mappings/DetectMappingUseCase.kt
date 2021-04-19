package io.github.sds100.keymapper.domain.mappings

import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.domain.adapter.PopupMessageAdapter
import io.github.sds100.keymapper.domain.adapter.VibratorAdapter
import io.github.sds100.keymapper.domain.preferences.Keys
import io.github.sds100.keymapper.domain.preferences.PreferenceDefaults
import io.github.sds100.keymapper.domain.repositories.PreferenceRepository
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Created by sds100 on 17/04/2021.
 */

class DetectMappingUseCaseImpl(
    private val vibrator: VibratorAdapter,
    private val preferenceRepository: PreferenceRepository,
    private val popupMessageAdapter: PopupMessageAdapter,
    private val resourceProvider: ResourceProvider
) : DetectMappingUseCase {

    override val forceVibrate: Flow<Boolean> =
        preferenceRepository.get(Keys.forceVibrate).map { it ?: false }

    override val defaultVibrateDuration: Flow<Long> =
        preferenceRepository.get(Keys.defaultVibrateDuration)
            .map { it ?: PreferenceDefaults.VIBRATION_DURATION }
            .map { it.toLong() }

    override val defaultRepeatRate: Flow<Long> =
        preferenceRepository.get(Keys.defaultRepeatRate)
            .map { it ?: PreferenceDefaults.REPEAT_RATE }
            .map { it.toLong() }

    override val defaultHoldDownDuration: Flow<Long> =
        preferenceRepository.get(Keys.defaultHoldDownDuration)
            .map { it ?: PreferenceDefaults.HOLD_DOWN_DURATION }
            .map { it.toLong() }

    override fun showTriggeredToast() {
        popupMessageAdapter.showPopupMessage(resourceProvider.getString(R.string.toast_triggered_keymap))
    }

    override fun vibrate(duration: Long) {
        vibrator.vibrate(duration)
    }
}

interface DetectMappingUseCase {
    val forceVibrate: Flow<Boolean>
    val defaultVibrateDuration: Flow<Long>
    val defaultRepeatRate: Flow<Long>
    val defaultHoldDownDuration: Flow<Long>

    fun showTriggeredToast()
    fun vibrate(duration: Long)
}