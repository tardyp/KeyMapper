package io.github.sds100.keymapper.domain.adapter

import io.github.sds100.keymapper.domain.devices.DeviceInfo
import io.github.sds100.keymapper.util.result.Result
import kotlinx.coroutines.flow.StateFlow

/**
 * Created by sds100 on 07/03/2021.
 */
interface ExternalInputDeviceAdapter {
    val devices: StateFlow<List<DeviceInfo>>

    fun getDeviceName(descriptor: String): Result<String>
}