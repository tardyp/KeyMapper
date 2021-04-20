package io.github.sds100.keymapper.system.devices

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice

/**
 * Created by sds100 on 02/10/2018.
 */

object BluetoothUtils {
    /**
     * @return a list of all paired bluetooth devices. It is null if bluetooth is disabled and/or
     * there are not paired devices
     */
    fun getPairedDevices(): List<BluetoothDevice>? {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ?: return null

        return bluetoothAdapter.bondedDevices.toList()
    }

    fun enableBluetooth() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothAdapter.enable()
    }

    fun disableBluetooth() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothAdapter.disable()
    }

    fun toggleBluetooth() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter.isEnabled) {
            bluetoothAdapter.disable()
        } else {
            bluetoothAdapter.enable()
        }
    }
}