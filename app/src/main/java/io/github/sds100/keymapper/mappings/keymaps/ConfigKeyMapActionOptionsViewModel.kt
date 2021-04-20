package io.github.sds100.keymapper.mappings.keymaps

import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.util.ui.SliderModel
import io.github.sds100.keymapper.util.Defaultable
import io.github.sds100.keymapper.util.ui.ResourceProvider
import io.github.sds100.keymapper.mappings.ConfigActionOptionsViewModel
import io.github.sds100.keymapper.mappings.OptionMinimums
import io.github.sds100.keymapper.mappings.isDelayBeforeNextActionAllowed
import io.github.sds100.keymapper.util.ui.*
import kotlinx.coroutines.CoroutineScope

/**
 * Created by sds100 on 27/06/20.
 */
class ConfigKeyMapActionOptionsViewModel(
    coroutineScope: CoroutineScope,
    val config: ConfigKeyMapUseCase,
    resourceProvider: ResourceProvider
) : ConfigActionOptionsViewModel<KeyMap, KeyMapAction>(coroutineScope, config),
    ResourceProvider by resourceProvider {

    companion object {
        private const val ID_REPEAT_RATE = "repeat_rate"
        private const val ID_REPEAT = "repeat"
        private const val ID_STOP_REPEATING_TRIGGER_RELEASED = "stop_repeating_trigger_released"
        private const val ID_STOP_REPEATING_TRIGGER_PRESSED_AGAIN =
            "stop_repeating_trigger_pressed_again"

        private const val ID_MULTIPLIER = "multiplier"
        private const val ID_REPEAT_DELAY = "repeat_delay"
        private const val ID_HOLD_DOWN = "hold_down"
        private const val ID_DELAY_BEFORE_NEXT_ACTION = "delay_before_next_action"
        private const val ID_HOLD_DOWN_DURATION = "hold_down_duration"
        private const val ID_STOP_HOLD_DOWN_WHEN_TRIGGER_RELEASED =
            "stop_hold_down_when_trigger_released"
        private const val ID_STOP_HOLD_DOWN_WHEN_TRIGGER_PRESSED_AGAIN =
            "stop_hold_down_when_trigger_pressed_again"

    }

    override fun setRadioButtonValue(id: String, value: Boolean) {
        val actionUid = actionUid.value ?: return

        when (id) {
            ID_STOP_REPEATING_TRIGGER_RELEASED -> config.setActionStopRepeatingWhenTriggerPressedAgain(
                actionUid,
                !value
            )

            ID_STOP_REPEATING_TRIGGER_PRESSED_AGAIN -> config.setActionStopRepeatingWhenTriggerPressedAgain(
                actionUid,
                value
            )

            ID_STOP_HOLD_DOWN_WHEN_TRIGGER_RELEASED -> config.setActionStopHoldingDownWhenTriggerPressedAgain(
                actionUid,
                !value
            )

            ID_STOP_HOLD_DOWN_WHEN_TRIGGER_PRESSED_AGAIN -> config.setActionStopHoldingDownWhenTriggerPressedAgain(
                actionUid,
                value
            )
        }
    }

    override fun setSliderValue(id: String, value: Defaultable<Int>) {
        val actionUid = actionUid.value ?: return

        when (id) {
            ID_REPEAT_DELAY -> config.setActionRepeatDelay(actionUid, value.nullIfDefault())
            ID_REPEAT_RATE -> config.setActionRepeatRate(actionUid, value.nullIfDefault())
            ID_HOLD_DOWN_DURATION -> config.setActionHoldDownDuration(
                actionUid,
                value.nullIfDefault()
            )
            ID_MULTIPLIER -> config.setActionMultiplier(actionUid, value.nullIfDefault())
            ID_DELAY_BEFORE_NEXT_ACTION -> config.setDelayBeforeNextAction(
                actionUid,
                value.nullIfDefault()
            )
        }
    }

    override fun setCheckboxValue(id: String, value: Boolean) {
        val actionUid = actionUid.value ?: return

        when (id) {
            ID_REPEAT -> config.setActionRepeatEnabled(actionUid, value)
            ID_HOLD_DOWN -> config.setActionHoldDownEnabled(actionUid, value)
        }
    }


    override fun createListItems(keyMap: KeyMap, action: KeyMapAction): List<ListItem> {
        return sequence {

            if (keyMap.isRepeatingActionsAllowed()) {
                yield(
                    CheckBoxListItem(
                        id = ID_REPEAT,
                        label = getString(R.string.flag_repeat_actions),
                        isChecked = action.repeat
                    )
                )
            }

            if (keyMap.isChangingActionRepeatRateAllowed(action)) {
                yield(
                    SliderListItem(
                        id = ID_REPEAT_RATE,
                        label = getString(R.string.extra_label_repeat_rate),
                        sliderModel = SliderModel(
                            value = Defaultable.create(action.repeatRate),
                            isDefaultStepEnabled = true,
                            min = OptionMinimums.ACTION_REPEAT_RATE,
                            max = SliderMaximums.ACTION_REPEAT_RATE,
                            stepSize = SliderStepSizes.ACTION_REPEAT_RATE
                        )
                    )
                )
            }

            if (keyMap.isChangingActionRepeatDelayAllowed(action)) {
                yield(
                    SliderListItem(
                        id = ID_REPEAT_DELAY,
                        label = getString(R.string.extra_label_repeat_delay),
                        sliderModel = SliderModel(
                            value = Defaultable.create(action.repeatDelay),
                            isDefaultStepEnabled = true,
                            min = KeyMapAction.REPEAT_DELAY_MIN,
                            max = 5000,
                            stepSize = 5
                        )
                    )
                )
            }

            if (keyMap.isStopRepeatingActionWhenTriggerPressedAgainAllowed(action)) {
                yield(
                    RadioButtonPairListItem(
                        id = "repeat_mode",
                        header = getString(R.string.stop_repeating_dot_dot_dot),

                        leftButtonId = ID_STOP_REPEATING_TRIGGER_RELEASED,
                        leftButtonText = getString(R.string.trigger_released),
                        leftButtonChecked = !action.stopRepeatingWhenTriggerPressedAgain,

                        rightButtonId = ID_STOP_REPEATING_TRIGGER_PRESSED_AGAIN,
                        rightButtonText = getString(R.string.trigger_pressed_again),
                        rightButtonChecked = action.stopRepeatingWhenTriggerPressedAgain
                    )
                )
            }

            if (keyMap.isHoldingDownActionAllowed(action)) {
                yield(DividerListItem("hold_down_divider"))

                yield(
                    CheckBoxListItem(
                        id = ID_HOLD_DOWN,
                        label = getString(R.string.flag_hold_down),
                        isChecked = action.holdDown
                    )
                )
            }

            if (keyMap.isHoldingDownActionBeforeRepeatingAllowed(action)) {
                yield(
                    SliderListItem(
                        id = ID_HOLD_DOWN_DURATION,
                        label = getString(R.string.extra_label_hold_down_duration),
                        sliderModel = SliderModel(
                            value = Defaultable.create(action.holdDownDuration),
                            isDefaultStepEnabled = true,
                            min = OptionMinimums.ACTION_HOLD_DOWN_DURATION,
                            max = SliderMaximums.ACTION_HOLD_DOWN_DURATION,
                            stepSize = SliderStepSizes.ACTION_HOLD_DOWN_DURATION
                        )
                    )
                )
            }

            if (keyMap.isStopHoldingDownActionWhenTriggerPressedAgainAllowed(action)) {
                yield(
                    RadioButtonPairListItem(
                        id = "hold_down_mode",
                        header = getString(R.string.hold_down_until_trigger_is_dot_dot_dot),

                        leftButtonId = ID_STOP_HOLD_DOWN_WHEN_TRIGGER_RELEASED,
                        leftButtonText = getString(R.string.trigger_released),
                        leftButtonChecked = !action.stopHoldDownWhenTriggerPressedAgain,

                        rightButtonId = ID_STOP_HOLD_DOWN_WHEN_TRIGGER_PRESSED_AGAIN,
                        rightButtonText = getString(R.string.trigger_pressed_again),
                        rightButtonChecked = action.stopHoldDownWhenTriggerPressedAgain
                    )
                )
            }

            if (keyMap.isDelayBeforeNextActionAllowed()) {
                yield(DividerListItem("other_divider"))

                yield(
                    SliderListItem(
                        id = ID_DELAY_BEFORE_NEXT_ACTION,
                        label = getString(R.string.extra_label_delay_before_next_action),
                        sliderModel = SliderModel(
                            value = Defaultable.create(action.delayBeforeNextAction),
                            isDefaultStepEnabled = true,
                            min = OptionMinimums.DELAY_BEFORE_NEXT_ACTION,
                            max = SliderMaximums.DELAY_BEFORE_NEXT_ACTION,
                            stepSize = SliderStepSizes.DELAY_BEFORE_NEXT_ACTION
                        )
                    )
                )
            }

            yield(
                SliderListItem(
                    id = ID_MULTIPLIER,
                    label = getString(R.string.extra_label_action_multiplier),
                    sliderModel = SliderModel(
                        value = Defaultable.create(action.multiplier),
                        isDefaultStepEnabled = true,
                        min = OptionMinimums.ACTION_MULTIPLIER,
                        max = SliderMaximums.ACTION_MULTIPLIER,
                        stepSize = SliderStepSizes.ACTION_MULTIPLIER
                    )
                )
            )

        }.toList()
    }
}

