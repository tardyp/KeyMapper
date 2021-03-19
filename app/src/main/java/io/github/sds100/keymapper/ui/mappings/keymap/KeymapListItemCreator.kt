package io.github.sds100.keymapper.ui.mappings.keymap

import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.domain.actions.GetActionErrorUseCase
import io.github.sds100.keymapper.domain.constraints.IsConstraintSupportedUseCase
import io.github.sds100.keymapper.domain.mappings.keymap.KeyMap
import io.github.sds100.keymapper.domain.mappings.keymap.KeymapAction
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.KeymapTrigger
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.TriggerKeyDevice
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.TriggerMode
import io.github.sds100.keymapper.domain.models.ifIsAllowed
import io.github.sds100.keymapper.domain.utils.ClickType
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.ui.actions.ActionUiHelper
import io.github.sds100.keymapper.ui.constraints.ConstraintUiHelper
import io.github.sds100.keymapper.ui.mappings.common.BaseMappingListItemCreator
import io.github.sds100.keymapper.util.KeyEventUtils

/**
 * Created by sds100 on 19/03/2021.
 */
class KeymapListItemCreator(
    private val getActionError: GetActionErrorUseCase,
    actionUiHelper: ActionUiHelper<KeymapAction>,
    constraintUiHelper: ConstraintUiHelper,
    isConstraintSupported: IsConstraintSupportedUseCase,
    resourceProvider: ResourceProvider
) : BaseMappingListItemCreator<KeymapAction>(
    getActionError,
    actionUiHelper,
    isConstraintSupported,
    constraintUiHelper,
    resourceProvider
) {

    fun map(
        keymap: KeyMap,
        isSelected: Boolean,
        isSelectable: Boolean
    ): KeymapListItemModel {
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

        val extraInfo = buildString {
            if (!keymap.isEnabled) {
                append(getString(R.string.disabled))
            }

            if (keymap.actionList.any { getActionError.getError(it.data) != null }) {
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

        return KeymapListItemModel(
            uid = keymap.uid,
            chipList = getChipList(keymap.actionList, keymap.constraintList, keymap.constraintMode),
            triggerDescription = triggerDescription,
            optionsDescription = optionsDescription,
            isSelectable = isSelectable,
            isSelected = isSelected,
            extraInfo = extraInfo,
        )
    }

    private fun getTriggerOptionLabels(trigger: KeymapTrigger): List<String> {
        val labels = mutableListOf<String>()

        trigger.options.vibrate.ifIsAllowed {
            if (it) {
                labels.add(getString(R.string.flag_vibrate))
            }
        }

        trigger.options.longPressDoubleVibration.ifIsAllowed {
            if (it) {
                labels.add(getString(R.string.flag_long_press_double_vibration))
            }
        }

        trigger.options.screenOffTrigger.ifIsAllowed {
            if (it) {
                labels.add(getString(R.string.flag_detect_triggers_screen_off))
            }
        }

        trigger.options.triggerFromOtherApps.ifIsAllowed {
            if (it) {
                labels.add(getString(R.string.flag_trigger_from_other_apps))
            }
        }

        trigger.options.showToast.ifIsAllowed {
            if (it) {
                labels.add(getString(R.string.flag_show_toast))
            }
        }

        return labels
    }
}