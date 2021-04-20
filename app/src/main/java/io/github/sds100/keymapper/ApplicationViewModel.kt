package io.github.sds100.keymapper

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.sds100.keymapper.domain.usecases.ControlKeyboardOnBluetoothEventUseCase
import io.github.sds100.keymapper.system.inputmethod.ControlKeyboardOnToggleKeymapsUseCase
import io.github.sds100.keymapper.settings.GetThemeUseCase
import io.github.sds100.keymapper.settings.ThemeUtils
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Created by sds100 on 14/02/2021.
 */
class ApplicationViewModel(
    getTheme: GetThemeUseCase,
    controlKeyboardOnToggleKeymapsUseCase: ControlKeyboardOnToggleKeymapsUseCase,
    controlKeyboardOnBluetoothEvent: ControlKeyboardOnBluetoothEventUseCase
) : ViewModel() {

    private val _theme = MutableLiveData<Int>()
    val theme: LiveData<Int> = _theme

    init {
        getTheme().onEach {
            _theme.value = when (it) {
                ThemeUtils.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                ThemeUtils.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                ThemeUtils.AUTO -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        }.launchIn(viewModelScope)

        controlKeyboardOnToggleKeymapsUseCase.start(viewModelScope)
        controlKeyboardOnBluetoothEvent.start(viewModelScope)
    }
}