package io.github.sds100.keymapper.domain.usecases

import io.github.sds100.keymapper.domain.KeyMapperImeManager
import io.github.sds100.keymapper.domain.adapter.BluetoothMonitor
import io.github.sds100.keymapper.domain.adapter.InputMethodAdapter
import io.github.sds100.keymapper.domain.preferences.Keys
import io.github.sds100.keymapper.domain.repositories.PreferenceRepository
import io.github.sds100.keymapper.domain.utils.PrefDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Created by sds100 on 14/02/2021.
 */
class ControlKeyboardOnBluetoothEventUseCaseImpl(
   private val inputMethodAdapter: InputMethodAdapter,
    private val preferenceRepository: PreferenceRepository,
    private val bluetoothMonitor: BluetoothMonitor
) : PreferenceRepository by preferenceRepository, ControlKeyboardOnBluetoothEventUseCase {
    private val imeManager = KeyMapperImeManager(inputMethodAdapter)

    private val devicesThatToggleKeyboard
        by PrefDelegate(Keys.bluetoothDevicesThatToggleKeyboard, emptySet())

    private val bluetoothDevicesThatShowImePicker
        by PrefDelegate(Keys.bluetoothDevicesThatShowImePicker, emptySet())

    private val changeImeOnBtConnect by PrefDelegate(Keys.changeImeOnBtConnect, false)
    private val showImePickerOnBtConnect by PrefDelegate(Keys.showImePickerOnBtConnect, false)

    override fun start(coroutineScope: CoroutineScope) {
        bluetoothMonitor.onDeviceConnect.onEach { address ->
            if (changeImeOnBtConnect && devicesThatToggleKeyboard.contains(address)) {
                imeManager.chooseCompatibleInputMethod(fromForeground = true)
            }

            if (showImePickerOnBtConnect && bluetoothDevicesThatShowImePicker.contains(address)) {
                inputMethodAdapter.showImePicker(fromForeground = false)
            }
        }.launchIn(coroutineScope)

        bluetoothMonitor.onDeviceDisconnect.onEach { address ->
            if (changeImeOnBtConnect && devicesThatToggleKeyboard.contains(address)) {
                imeManager.chooseLastUsedIncompatibleInputMethod(fromForeground = false)
            }

            if (showImePickerOnBtConnect && bluetoothDevicesThatShowImePicker.contains(address)) {
                inputMethodAdapter.showImePicker(fromForeground = false)
            }
        }.launchIn(coroutineScope)
    }
}

interface ControlKeyboardOnBluetoothEventUseCase {
    fun start(coroutineScope: CoroutineScope)
}