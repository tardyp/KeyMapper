package io.github.sds100.keymapper.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.sds100.keymapper.util.KeyEventUtils
import kotlinx.coroutines.flow.*

/**
 * Created by sds100 on 30/03/2020.
 */

class KeyActionTypeViewModel : ViewModel() {
    private val _keyCode = MutableStateFlow<Int?>(null)
    val keyCode = _keyCode.asStateFlow()

    val keyLabel = _keyCode.map {
        it ?: return@map null
        KeyEventUtils.keycodeToString(it)
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun clearKey() {
        _keyCode.value = null
    }

    fun onKeyDown(keyCode: Int) {
        this._keyCode.value = keyCode
    }

    @Suppress("UNCHECKED_CAST")
    class Factory : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return KeyActionTypeViewModel() as T
        }
    }
}