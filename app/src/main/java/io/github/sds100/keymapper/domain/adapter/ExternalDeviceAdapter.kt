package io.github.sds100.keymapper.domain.adapter

import io.github.sds100.keymapper.domain.devices.DeviceInfo
import io.github.sds100.keymapper.util.result.Result

/**
 * Created by sds100 on 07/03/2021.
 */
interface ExternalDeviceAdapter {
    fun getExternalInputDevices(): List<DeviceInfo>

    fun getDeviceName(descriptor: String): Result<String>
}