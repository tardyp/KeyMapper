package io.github.sds100.keymapper.ui.mappings.keymap

import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.domain.actions.GetActionErrorUseCase
import io.github.sds100.keymapper.domain.constraints.GetConstraintErrorUseCase
import io.github.sds100.keymapper.domain.mappings.keymap.KeyMap
import io.github.sds100.keymapper.domain.mappings.keymap.KeyMapAction
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.KeyMapTrigger
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.TriggerKeyDevice
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.TriggerMode
import io.github.sds100.keymapper.domain.models.ifIsAllowed
import io.github.sds100.keymapper.domain.utils.ClickType
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.mappings.common.DisplaySimpleMappingUseCase
import io.github.sds100.keymapper.ui.ChipUi
import io.github.sds100.keymapper.ui.actions.ActionUiHelper
import io.github.sds100.keymapper.ui.constraints.ConstraintUiHelper
import io.github.sds100.keymapper.ui.mappings.common.BaseMappingListItemCreator
import io.github.sds100.keymapper.util.KeyEventUtils

/**
 * Created by sds100 on 19/03/2021.
 */
class KeymapListItemCreator(
    private val displayMapping: DisplaySimpleMappingUseCase,
    resourceProvider: ResourceProvider
) : BaseMappingListItemCreator<KeyMap, KeyMapAction>(
    displayMapping,
    KeyMapActionUiHelper(displayMapping, resourceProvider),
    resourceProvider
) {

    fun map(keymap: KeyMap): KeymapListItem.KeymapUiState {
        val midDot = getString(R.string.middot)

        val triggerDescription = buildString {
            val separator = when (keymap.trigger.mode) {
                is TriggerMode.Parallel -> getString(R.string.plus)
                is TriggerMode.Sequence -> getString(R.string.arrow)
                is TriggerMode.Undefined -> null
            }

            val longPressString = getString(R.string.clicktype_long_press)
            val doublePressString = getString(R.string.clicktype_double_press)

            keymap.trigger.keys.forEachIndexed { index, key ->
                if (index > 0) {
                    append("  $separator ")
                }

                when (key.clickType) {
                    ClickType.LONG_PRESS -> append(longPressString)
                    ClickType.DOUBLE_PRESS -> append(doublePressString)
                }

                append(" ${KeyEventUtils.keycodeToString(key.keyCode)}")

                val deviceName = when (key.device) {
                    is TriggerKeyDevice.Internal -> getString(R.string.this_device)
                    is TriggerKeyDevice.Any -> getString(R.string.any_device)
                    is TriggerKeyDevice.External -> key.device.name
                }

                append(" (")

                append(deviceName)

                if (!key.consumeKeyEvent) {
                    append(" $midDot ${getString(R.string.flag_dont_override_default_action)}")
                }

                append(")")
            }
        }

        val optionsDescription = buildString {
            getTriggerOptionLabels(keymap.trigger).forEachIndexed { index, label ->
                if (index != 0) {
                    append(" $midDot ")
                }

                append(label)
            }
        }

        val chipList = getChipList(keymap)

        val extraInfo = buildString {
            if (!keymap.isEnabled) {
                append(getString(R.string.disabled))
            }

            //TODO create separate list of actions and constraints in list item. show "tap constraints" to fix if constraints have errors.
            //TODO chips should show the title of the constraint/action and then a snackbar should be shown when they are tapped
            if (chipList.any { it is ChipUi.FixableError }) {
                if (this.isNotEmpty()) {
                    append(" $midDot ")
                }

                append(getString(R.string.tap_actions_to_fix))
            }

            if (keymap.actionList.isEmpty()) {
                if (this.isNotEmpty()) {
                    append(" $midDot ")
                }

                append(getString(R.string.no_actions))
            }

            if (keymap.trigger.keys.isEmpty()) {
                if (this.isNotEmpty()) {
                    append(" $midDot ")
                }

                append(getString(R.string.no_trigger))
            }
        }

        return KeymapListItem.KeymapUiState(
            uid = keymap.uid,
            chipList = chipList,
            triggerDescription = triggerDescription,
            optionsDescription = optionsDescription,
            extraInfo = extraInfo,
        )
    }

    private fun getTriggerOptionLabels(trigger: KeyMapTrigger): List<String> {
        val labels = mutableListOf<String>()

        if (trigger.isVibrateAllowed() && trigger.vibrate){
            labels.add(getString(R.string.flag_vibrate))
        }

        if (trigger.isLongPressDoubleVibrationAllowed() && trigger.longPressDoubleVibration){
            labels.add(getString(R.string.flag_long_press_double_vibration))
        }

        if (trigger.isDetectingWhenScreenOffAllowed() && trigger.screenOffTrigger){
            labels.add(getString(R.string.flag_detect_triggers_screen_off))
        }

        if (trigger.triggerFromOtherApps){
            labels.add(getString(R.string.flag_trigger_from_other_apps))
        }

        if (trigger.showToast){
            labels.add(getString(R.string.flag_show_toast))
        }

        return labels
    }
}