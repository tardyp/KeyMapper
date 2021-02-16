package io.github.sds100.keymapper.broadcastreceiver

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.sds100.keymapper.MyApplication

/**
 * Created by sds100 on 28/12/2018.
 */

/**
 * Listens for bluetooth devices to connect/disconnect
 */
class BluetoothConnectionBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        intent ?: return

        if (intent.action == BluetoothDevice.ACTION_ACL_CONNECTED ||
            intent.action == BluetoothDevice.ACTION_ACL_DISCONNECTED
        ) {
            (context.applicationContext as MyApplication).bluetoothMonitor.onReceiveIntent(intent)
        }
    }
}