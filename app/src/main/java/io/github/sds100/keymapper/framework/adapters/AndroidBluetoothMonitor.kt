package io.github.sds100.keymapper.framework.adapters

import android.bluetooth.BluetoothDevice
import android.content.Intent
import io.github.sds100.keymapper.domain.adapter.BluetoothMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

/**
 * Created by sds100 on 14/02/2021.
 */
class AndroidBluetoothMonitor(
    private val coroutineScope: CoroutineScope
) : BluetoothMonitor {

    override val onDeviceConnect = MutableSharedFlow<String>()
    override val onDeviceDisconnect = MutableSharedFlow<String>()

    fun onReceiveIntent(intent: Intent) {

        when (intent.action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        ?: return

                coroutineScope.launch {
                    onDeviceConnect.emit(device.address)
                }
            }

            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        ?: return

                coroutineScope.launch {
                    onDeviceDisconnect.emit(device.address)
                }
            }
        }
    }
}