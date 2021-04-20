package io.github.sds100.keymapper.mappings.keymaps

import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.mappings.BaseMappingListItemCreator
import io.github.sds100.keymapper.mappings.ClickType
import io.github.sds100.keymapper.mappings.keymaps.trigger.KeyMapTrigger
import io.github.sds100.keymapper.mappings.keymaps.trigger.KeyMapTriggerError
import io.github.sds100.keymapper.mappings.keymaps.trigger.TriggerKeyDevice
import io.github.sds100.keymapper.mappings.keymaps.trigger.TriggerMode
import io.github.sds100.keymapper.system.keyevents.KeyEventUtils
import io.github.sds100.keymapper.system.permissions.Permission
import io.github.sds100.keymapper.util.result.FixableError
import io.github.sds100.keymapper.util.ui.ChipUi
import io.github.sds100.keymapper.util.ui.ResourceProvider

/**
 * Created by sds100 on 19/03/2021.
 */
class KeyMapListItemCreator(
    private val displayMapping: DisplayKeyMapUseCase,
    resourceProvider: ResourceProvider
) : BaseMappingListItemCreator<KeyMap, KeyMapAction>(
    displayMapping,
    KeyMapActionUiHelper(displayMapping, resourceProvider),
    resourceProvider
) {

    fun map(keyMap: KeyMap): KeyMapListItem.KeyMapUiState {
        val midDot = getString(R.string.middot)

        val triggerDescription = buildString {
            val separator = when (keyMap.trigger.mode) {
                is TriggerMode.Parallel -> getString(R.string.plus)
                is TriggerMode.Sequence -> getString(R.string.arrow)
                is TriggerMode.Undefined -> null
            }

            val longPressString = getString(R.string.clicktype_long_press)
            val doublePressString = getString(R.string.clicktype_double_press)

            keyMap.trigger.keys.forEachIndexed { index, key ->
                if (index > 0) {
                    append(" $separator ")
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
            getTriggerOptionLabels(keyMap.trigger).forEachIndexed { index, label ->
                if (index != 0) {
                    append(" $midDot ")
                }

                append(label)
            }
        }

        val actionChipList = getActionChipList(keyMap)
        val constraintChipList = getConstraintChipList(keyMap)

        val extraInfo = buildString {
            append(createExtraInfoString(keyMap, actionChipList, constraintChipList))

            if (keyMap.trigger.keys.isEmpty()) {
                if (this.isNotEmpty()) {
                    append(" $midDot ")
                }

                append(getString(R.string.no_trigger))
            }
        }

        val triggerErrors = displayMapping.getTriggerErrors(keyMap.trigger)

        val triggerErrorChips = triggerErrors.map {
            when (it) {
                KeyMapTriggerError.DND_ACCESS_DENIED ->
                    ChipUi.FixableError(
                        id = KeyMapTriggerError.DND_ACCESS_DENIED.toString(),
                        text = getString(R.string.trigger_error_dnd_access_denied_short),
                        error = FixableError.PermissionDenied(Permission.ACCESS_NOTIFICATION_POLICY)
                    )

                KeyMapTriggerError.SCREEN_OFF_ROOT_DENIED -> ChipUi.FixableError(
                    id = KeyMapTriggerError.SCREEN_OFF_ROOT_DENIED.toString(),
                    text = getString(R.string.trigger_error_screen_off_root_permission_denied_short),
                    error = FixableError.PermissionDenied(Permission.ROOT)
                )
            }
        }

        return KeyMapListItem.KeyMapUiState(
            uid = keyMap.uid,
            actionChipList = actionChipList,
            constraintChipList = constraintChipList,
            triggerDescription = triggerDescription,
            optionsDescription = optionsDescription,
            extraInfo = extraInfo,
            triggerErrorChipList = triggerErrorChips
        )
    }

    private fun getTriggerOptionLabels(trigger: KeyMapTrigger): List<String> {
        val labels = mutableListOf<String>()

        if (trigger.isVibrateAllowed() && trigger.vibrate) {
            labels.add(getString(R.string.flag_vibrate))
        }

        if (trigger.isLongPressDoubleVibrationAllowed() && trigger.longPressDoubleVibration) {
            labels.add(getString(R.string.flag_long_press_double_vibration))
        }

        if (trigger.isDetectingWhenScreenOffAllowed() && trigger.screenOffTrigger) {
            labels.add(getString(R.string.flag_detect_triggers_screen_off))
        }

        if (trigger.triggerFromOtherApps) {
            labels.add(getString(R.string.flag_trigger_from_other_apps))
        }

        if (trigger.showToast) {
            labels.add(getString(R.string.flag_show_toast))
        }

        return labels
    }
}