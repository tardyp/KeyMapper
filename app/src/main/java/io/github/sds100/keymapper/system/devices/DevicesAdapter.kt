package io.github.sds100.keymapper.system.devices

import io.github.sds100.keymapper.system.bluetooth.BluetoothDeviceInfo
import io.github.sds100.keymapper.util.Result
import kotlinx.coroutines.flow.StateFlow

/**
 * Created by sds100 on 07/03/2021.
 */
interface DevicesAdapter {
    val inputDevices: StateFlow<List<InputDeviceInfo>>
    val pairedBluetoothDevices: StateFlow<List<BluetoothDeviceInfo>>
    val connectedBluetoothDevices: StateFlow<Set<BluetoothDeviceInfo>>

    fun deviceHasKey(id: Int, keyCode: Int): Boolean
    fun getInputDeviceName(descriptor: String): Result<String>
}