package io.github.sds100.keymapper.devices

import io.github.sds100.keymapper.domain.adapter.ExternalDevicesAdapter
import io.github.sds100.keymapper.domain.utils.State
import kotlinx.coroutines.flow.Flow

/**
 * Created by sds100 on 07/04/2021.
 */

class ChooseBluetoothDeviceUseCaseImpl(adapter: ExternalDevicesAdapter): ChooseBluetoothDeviceUseCase{
    override val devices: Flow<State<List<BluetoothDeviceInfo>>> = adapter.pairedBluetoothDevices
}

interface ChooseBluetoothDeviceUseCase {
    val devices: Flow<State<List<BluetoothDeviceInfo>>>
}