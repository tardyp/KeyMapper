package io.github.sds100.keymapper.system.devices

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Created by sds100 on 14/02/2021.
 */

class AndroidBluetoothMonitor(
    context: Context,
    private val coroutineScope: CoroutineScope
) : BluetoothMonitor {

    override val onDeviceConnect = MutableSharedFlow<String>()
    override val onDeviceDisconnect = MutableSharedFlow<String>()
    override val onDevicePairedChange = MutableSharedFlow<String>()
    override val isBluetoothEnabled =
        MutableStateFlow(BluetoothAdapter.getDefaultAdapter()?.isEnabled ?: false)

    private val ctx: Context = context.applicationContext
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return

            onReceiveIntent(intent)
        }
    }

    init {
        IntentFilter().apply {
            //these broadcasts can't be received from a manifest declared receiver on Android 8.0+
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)

            ctx.registerReceiver(broadcastReceiver, this)
        }
    }

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

            BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        ?: return

                coroutineScope.launch {
                    onDevicePairedChange.emit(device.address)
                }
            }

            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)

                if (state == -1) return

                when (state) {
                    BluetoothAdapter.STATE_ON -> isBluetoothEnabled.value = true
                    BluetoothAdapter.STATE_OFF -> isBluetoothEnabled.value = false
                }
            }
        }
    }
}