package io.github.sds100.keymapper.domain.devices

import io.github.sds100.keymapper.data.repository.DeviceInfoCache
import io.github.sds100.keymapper.domain.adapter.ExternalDeviceAdapter
import io.github.sds100.keymapper.domain.preferences.Keys
import io.github.sds100.keymapper.domain.repositories.PreferenceRepository
import io.github.sds100.keymapper.domain.utils.PrefDelegate
import io.github.sds100.keymapper.util.result.Result
import io.github.sds100.keymapper.util.result.Success
import io.github.sds100.keymapper.util.result.otherwise
import io.github.sds100.keymapper.util.result.then

/**
 * Created by sds100 on 14/02/2021.
 */

class ShowDeviceInfoUseCaseImpl(
    private val deviceInfoCache: DeviceInfoCache,
    private val preferenceRepository: PreferenceRepository,
    private val externalDeviceAdapter: ExternalDeviceAdapter
) : PreferenceRepository by preferenceRepository, ShowDeviceInfoUseCase, GetDeviceNameUseCase {

    override suspend fun getAll(): Set<DeviceInfo> {
        val cached = deviceInfoCache.getAll().map { DeviceInfo(it.descriptor, it.name) }
        val connected = externalDeviceAdapter.getExternalInputDevices()

        return sequence {
            yieldAll(cached)
            yieldAll(connected)
        }.toSet()
    }

    override suspend fun getDeviceName(descriptor: String) =
        deviceInfoCache.getDeviceName(descriptor).otherwise {
            externalDeviceAdapter.getDeviceName(descriptor)
        }.then { name ->
            if (showDeviceDescriptors) {
                Success("$name ${descriptor.substring(0..4)}")
            } else {
                Success(name)
            }
        }

    override suspend fun invoke(descriptor: String) = getDeviceName(descriptor)

    //TODO remove
    override val showDeviceDescriptors by PrefDelegate(Keys.showDeviceDescriptors, false)
}

//TODO remove
interface ShowDeviceInfoUseCase {
    //TODO remove
    suspend fun getAll(): Set<DeviceInfo>
    suspend fun getDeviceName(descriptor: String): Result<String>

    //TODO remove
    val showDeviceDescriptors: Boolean
}

interface GetDeviceNameUseCase {
    suspend operator fun invoke(descriptor: String): Result<String>
}