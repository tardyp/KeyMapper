package io.github.sds100.keymapper.domain.usecases

import io.github.sds100.keymapper.domain.adapter.BluetoothMonitor
import io.github.sds100.keymapper.domain.adapter.KeyboardAdapter
import io.github.sds100.keymapper.domain.preferences.Keys
import io.github.sds100.keymapper.domain.repositories.PreferenceRepository
import io.github.sds100.keymapper.domain.utils.PrefDelegate
import io.github.sds100.keymapper.util.collectIn
import kotlinx.coroutines.CoroutineScope

/**
 * Created by sds100 on 14/02/2021.
 */
class ControlKeyboardOnBluetoothEventUseCaseImpl(
    private val keyboardAdapter: KeyboardAdapter,
    private val preferenceRepository: PreferenceRepository,
    private val bluetoothMonitor: BluetoothMonitor
) : PreferenceRepository by preferenceRepository, ControlKeyboardOnBluetoothEventUseCase {
    private val devicesThatToggleKeyboard
        by PrefDelegate(Keys.bluetoothDevicesThatToggleKeyboard, emptySet())

    private val bluetoothDevicesThatShowImePicker
        by PrefDelegate(Keys.bluetoothDevicesThatShowImePicker, emptySet())

    private val changeImeOnBtConnect by PrefDelegate(Keys.changeImeOnBtConnect, false)
    private val showImePickerOnBtConnect by PrefDelegate(Keys.showImePickerOnBtConnect, false)

    override fun start(coroutineScope: CoroutineScope) {
        bluetoothMonitor.onDeviceConnect.collectIn(coroutineScope) { address ->
            if (changeImeOnBtConnect && devicesThatToggleKeyboard.contains(address)) {
                keyboardAdapter.chooseCompatibleInputMethod()
            }

            if (showImePickerOnBtConnect && bluetoothDevicesThatShowImePicker.contains(address)) {
                keyboardAdapter.showImePickerOutsideApp()
            }
        }

        bluetoothMonitor.onDeviceDisconnect.collectIn(coroutineScope) { address ->
            if (changeImeOnBtConnect && devicesThatToggleKeyboard.contains(address)) {
                keyboardAdapter.chooseLastUsedIncompatibleInputMethod()
            }

            if (showImePickerOnBtConnect && bluetoothDevicesThatShowImePicker.contains(address)) {
                keyboardAdapter.showImePickerOutsideApp()
            }
        }
    }
}

interface ControlKeyboardOnBluetoothEventUseCase {
    fun start(coroutineScope: CoroutineScope)
}