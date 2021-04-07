package io.github.sds100.keymapper.framework.adapters

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.hardware.input.InputManager
import android.os.Handler
import android.view.InputDevice
import androidx.core.content.getSystemService
import io.github.sds100.keymapper.devices.BluetoothDeviceInfo
import io.github.sds100.keymapper.domain.adapter.BluetoothMonitor
import io.github.sds100.keymapper.domain.adapter.ExternalDevicesAdapter
import io.github.sds100.keymapper.domain.devices.InputDeviceInfo
import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.util.isExternalCompat
import io.github.sds100.keymapper.util.result.Error
import io.github.sds100.keymapper.util.result.Result
import io.github.sds100.keymapper.util.result.Success
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import splitties.mainthread.mainLooper
import timber.log.Timber

/**
 * Created by sds100 on 13/03/2021.
 */
class AndroidExternalDevicesAdapter(
    context: Context,
    private val bluetoothMonitor: BluetoothMonitor,
    private val coroutineScope: CoroutineScope
) : ExternalDevicesAdapter {
    private val ctx = context.applicationContext
    private val inputManager = ctx.getSystemService<InputManager>()

    override val inputDevices = MutableStateFlow<State<List<InputDeviceInfo>>>(State.Loading)

    override val pairedBluetoothDevices =
        MutableStateFlow<State<List<BluetoothDeviceInfo>>>(State.Loading)

    init {
        coroutineScope.launch {
            updatePairedBluetoothDevices()
            updateInputDevices()
        }

        coroutineScope.launch {
            merge(
                bluetoothMonitor.onDevicePairedChange,
                bluetoothMonitor.isBluetoothEnabled
            ).collectLatest {
               updatePairedBluetoothDevices()
            }
        }

        inputManager?.apply {
            registerInputDeviceListener(object : InputManager.InputDeviceListener {
                override fun onInputDeviceAdded(deviceId: Int) {
                    updateInputDevices()
                }

                override fun onInputDeviceRemoved(deviceId: Int) {
                    updateInputDevices()
                }

                override fun onInputDeviceChanged(deviceId: Int) {
                    updateInputDevices()
                }

            }, Handler(mainLooper))
        }
    }

    override fun getInputDeviceName(descriptor: String): Result<String> {
        InputDevice.getDeviceIds().forEach {
            val device = InputDevice.getDevice(it)

            if (device.descriptor == descriptor) {
                return Success(device.name)
            }
        }

        return Error.DeviceNotFound(descriptor)
    }

    private fun updateInputDevices(){
        val devices = mutableListOf<InputDeviceInfo>()

        InputDevice.getDeviceIds().forEach {
            val device = InputDevice.getDevice(it)

            if (!device.isExternalCompat) return@forEach

            devices.add(InputDeviceInfo(device.descriptor, device.name))
        }

        inputDevices.value = State.Data(devices)
    }

    private fun updatePairedBluetoothDevices(){
        val devices = BluetoothAdapter.getDefaultAdapter().bondedDevices.map {
            BluetoothDeviceInfo(it.address, it.name)
        }

        pairedBluetoothDevices.value = State.Data(devices)
    }
}