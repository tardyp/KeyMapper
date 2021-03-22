package io.github.sds100.keymapper.ui.mappings.keymap

import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.data.model.SliderModel
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.ConfigKeymapTriggerState
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.ConfigKeymapTriggerUseCase
import io.github.sds100.keymapper.domain.preferences.PreferenceMinimums
import io.github.sds100.keymapper.domain.usecases.OnboardingUseCase
import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.domain.utils.defaultable.Defaultable
import io.github.sds100.keymapper.framework.adapters.LauncherShortcutAdapter
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.ui.*
import io.github.sds100.keymapper.util.UserResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Created by sds100 on 29/11/20.
 */
class TriggerOptionsViewModel(
    private val coroutineScope: CoroutineScope,
    private val onboardingUseCase: OnboardingUseCase,
    private val useCase: ConfigKeymapTriggerUseCase,
    private val launcherShortcutAdapter: LauncherShortcutAdapter,
    resourceProvider: ResourceProvider
) : UiStateProducer<ListState<ListItem>>, ResourceProvider by resourceProvider {

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

    //TODO screen off triggers explanation

    override val state = MutableStateFlow(buildUiState(State.Loading()))

    private val rebuildUiState = MutableSharedFlow<Unit>()

    init {
        coroutineScope.launch {
            combine(rebuildUiState, useCase.state) { _, configState ->
                configState
            }.collectLatest {
                state.value = buildUiState(it)
            }
        }
    }

    fun setSliderValue(id: String, value: Defaultable<Int>) {
        when (id) {
            ID_VIBRATE_DURATION -> useCase.setVibrationDuration(value)
            ID_LONG_PRESS_DELAY -> useCase.setLongPressDelay(value)
            ID_DOUBLE_PRESS_DELAY -> useCase.setDoublePressDelay(value)
            ID_SEQUENCE_TRIGGER_TIMEOUT -> useCase.setSequenceTriggerTimeout(value)
        }
    }

    fun setCheckboxValue(id: String, value: Boolean) {
        when (id) {
            ID_VIBRATE -> useCase.setVibrateEnabled(value)
            ID_TRIGGER_FROM_OTHER_APPS -> useCase.setTriggerFromOtherAppsEnabled(value)
            ID_LONG_PRESS_DOUBLE_VIBRATION -> useCase.setTriggerFromOtherAppsEnabled(value)
            ID_SHOW_TOAST -> useCase.setShowToastEnabled(value)
            ID_SCREEN_OFF_TRIGGER -> useCase.setTriggerWhenScreenOff(value)
        }
    }

    fun onDialogResponse(key: String, response: UserResponse) {
        when {
            key == KEY_SCREEN_OFF_TRIGGERS && response == UserResponse.POSITIVE ->
                onboardingUseCase.shownScreenOffTriggersExplanation = true
        }
    }

    override fun rebuildUiState() {
        runBlocking { rebuildUiState.emit(Unit) }
    }

    private fun buildUiState(configState: State<ConfigKeymapTriggerState>): ListState<ListItem> {
        return when (configState) {
            is State.Data -> sequence {
                val options = configState.data.options
                val keymapUid = configState.data.keymapUid

                if (options.triggerFromOtherApps.isAllowed) {
                    yield(
                        TriggerFromOtherAppsListItem(
                            id = ID_TRIGGER_FROM_OTHER_APPS,
                            isEnabled = options.triggerFromOtherApps.value,
                            keymapUid = keymapUid,
                            label = getString(R.string.flag_trigger_from_other_apps),
                            areLauncherShortcutsSupported = launcherShortcutAdapter.isSupported
                        )
                    )
                }

                if (options.showToast.isAllowed) {
                    yield(
                        CheckBoxListItem(
                            id = ID_SHOW_TOAST,
                            isChecked = options.showToast.value,
                            label = getString(R.string.flag_show_toast)
                        )
                    )
                }

                if (options.screenOffTrigger.isAllowed) {
                    yield(
                        CheckBoxListItem(
                            id = ID_SCREEN_OFF_TRIGGER,
                            isChecked = options.screenOffTrigger.value,
                            label = getString(R.string.flag_detect_triggers_screen_off)
                        )
                    )
                }

                if (options.vibrate.isAllowed) {
                    yield(
                        CheckBoxListItem(
                            id = ID_VIBRATE,
                            isChecked = options.vibrate.value,
                            label = getString(R.string.flag_vibrate)
                        )
                    )
                }

                if (options.vibrateDuration.isAllowed) {
                    yield(
                        SliderListItem(
                            id = ID_VIBRATE_DURATION,
                            label = getString(R.string.extra_label_vibration_duration),
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
                            label = getString(R.string.extra_label_long_press_delay_timeout),
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
                            label = getString(R.string.flag_long_press_double_vibration)
                        )
                    )
                }

                if (options.doublePressDelay.isAllowed) {
                    yield(
                        SliderListItem(
                            id = ID_DOUBLE_PRESS_DELAY,
                            label = getString(R.string.extra_label_double_press_delay_timeout),
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
                            label = getString(R.string.extra_label_sequence_trigger_timeout),
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
            }.toList().createListState()

            is State.Loading -> ListState.Loading()
        }
    }
}