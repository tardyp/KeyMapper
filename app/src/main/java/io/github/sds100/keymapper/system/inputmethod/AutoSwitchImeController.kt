package io.github.sds100.keymapper.system.inputmethod

import io.github.sds100.keymapper.data.Keys
import io.github.sds100.keymapper.data.repositories.PreferenceRepository
import io.github.sds100.keymapper.util.PrefDelegate
import io.github.sds100.keymapper.mappings.PauseMappingsUseCase
import io.github.sds100.keymapper.system.bluetooth.BluetoothAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Created by sds100 on 20/04/2021.
 */
class AutoSwitchImeController(
    private val coroutineScope: CoroutineScope,
    private val preferenceRepository: PreferenceRepository,
    private val inputMethodAdapter: InputMethodAdapter,
    private val pauseMappingsUseCase: PauseMappingsUseCase,
    private val bluetoothAdapter: BluetoothAdapter
) : PreferenceRepository by preferenceRepository{
    private val imeHelper = KeyMapperImeHelper(inputMethodAdapter)

    private val devicesThatToggleKeyboard
        by PrefDelegate(Keys.bluetoothDevicesThatToggleKeyboard, emptySet())

    private val bluetoothDevicesThatShowImePicker
        by PrefDelegate(Keys.bluetoothDevicesThatShowImePicker, emptySet())

    private val changeImeOnBtConnect by PrefDelegate(Keys.changeImeOnBtConnect, false)
    private val showImePickerOnBtConnect by PrefDelegate(Keys.showImePickerOnBtConnect, false)

    private val toggleKeyboardOnToggleKeymaps by PrefDelegate(
        Keys.toggleKeyboardOnToggleKeymaps,
        false
    )
    init {
        pauseMappingsUseCase.isPaused.onEach { isPaused ->

            if (!toggleKeyboardOnToggleKeymaps) return@onEach

            if (isPaused) {
                imeHelper.chooseLastUsedIncompatibleInputMethod(fromForeground = false)
            } else {
                imeHelper.chooseCompatibleInputMethod(fromForeground = false)
            }
        }.launchIn(coroutineScope)

        bluetoothAdapter.onDeviceConnect.onEach { address ->
            if (changeImeOnBtConnect && devicesThatToggleKeyboard.contains(address)) {
                imeHelper.chooseCompatibleInputMethod(fromForeground = false)
            }

            if (showImePickerOnBtConnect && bluetoothDevicesThatShowImePicker.contains(address)) {
                inputMethodAdapter.showImePicker(fromForeground = false)
            }
        }.launchIn(coroutineScope)

        bluetoothAdapter.onDeviceDisconnect.onEach { address ->
            if (changeImeOnBtConnect && devicesThatToggleKeyboard.contains(address)) {
                imeHelper.chooseLastUsedIncompatibleInputMethod(fromForeground = false)
            }

            if (showImePickerOnBtConnect && bluetoothDevicesThatShowImePicker.contains(address)) {
                inputMethodAdapter.showImePicker(fromForeground = false)
            }
        }.launchIn(coroutineScope)
    }
}