package io.github.sds100.keymapper.system.devices

import io.github.sds100.keymapper.util.State
import io.github.sds100.keymapper.util.Result
import kotlinx.coroutines.flow.StateFlow

/**
 * Created by sds100 on 07/03/2021.
 */
interface ExternalDevicesAdapter {
    val inputDevices: StateFlow<State<List<InputDeviceInfo>>>
    val pairedBluetoothDevices: StateFlow<State<List<BluetoothDeviceInfo>>>

    fun deviceHasKey(id: Int, keyCode: Int):Boolean
    fun getInputDeviceName(descriptor: String): Result<String>
}