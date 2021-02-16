package io.github.sds100.keymapper.domain.usecases

import io.github.sds100.keymapper.data.model.DeviceInfo
import io.github.sds100.keymapper.data.repository.DeviceInfoRepository

/**
 * Created by sds100 on 14/02/2021.
 */

class SaveDeviceInfoUseCase(private val deviceInfoRepository: DeviceInfoRepository) {
    operator fun invoke(vararg deviceInfo: DeviceInfo) =
        deviceInfoRepository.insertDeviceInfo(*deviceInfo)
}