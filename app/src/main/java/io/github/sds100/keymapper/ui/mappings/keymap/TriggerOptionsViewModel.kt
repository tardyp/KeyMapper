package io.github.sds100.keymapper.ui.mappings.keymap

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.data.model.SliderModel
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.ConfigKeymapTriggerUseCase
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.KeymapTriggerOptions
import io.github.sds100.keymapper.domain.models.Defaultable
import io.github.sds100.keymapper.domain.preferences.PreferenceMinimums
import io.github.sds100.keymapper.domain.usecases.OnboardingUseCase
import io.github.sds100.keymapper.ui.models.CheckBoxListItem
import io.github.sds100.keymapper.ui.models.ListItem
import io.github.sds100.keymapper.ui.models.SliderListItem
import io.github.sds100.keymapper.ui.models.TriggerFromOtherAppsListItem
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.delegate.ModelState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Created by sds100 on 29/11/20.
 */
class TriggerOptionsViewModel(
    private val onboardingUseCase: OnboardingUseCase,
    private val useCase: ConfigKeymapTriggerUseCase
) : ViewModel(), ModelState<List<ListItem>> {

    private companion object {
        const val ID_LONG_PRESS_DELAY = "long_press_delay"
        const val ID_DOUBLE_PRESS_DELAY = "double_press_delay"
        const val ID_SEQUENCE_TRIGGER_TIMEOUT = "sequence_trigger_timeout"
        const val ID_VIBRATE_DURATION = "vibrate_duration"
        const val ID_VIBRATE = "vibrate"
        const val ID_LONG_PRESS_DOUBLE_VIBRATION = "long_press_double_vibration"
        const val ID_SCREEN_OFF_TRIGGER = "screen_off_trigger"
        const val ID_TRIGGER_FROM_OTHER_APPS = "trigger_from_other_apps"
        const val ID_SHOW_TOAST = "show_toast"

        const val KEY_SCREEN_OFF_TRIGGERS = "screen_off_triggers"
    }

    override val model = MutableLiveData<DataState<List<ListItem>>>(Loading())
    override val viewState = MutableLiveData<ViewState>(ViewLoading())

    init {
        useCase.state.onEach { state ->
            if (state is Data) {
                model.value = buildModels(state.data.options, state.data.keymapUid).let {
                    if (it.isEmpty()) {
                        Empty()
                    } else {
                        Data(it)
                    }
                }
            } else {
                model.value = Loading()
            }
        }.launchIn(viewModelScope)
    }

    private fun buildModels(options: KeymapTriggerOptions, keymapUid: String) =
        sequence {
            if (options.triggerFromOtherApps.isAllowed) {
                yield(
                    TriggerFromOtherAppsListItem(
                        id = ID_TRIGGER_FROM_OTHER_APPS,
                        isEnabled = options.triggerFromOtherApps.value,
                        keymapUid = keymapUid,
                        label = R.string.flag_trigger_from_other_apps
                    )
                )
            }

            if (options.showToast.isAllowed) {
                yield(
                    CheckBoxListItem(
                        id = ID_SHOW_TOAST,
                        isChecked = options.showToast.value,
                        label = R.string.flag_show_toast
                    )
                )
            }

            if (options.screenOffTrigger.isAllowed) {
                yield(
                    CheckBoxListItem(
                        id = ID_SCREEN_OFF_TRIGGER,
                        isChecked = options.screenOffTrigger.value,
                        label = R.string.flag_detect_triggers_screen_off
                    )
                )
            }

            if (options.vibrate.isAllowed) {
                yield(
                    CheckBoxListItem(
                        id = ID_VIBRATE,
                        isChecked = options.vibrate.value,
                        label = R.string.flag_vibrate
                    )
                )
            }

            if (options.vibrateDuration.isAllowed) {
                yield(
                    SliderListItem(
                        id = ID_VIBRATE_DURATION,
                        label = R.string.extra_label_vibration_duration,
                        SliderModel(
                            value = options.vibrateDuration.value,
                            isDefaultStepEnabled = true,
                            min = PreferenceMinimums.VIBRATION_DURATION_MIN,
                            max = 1000,
                            stepSize = 5,
                        )
                    )
                )
            }

            if (options.longPressDelay.isAllowed) {
                yield(
                    SliderListItem(
                        id = ID_LONG_PRESS_DELAY,
                        label = R.string.extra_label_long_press_delay_timeout,
                        SliderModel(
                            value = options.longPressDelay.value,
                            isDefaultStepEnabled = true,
                            min = PreferenceMinimums.LONG_PRESS_DELAY_MIN,
                            max = 5000,
                            stepSize = 5,
                        )
                    )
                )
            }

            if (options.longPressDoubleVibration.isAllowed) {
                yield(
                    CheckBoxListItem(
                        id = ID_LONG_PRESS_DOUBLE_VIBRATION,
                        isChecked = options.longPressDoubleVibration.value,
                        label = R.string.flag_long_press_double_vibration
                    )
                )
            }

            if (options.doublePressDelay.isAllowed) {
                yield(
                    SliderListItem(
                        id = ID_DOUBLE_PRESS_DELAY,
                        label = R.string.extra_label_double_press_delay_timeout,
                        SliderModel(
                            value = options.doublePressDelay.value,
                            isDefaultStepEnabled = true,
                            min = PreferenceMinimums.DOUBLE_PRESS_DELAY_MIN,
                            max = 5000,
                            stepSize = 5,
                        )
                    )
                )
            }

            if (options.sequenceTriggerTimeout.isAllowed) {
                yield(
                    SliderListItem(
                        id = ID_SEQUENCE_TRIGGER_TIMEOUT,
                        label = R.string.extra_label_sequence_trigger_timeout,
                        SliderModel(
                            value = options.sequenceTriggerTimeout.value,
                            isDefaultStepEnabled = true,
                            min = PreferenceMinimums.SEQUENCE_TRIGGER_TIMEOUT_MIN,
                            max = 5000,
                            stepSize = 5,
                        )
                    )
                )
            }
        }.toList()

    fun setSliderValue(id: String, value: Defaultable<Int>) {
        when (id) {
            ID_VIBRATE_DURATION -> useCase.setVibrationDuration(value)
        }
    }

    fun setCheckboxValue(id: String, value: Boolean) {
        when (id) {
            ID_VIBRATE -> useCase.setVibrateEnabled(value)
        }
    }

    fun onDialogResponse(key: String, response: UserResponse) {
        when {
            key == KEY_SCREEN_OFF_TRIGGERS && response == UserResponse.POSITIVE ->
                onboardingUseCase.shownScreenOffTriggersExplanation = true
        }
    }

    override fun rebuildModels() {
        TODO("Not yet implemented")
    }
}