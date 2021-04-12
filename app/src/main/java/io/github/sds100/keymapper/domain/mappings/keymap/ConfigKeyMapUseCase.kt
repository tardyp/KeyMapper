package io.github.sds100.keymapper.domain.mappings.keymap

import io.github.sds100.keymapper.constraints.ConstraintState
import io.github.sds100.keymapper.domain.actions.ActionData
import io.github.sds100.keymapper.domain.actions.KeyEventAction
import io.github.sds100.keymapper.domain.adapter.ExternalDevicesAdapter
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.KeyMapTrigger
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.TriggerKey
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.TriggerKeyDevice
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.TriggerMode
import io.github.sds100.keymapper.domain.utils.*
import io.github.sds100.keymapper.mappings.common.BaseConfigMappingUseCase
import io.github.sds100.keymapper.mappings.common.ConfigMappingUseCase
import io.github.sds100.keymapper.util.KeyEventUtils

/**
 * Created by sds100 on 16/02/2021.
 */
class ConfigKeyMapUseCaseImpl(
    private val externalDevicesAdapter: ExternalDevicesAdapter
) : BaseConfigMappingUseCase<KeyMapAction, KeyMap>(), ConfigKeyMapUseCase {

    override fun addTriggerKey(
        keyCode: Int,
        device: TriggerKeyDevice
    ) = editTrigger { trigger ->
        val clickType = when (trigger.mode) {
            is TriggerMode.Parallel -> trigger.mode.clickType
            TriggerMode.Sequence -> ClickType.SHORT_PRESS
            TriggerMode.Undefined -> ClickType.SHORT_PRESS
        }

        val containsKey = trigger.keys.any { keyToCompare ->
            if (trigger.mode != TriggerMode.Sequence) {
                val sameKeyCode = keyCode == keyToCompare.keyCode

                //if the new key is not external, check whether a trigger key already exists for this device
                val sameDevice = when {
                    keyToCompare.device is TriggerKeyDevice.External
                        && device is TriggerKeyDevice.External ->
                        keyToCompare.device.descriptor == device.descriptor

                    else -> true
                }

                sameKeyCode && sameDevice

            } else {
                false
            }
        }

        val newKeys = trigger.keys.toMutableList().apply {

            val triggerKey = TriggerKey(
                keyCode = keyCode,
                device = device,
                clickType = clickType
            )

            add(triggerKey)
        }

        val newMode = when {
            containsKey -> TriggerMode.Sequence
            newKeys.size <= 1 -> TriggerMode.Undefined

            /* Automatically make it a parallel trigger when the user makes a trigger with more than one key
            because this is what most users are expecting when they make a trigger with multiple keys */
            newKeys.size == 2 && !containsKey -> TriggerMode.Parallel(clickType)
            else -> trigger.mode
        }

        trigger.copy(keys = newKeys, mode = newMode)
    }

    override fun removeTriggerKey(uid: String) = editTrigger { trigger ->
        val newKeys = trigger.keys.toMutableList().apply {
            removeAll { it.uid == uid }
        }

        val newMode = when {
            newKeys.size <= 1 -> TriggerMode.Undefined
            else -> trigger.mode
        }

        trigger.copy(keys = newKeys, mode = newMode)
    }

    override fun moveTriggerKey(fromIndex: Int, toIndex: Int) = editTrigger { trigger ->
        trigger.copy(
            keys = trigger.keys.toMutableList().apply {
                moveElement(fromIndex, toIndex)
            }
        )
    }

    override fun setParallelTriggerMode() = editTrigger { trigger ->
        if (trigger.mode is TriggerMode.Parallel) return@editTrigger trigger

        //undefined mode only allowed if one or no keys
        if (trigger.keys.size <= 1) {
            return@editTrigger trigger.copy(mode = TriggerMode.Undefined)
        }

        val oldKeys = trigger.keys
        var newKeys = oldKeys.toMutableList()

        if (trigger.mode !is TriggerMode.Parallel) {
            // set all the keys to a short press if coming from a non-parallel trigger
            // because they must all be the same click type and can't all be double pressed
            newKeys = newKeys.map { key ->
                key.copy(clickType = ClickType.SHORT_PRESS)
            }.toMutableList()

            //remove duplicates of keys that have the same keycode and device id
            newKeys =
                newKeys.distinctBy { Pair(it.keyCode, it.device) }.toMutableList()
        }

        val newMode = if (newKeys.size <= 1){
            TriggerMode.Undefined
        }else{
            TriggerMode.Parallel(newKeys[0].clickType)
        }

        trigger.copy(keys = newKeys, mode = newMode)
    }

    override fun setSequenceTriggerMode() = editTrigger { trigger ->
        if (trigger.mode == TriggerMode.Sequence) return@editTrigger trigger
        //undefined mode only allowed if one or no keys
        if (trigger.keys.size <= 1) {
            return@editTrigger trigger.copy(mode = TriggerMode.Undefined)
        }

        trigger.copy(mode = TriggerMode.Sequence)
    }

    override fun setUndefinedTriggerMode() = editTrigger { trigger ->
        if (trigger.mode == TriggerMode.Undefined) return@editTrigger trigger

        //undefined mode only allowed if one or no keys
        if (trigger.keys.size > 1) {
            return@editTrigger trigger
        }

        trigger.copy(mode = TriggerMode.Undefined)
    }

    override fun setTriggerShortPress() {
        editTrigger { oldTrigger ->
            if (oldTrigger.mode == TriggerMode.Sequence) {
                return@editTrigger oldTrigger
            }

            val newKeys = oldTrigger.keys.map { it.copy(clickType = ClickType.SHORT_PRESS) }
            val newMode = if (newKeys.size <= 1) {
                TriggerMode.Undefined
            } else {
                TriggerMode.Parallel(ClickType.SHORT_PRESS)
            }
            oldTrigger.copy(keys = newKeys, mode = newMode)
        }
    }

    override fun setTriggerLongPress() {
        editTrigger { oldTrigger ->
            if (oldTrigger.mode == TriggerMode.Sequence) {
                return@editTrigger oldTrigger
            }

            val newKeys = oldTrigger.keys.map { it.copy(clickType = ClickType.LONG_PRESS) }
            val newMode = if (newKeys.size <= 1) {
                TriggerMode.Undefined
            } else {
                TriggerMode.Parallel(ClickType.LONG_PRESS)
            }

            oldTrigger.copy(keys = newKeys, mode = newMode)
        }
    }

    override fun setTriggerDoublePress() {
        editTrigger { oldTrigger ->
            if (oldTrigger.mode != TriggerMode.Undefined) {
                return@editTrigger oldTrigger
            }

            val newKeys = oldTrigger.keys.map { it.copy(clickType = ClickType.DOUBLE_PRESS) }
            val newMode = TriggerMode.Undefined

            oldTrigger.copy(keys = newKeys, mode = newMode)
        }
    }

    override fun setTriggerKeyClickType(keyUid: String, clickType: ClickType) {
        editTriggerKey(keyUid) {
            it.copy(clickType = clickType)
        }
    }

    override fun setTriggerKeyDevice(keyUid: String, device: TriggerKeyDevice) {
        editTriggerKey(keyUid) {
            it.copy(device = device)
        }
    }

    override fun setTriggerKeyConsumeKeyEvent(keyUid: String, consumeKeyEvent: Boolean) {
        editTriggerKey(keyUid) {
            it.copy(consumeKeyEvent = consumeKeyEvent)
        }
    }

    override fun setVibrateEnabled(enabled: Boolean) = editTrigger { it.copy(vibrate = enabled) }

    override fun setVibrationDuration(duration: Defaultable<Int>) =
        editTrigger { it.copy(vibrateDuration = duration.nullIfDefault()) }

    override fun setLongPressDelay(delay: Defaultable<Int>) =
        editTrigger { it.copy(longPressDelay = delay.nullIfDefault()) }

    override fun setDoublePressDelay(delay: Defaultable<Int>) {
        editTrigger { it.copy(doublePressDelay = delay.nullIfDefault()) }
    }

    override fun setSequenceTriggerTimeout(delay: Defaultable<Int>) {
        editTrigger { it.copy(sequenceTriggerTimeout = delay.nullIfDefault()) }
    }

    override fun setLongPressDoubleVibrationEnabled(enabled: Boolean) {
        editTrigger { it.copy(longPressDoubleVibration = enabled) }
    }

    override fun setTriggerWhenScreenOff(enabled: Boolean) {
        editTrigger { it.copy(screenOffTrigger = enabled) }
    }

    override fun setTriggerFromOtherAppsEnabled(enabled: Boolean) {
        editTrigger { it.copy(triggerFromOtherApps = enabled) }
    }

    override fun setShowToastEnabled(enabled: Boolean) {
        editTrigger { it.copy(showToast = enabled) }
    }

    override fun getAvailableTriggerKeyDevices(): List<TriggerKeyDevice> {
        val externalDevices = sequence {
            val inputDevices =
                externalDevicesAdapter.inputDevices.value.dataOrNull() ?: return@sequence

            inputDevices.forEach {
                yield(
                    TriggerKeyDevice.External(it.descriptor, it.name)
                )
            }
        }

        return sequence {
            yield(TriggerKeyDevice.Internal)
            yield(TriggerKeyDevice.Any)
            yieldAll(externalDevices)
        }.toList()
    }

    override fun setEnabled(enabled: Boolean) {
        editKeymap { it.copy(isEnabled = enabled) }
    }

    override fun setActionRepeatEnabled(uid: String, repeat: Boolean) =
        setActionOption(uid) { it.copy(repeat = repeat) }

    override fun setActionRepeatRate(uid: String, repeatRate: Int?) =
        setActionOption(uid) { it.copy(repeatRate = repeatRate) }

    override fun setActionRepeatDelay(uid: String, repeatDelay: Int?) =
        setActionOption(uid) { it.copy(repeatDelay = repeatDelay) }

    override fun setActionHoldDownEnabled(uid: String, holdDown: Boolean) =
        setActionOption(uid) { it.copy(holdDown = holdDown) }

    override fun setActionHoldDownDuration(uid: String, holdDownDuration: Int?) =
        setActionOption(uid) { it.copy(holdDownDuration = holdDownDuration) }

    override fun setActionStopRepeatingWhenTriggerPressedAgain(uid: String, enabled: Boolean) =
        setActionOption(uid) { it.copy(stopRepeatingWhenTriggerPressedAgain = enabled) }

    override fun setActionStopHoldingDownWhenTriggerPressedAgain(uid: String, enabled: Boolean) =
        setActionOption(uid) { it.copy(stopHoldDownWhenTriggerPressedAgain = enabled) }

    override fun setActionMultiplier(uid: String, multiplier: Int?) =
        setActionOption(uid) { it.copy(multiplier = multiplier) }

    override fun setDelayBeforeNextAction(uid: String, delay: Int?) =
        setActionOption(uid) { it.copy(delayBeforeNextAction = delay) }

    override fun createAction(data: ActionData): KeyMapAction {
        var holdDown = false
        var repeat = false

        if (data is KeyEventAction) {
            if (KeyEventUtils.isModifierKey(data.keyCode)) {
                holdDown = true
                repeat = true
            } else {
                repeat = true
            }
        }

        return KeyMapAction(
            data = data,
            repeat = repeat,
            holdDown = holdDown
        )
    }

    override fun setActionList(actionList: List<KeyMapAction>) {
        editKeymap { it.copy(actionList = actionList) }
    }

    override fun setConstraintState(constraintState: ConstraintState) {
        editKeymap { it.copy(constraintState = constraintState) }
    }

    private fun setActionOption(
        uid: String,
        block: (action: KeyMapAction) -> KeyMapAction
    ) {
        editKeymap { keyMap ->
            val newActionList = keyMap.actionList.map { action ->
                if (action.uid == uid) {
                    block.invoke(action)
                } else {
                    action
                }
            }

            keyMap.copy(
                actionList = newActionList
            )
        }
    }

    private fun editTrigger(block: (trigger: KeyMapTrigger) -> KeyMapTrigger) {
        mapping.value.ifIsData { keyMap ->
            val newTrigger = block(keyMap.trigger)

            setMapping(keyMap.copy(trigger = newTrigger))
        }
    }

    private fun editTriggerKey(uid: String, block: (key: TriggerKey) -> TriggerKey) {
        editTrigger { oldTrigger ->
            val newKeys = oldTrigger.keys.map {
                if (it.uid == uid) {
                    block.invoke(it)
                } else {
                    it
                }
            }

            oldTrigger.copy(keys = newKeys)
        }
    }

    private fun editKeymap(block: (keymap: KeyMap) -> KeyMap) {
        mapping.value.ifIsData { setMapping(block.invoke(it)) }
    }
}

interface ConfigKeyMapUseCase : ConfigMappingUseCase<KeyMapAction, KeyMap> {
    //trigger
    fun addTriggerKey(keyCode: Int, device: TriggerKeyDevice)
    fun removeTriggerKey(uid: String)
    fun moveTriggerKey(fromIndex: Int, toIndex: Int)

    fun setParallelTriggerMode()
    fun setSequenceTriggerMode()
    fun setUndefinedTriggerMode()

    fun setTriggerShortPress()
    fun setTriggerLongPress()
    fun setTriggerDoublePress()

    fun setTriggerKeyClickType(keyUid: String, clickType: ClickType)
    fun setTriggerKeyDevice(keyUid: String, device: TriggerKeyDevice)
    fun setTriggerKeyConsumeKeyEvent(keyUid: String, consumeKeyEvent: Boolean)

    fun setVibrateEnabled(enabled: Boolean)
    fun setVibrationDuration(duration: Defaultable<Int>)
    fun setLongPressDelay(delay: Defaultable<Int>)
    fun setDoublePressDelay(delay: Defaultable<Int>)
    fun setSequenceTriggerTimeout(delay: Defaultable<Int>)
    fun setLongPressDoubleVibrationEnabled(enabled: Boolean)
    fun setTriggerWhenScreenOff(enabled: Boolean)
    fun setTriggerFromOtherAppsEnabled(enabled: Boolean)
    fun setShowToastEnabled(enabled: Boolean)

    fun getAvailableTriggerKeyDevices(): List<TriggerKeyDevice>

    //actions
    fun setActionRepeatEnabled(uid: String, repeat: Boolean)
    fun setActionRepeatRate(uid: String, repeatRate: Int?)
    fun setActionRepeatDelay(uid: String, repeatDelay: Int?)
    fun setActionHoldDownEnabled(uid: String, holdDown: Boolean)
    fun setActionHoldDownDuration(uid: String, holdDownDuration: Int?)
    fun setActionStopRepeatingWhenTriggerPressedAgain(uid: String, enabled: Boolean)
    fun setActionStopHoldingDownWhenTriggerPressedAgain(uid: String, enabled: Boolean)
}