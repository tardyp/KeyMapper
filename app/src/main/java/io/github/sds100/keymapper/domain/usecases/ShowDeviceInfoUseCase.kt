package io.github.sds100.keymapper.domain.usecases

import io.github.sds100.keymapper.data.repository.DeviceInfoRepository
import io.github.sds100.keymapper.domain.preferences.Keys
import io.github.sds100.keymapper.domain.repositories.PreferenceRepository
import io.github.sds100.keymapper.domain.utils.PrefDelegate

/**
 * Created by sds100 on 14/02/2021.
 */

class ShowDeviceInfoUseCase(
    private val deviceInfoRepository: DeviceInfoRepository,
    preferenceRepository: PreferenceRepository
) : PreferenceRepository by preferenceRepository {
    suspend fun getAll() = deviceInfoRepository.getAll()
    fun getDeviceInfo(descriptor: String) = deviceInfoRepository.getDeviceInfo(descriptor)

    val showDeviceDescriptors by PrefDelegate(Keys.showDeviceDescriptors, false)
}