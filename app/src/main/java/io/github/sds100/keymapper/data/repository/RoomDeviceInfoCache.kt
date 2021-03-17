package io.github.sds100.keymapper.data.repository

import io.github.sds100.keymapper.data.db.dao.DeviceInfoDao
import io.github.sds100.keymapper.data.model.DeviceInfoEntity
import io.github.sds100.keymapper.data.model.TriggerEntity
import io.github.sds100.keymapper.util.result.Error
import io.github.sds100.keymapper.util.result.Result
import io.github.sds100.keymapper.util.result.Success
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Created by sds100 on 26/01/2020.
 */

//TODO delete because device names should be saved with actions and triggers
class RoomDeviceInfoCache(
    private val deviceInfoDao: DeviceInfoDao,
    private val coroutineScope: CoroutineScope
) : DeviceInfoCache {

    override suspend fun getAll(): List<DeviceInfoEntity> {
        //these devices might have been accidentally saved at some point
        return deviceInfoDao.getAll().toMutableList().apply {
            removeAll {
                it.descriptor == TriggerEntity.KeyEntity.DEVICE_ID_THIS_DEVICE
                    || it.descriptor == TriggerEntity.KeyEntity.DEVICE_ID_ANY_DEVICE
            }
        }
    }

    override fun insertDeviceInfo(vararg deviceInfo: DeviceInfoEntity) {
        coroutineScope.launch {
            deviceInfoDao.insert(*deviceInfo)
        }
    }

    override suspend fun getDeviceName(descriptor: String): Result<String> {
        return with(deviceInfoDao.getByDescriptor(descriptor)) {
            if (this == null) {
                Error.DeviceNotFound(descriptor)
            } else {
                Success(this.name)
            }
        }
    }

    override fun deleteAll() {
        coroutineScope.launch {
            deviceInfoDao.deleteAll()
        }
    }
}