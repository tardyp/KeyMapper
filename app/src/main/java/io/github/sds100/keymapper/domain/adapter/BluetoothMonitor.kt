package io.github.sds100.keymapper.domain.adapter

import kotlinx.coroutines.flow.SharedFlow

/**
 * Created by sds100 on 14/02/2021.
 */
interface BluetoothMonitor {
    /**
     * Value is the address of the device
     */
    val onDeviceConnect: SharedFlow<String>

    /**
     * Value is the address of the device
     */
    val onDeviceDisconnect: SharedFlow<String>
}