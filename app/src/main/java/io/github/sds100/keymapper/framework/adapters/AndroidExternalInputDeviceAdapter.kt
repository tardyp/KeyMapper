package io.github.sds100.keymapper.framework.adapters

import android.content.Context
import android.hardware.input.InputManager
import android.os.Handler
import android.view.InputDevice
import androidx.core.content.getSystemService
import io.github.sds100.keymapper.domain.adapter.ExternalInputDeviceAdapter
import io.github.sds100.keymapper.domain.devices.DeviceInfo
import io.github.sds100.keymapper.util.isExternalCompat
import io.github.sds100.keymapper.util.result.Result
import kotlinx.coroutines.flow.MutableStateFlow
import splitties.mainthread.mainLooper

/**
 * Created by sds100 on 13/03/2021.
 */
class AndroidExternalInputDeviceAdapter(context: Context) : ExternalInputDeviceAdapter {
    private val ctx = context.applicationContext
    private val inputManager = ctx.getSystemService<InputManager>()
    override val devices = MutableStateFlow(getDeviceList())

    init {
        inputManager?.apply {
            registerInputDeviceListener(object : InputManager.InputDeviceListener {
                override fun onInputDeviceAdded(deviceId: Int) {
                    devices.value = getDeviceList()
                }

                override fun onInputDeviceRemoved(deviceId: Int) {
                    devices.value = getDeviceList()
                }

                override fun onInputDeviceChanged(deviceId: Int) {
                    devices.value = getDeviceList()
                }

            }, Handler(mainLooper))
        }
    }

    override fun getDeviceName(descriptor: String): Result<String> {
        TODO("Not yet implemented")
    }

    private fun getDeviceList(): List<DeviceInfo> = sequence {
        InputDevice.getDeviceIds().forEach {
            val device = InputDevice.getDevice(it)

            if (!device.isExternalCompat) return@forEach

            yield(DeviceInfo(device.descriptor, device.name))
        }
    }.toList()
}