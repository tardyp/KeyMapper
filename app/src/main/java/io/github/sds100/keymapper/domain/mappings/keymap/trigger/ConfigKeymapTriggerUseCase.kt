package io.github.sds100.keymapper.domain.mappings.keymap.trigger

import io.github.sds100.keymapper.domain.mappings.keymap.KeyMap
import io.github.sds100.keymapper.domain.models.Defaultable
import io.github.sds100.keymapper.domain.utils.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

/**
 * Created by sds100 on 15/02/2021.
 */
class ConfigKeymapTriggerUseCaseImpl(
    private val keymapFlow: StateFlow<State<KeyMap>>,
    val setKeymap: (keymap: KeyMap) -> Unit
) : ConfigKeymapTriggerUseCase {

    override val state = keymapFlow.map { state ->
        state.mapData {
            ConfigKeymapTriggerState(
                keymapUid = it.uid,
                keys = it.trigger.keys,
                mode = it.trigger.mode,
                options = it.trigger.options
            )
        }
    }

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

    override fun setParallelTriggerMode() = keymapFlow.value.ifIsData { keymap ->
        val trigger = keymap.trigger

        if (trigger.mode is TriggerMode.Parallel) return

        //undefined mode only allowed if one or no keys
        if (trigger.keys.size <= 1) {
            editTrigger { it.copy(mode = TriggerMode.Undefined) }
            return@ifIsData
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

        editTrigger { it.copy(keys = newKeys, mode = TriggerMode.Parallel(ClickType.SHORT_PRESS)) }
    }

    override fun setSequenceTriggerMode() = editTrigger { trigger ->
        //undefined mode only allowed if one or no keys
        if (trigger.keys.size <= 1) {
            return@editTrigger trigger.copy(mode = TriggerMode.Undefined)
        }

        trigger.copy(mode = TriggerMode.Sequence)
    }

    override fun setUndefinedTriggerMode() = editTrigger { trigger ->
        //undefined mode only allowed if one or no keys
        if (trigger.keys.size > 1) {
            return@editTrigger trigger
        }

        trigger.copy(mode = TriggerMode.Undefined)
    }

    override fun setParallelTriggerClickType(clickType: ClickType) {
        editTrigger { oldTrigger ->
            if (clickType == ClickType.DOUBLE_PRESS) return@editTrigger oldTrigger

            val newKeys = oldTrigger.keys.map { it.copy(clickType = clickType) }
            val newMode = TriggerMode.Parallel(clickType)

            oldTrigger.copy(keys = newKeys, mode = newMode)
        }
    }

    override fun setTriggerKeyDevice(keyUid: String, device: TriggerKeyDevice) {

    }

    override fun setVibrateEnabled(enabled: Boolean) = editTrigger { it.copy(vibrate = enabled) }

    override fun setVibrationDuration(duration: Defaultable<Int>) =
        editTrigger { it.copy(vibrateDuration = duration) }

    override fun setLongPressDelay(delay: Defaultable<Int>) =
        editTrigger { it.copy(longPressDelay = delay) }

    private fun editTrigger(block: (trigger: KeymapTrigger) -> KeymapTrigger) {
        keymapFlow.value.ifIsData { keymap ->
            setKeymap.invoke(keymap.copy(trigger = block(keymap.trigger)))
        }
    }
}

interface ConfigKeymapTriggerUseCase {
    val state: Flow<State<ConfigKeymapTriggerState>>

    fun addTriggerKey(keyCode: Int, device: TriggerKeyDevice)
    fun removeTriggerKey(uid: String)
    fun moveTriggerKey(fromIndex: Int, toIndex: Int)

    fun setParallelTriggerMode()
    fun setSequenceTriggerMode()
    fun setUndefinedTriggerMode()
    fun setParallelTriggerClickType(clickType: ClickType)
    fun setTriggerKeyDevice(keyUid: String, device: TriggerKeyDevice)

    fun setVibrateEnabled(enabled: Boolean)
    fun setVibrationDuration(duration: Defaultable<Int>)
    fun setLongPressDelay(delay: Defaultable<Int>)
}

data class ConfigKeymapTriggerState(
    val keymapUid: String,
    val keys: List<TriggerKey>,
    val mode: TriggerMode,
    val options: KeymapTriggerOptions
)