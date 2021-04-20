package io.github.sds100.keymapper.system.devices

import io.github.sds100.keymapper.data.Keys
import io.github.sds100.keymapper.data.repositories.PreferenceRepository
import io.github.sds100.keymapper.util.PrefDelegate
import io.github.sds100.keymapper.util.State
import io.github.sds100.keymapper.util.dataOrNull
import io.github.sds100.keymapper.util.Result
import io.github.sds100.keymapper.util.Success
import io.github.sds100.keymapper.util.otherwise
import io.github.sds100.keymapper.util.then
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.map

/**
 * Created by sds100 on 14/02/2021.
 */

class GetInputDevicesUseCaseImpl(
    private val deviceInfoCache: DeviceInfoCache,
    private val preferenceRepository: PreferenceRepository,
    private val externalDevicesAdapter: ExternalDevicesAdapter
) : PreferenceRepository by preferenceRepository, GetInputDevicesUseCase, ShowDeviceInfoUseCase,
    GetDeviceNameUseCase {

    //TODO show device descriptor in name if showdevicedescriptors is turned on
    override val devices: Flow<List<InputDeviceInfo>> = externalDevicesAdapter.inputDevices
        .dropWhile { it !is State.Data }
        .map { it.dataOrNull()!! }

    //TODO remove
    override suspend fun getAll(): Set<InputDeviceInfo> {
        return emptySet()
    }

    override suspend fun getDeviceName(descriptor: String) =
        deviceInfoCache.getDeviceName(descriptor).otherwise {
            externalDevicesAdapter.getInputDeviceName(descriptor)
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

interface GetInputDevicesUseCase {
    val devices: Flow<List<InputDeviceInfo>>
}

//TODO remove
interface ShowDeviceInfoUseCase {
    //TODO remove
    suspend fun getAll(): Set<InputDeviceInfo>
    suspend fun getDeviceName(descriptor: String): Result<String>

    //TODO remove
    val showDeviceDescriptors: Boolean
}

//TODO remove
interface GetDeviceNameUseCase {
    suspend operator fun invoke(descriptor: String): Result<String>
}