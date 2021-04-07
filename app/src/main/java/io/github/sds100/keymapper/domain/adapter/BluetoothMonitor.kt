package io.github.sds100.keymapper.domain.adapter

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

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