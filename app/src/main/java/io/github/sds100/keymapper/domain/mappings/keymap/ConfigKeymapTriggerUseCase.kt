package io.github.sds100.keymapper.domain.mappings.keymap

import io.github.sds100.keymapper.domain.models.Defaultable
import io.github.sds100.keymapper.domain.models.KeymapTrigger
import io.github.sds100.keymapper.domain.models.KeymapTriggerOptions
import io.github.sds100.keymapper.domain.models.TriggerKey
import io.github.sds100.keymapper.domain.repositories.PreferenceRepository
import io.github.sds100.keymapper.domain.trigger.TriggerKeyDevice
import io.github.sds100.keymapper.domain.trigger.TriggerMode
import io.github.sds100.keymapper.domain.utils.ClickType
import io.github.sds100.keymapper.util.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.*

/**
 * Created by sds100 on 15/02/2021.
 */
class ConfigKeymapTriggerUseCaseImpl() : ConfigKeymapTriggerUseCase {

    private val trigger = MutableStateFlow<DataState<KeymapTrigger>>(Loading())

    override val keys = trigger.map { state -> state.mapData { it.keys } }
    override val mode = trigger.map { state ->
        when (state) {
            is Data -> state.data.mode
            else -> TriggerMode.UNDEFINED
        }
    }

    override val options = trigger.map { state -> state.mapData { it.options } }

    override fun setMode(newMode: TriggerMode) {
        when (newMode) {
            TriggerMode.PARALLEL -> {
                trigger.value.ifIsData { data ->
                    val oldKeys = data.keys
                    var newKeys = oldKeys.toMutableList()

                    if (data.mode != TriggerMode.PARALLEL) {

                        if (newKeys.isNotEmpty()) {
                            // set all the keys to a short press if coming from a non-parallel trigger
                            // because they must all be the same click type and can't all be double pressed
                            newKeys = newKeys.map { key ->
                                key.copy(clickType = ClickType.SHORT_PRESS)
                            }.toMutableList()

                            //remove duplicates of keys that have the same keycode and device id
                            newKeys =
                                newKeys.distinctBy { Pair(it.keyCode, it.device) }.toMutableList()
                        }
                    }

                    trigger.value = Data(data.copy(keys = newKeys, mode = TriggerMode.PARALLEL))
                }
            }

            TriggerMode.SEQUENCE -> trigger.value.ifIsData {
                trigger.value = Data(it.copy(mode = TriggerMode.SEQUENCE))
            }

            TriggerMode.UNDEFINED -> trigger.value.ifIsData {
                trigger.value = Data(it.copy(mode = TriggerMode.UNDEFINED))
            }
        }
    }

    override fun addTriggerKey(
        keyCode: Int,
        device: TriggerKeyDevice
    ) = trigger.value.ifIsData { data ->
        val clickType = ClickType.SHORT_PRESS

        val containsKey = data.keys.any { keyToCompare ->
            if (data.mode != TriggerMode.SEQUENCE) {
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

        val newKeys = data.keys.toMutableList().apply {

            val triggerKey = TriggerKey(
                keyCode = keyCode,
                device = device,
                clickType = clickType
            )

            add(triggerKey)
        }

        val newMode = when {
            containsKey -> TriggerMode.SEQUENCE
            newKeys.size <= 1 -> TriggerMode.UNDEFINED

            /* Automatically make it a parallel trigger when the user makes a trigger with more than one key
            because this is what most users are expecting when they make a trigger with multiple keys */
            newKeys.size == 2 && !containsKey -> TriggerMode.PARALLEL
            else -> data.mode
        }

        trigger.value = Data(data.copy(keys = newKeys, mode = newMode))
    }

    override fun removeTriggerKey(uid: String) = trigger.value.ifIsData { data ->
        val newKeys = data.keys.toMutableList().apply {
            removeAll { it.uid == uid }
        }

        val newMode = when {
            newKeys.size <= 1 -> TriggerMode.UNDEFINED
            else -> data.mode
        }

        trigger.value = Data(data.copy(keys = newKeys, mode = newMode))
    }

    override fun moveTriggerKey(fromIndex: Int, toIndex: Int) = trigger.value.ifIsData { data ->
        data.keys.toMutableList().apply {
            if (fromIndex < toIndex) {
                for (i in fromIndex until toIndex) {
                    Collections.swap(this, i, i + 1)
                }
            } else {
                for (i in fromIndex downTo toIndex + 1) {
                    Collections.swap(this, i, i - 1)
                }
            }

            trigger.value = Data(data.copy(keys = this))
        }
    }

    override fun setParallelTriggerClickType(clickType: ClickType) {
        TODO("Not yet implemented")
    }

    override fun setTriggerKeyDevice(keyUid: String, device: TriggerKeyDevice) {

    }

    fun getTrigger(): DataState<KeymapTrigger> = trigger.value

    fun setTrigger(trigger: KeymapTrigger) {
        this.trigger.value = Data(trigger)
    }

    override fun setVibrateEnabled(enabled: Boolean) = setOption { it.copy(vibrate = enabled) }
    override fun setVibrationDuration(duration: Defaultable<Int>) =
        setOption { it.copy(vibrateDuration = duration) }

    override fun setLongPressDelay(delay: Defaultable<Int>) {
        TODO("Not yet implemented")
    }

    private fun setOption(block: (trigger: KeymapTrigger) -> KeymapTrigger) {
        trigger.value = trigger.value.mapData { block(it) }
    }
}

interface ConfigKeymapTriggerUseCase {
    val keys: Flow<DataState<List<TriggerKey>>>
    val mode: Flow<TriggerMode>
    val options: Flow<DataState<KeymapTriggerOptions>>

    fun addTriggerKey(keyCode: Int, device: TriggerKeyDevice)
    fun removeTriggerKey(uid: String)
    fun moveTriggerKey(fromIndex: Int, toIndex: Int)

    fun setMode(newMode: TriggerMode)
    fun setParallelTriggerClickType(clickType: ClickType)
    fun setTriggerKeyDevice(keyUid: String, device: TriggerKeyDevice)

    fun setVibrateEnabled(enabled: Boolean)
    fun setVibrationDuration(duration: Defaultable<Int>)
    fun setLongPressDelay(delay: Defaultable<Int>)
}