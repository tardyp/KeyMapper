package io.github.sds100.keymapper.framework.adapters

import android.content.Context
import io.github.sds100.keymapper.domain.adapter.ExternalDeviceAdapter
import io.github.sds100.keymapper.domain.devices.DeviceInfo
import io.github.sds100.keymapper.util.result.Result

/**
 * Created by sds100 on 13/03/2021.
 */
class AndroidExternalDeviceAdapter(ctx: Context) : ExternalDeviceAdapter {
    private val ctx = ctx.applicationContext
    override fun getExternalInputDevices(): List<DeviceInfo> {
        TODO("Not yet implemented")
    }

    override fun getDeviceName(descriptor: String): Result<String> {
        TODO("Not yet implemented")
    }
}