package io.github.sds100.keymapper.data.repository

import io.github.sds100.keymapper.data.model.DeviceInfoEntity
import io.github.sds100.keymapper.util.DataState
import io.github.sds100.keymapper.util.result.Result

/**
 * Created by sds100 on 17/05/2020.
 */

//TODO delete
interface DeviceInfoCache {
    suspend fun getAll(): List<DeviceInfoEntity>
    fun insertDeviceInfo(vararg deviceInfo: DeviceInfoEntity)
    suspend fun getDeviceName(descriptor: String): Result<String>
    fun deleteAll()
}