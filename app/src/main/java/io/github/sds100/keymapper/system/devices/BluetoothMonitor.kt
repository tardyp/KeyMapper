package io.github.sds100.keymapper.system.devices

import kotlinx.coroutines.flow.Flow

/**
 * Created by sds100 on 14/02/2021.
 */
interface BluetoothMonitor {
    /**
     * Value is the address of the device
     */
    val onDeviceConnect: Flow<String>

    /**
     * Value is the address of the device
     */
    val onDeviceDisconnect: Flow<String>

    /**
     * Value is the address of the device
     */
    val onDevicePairedChange: Flow<String>

    val isBluetoothEnabled: Flow<Boolean>
}