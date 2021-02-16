package io.github.sds100.keymapper.domain.usecases

import io.github.sds100.keymapper.data.repository.DeviceInfoRepository
import io.github.sds100.keymapper.domain.preferences.Keys
import io.github.sds100.keymapper.domain.repositories.PreferenceRepository
import io.github.sds100.keymapper.domain.utils.PrefDelegate

/**
 * Created by sds100 on 15/02/2021.
 */
class ShowActionsUseCase(
    preferenceRepository: PreferenceRepository,
    deviceInfoRepository: DeviceInfoRepository
) : PreferenceRepository by preferenceRepository {

    private val showDeviceInfoUseCase =
        ShowDeviceInfoUseCase(deviceInfoRepository, preferenceRepository)

    suspend fun getDeviceInfo() = showDeviceInfoUseCase.getAll()
    val showDeviceDescriptors = showDeviceInfoUseCase.showDeviceDescriptors
    val hasRootPermission by PrefDelegate(Keys.hasRootPermission, false)
}