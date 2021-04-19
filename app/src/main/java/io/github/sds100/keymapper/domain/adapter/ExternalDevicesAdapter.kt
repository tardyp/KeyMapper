package io.github.sds100.keymapper.domain.adapter

import io.github.sds100.keymapper.devices.BluetoothDeviceInfo
import io.github.sds100.keymapper.domain.devices.InputDeviceInfo
import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.util.result.Result
import kotlinx.coroutines.flow.StateFlow

/**
 * Created by sds100 on 07/03/2021.
 */
interface ExternalDevicesAdapter {
    val inputDevices: StateFlow<State<List<InputDeviceInfo>>>
    val pairedBluetoothDevices: StateFlow<State<List<BluetoothDeviceInfo>>>

    fun getInputDeviceName(descriptor: String): Result<String>
}