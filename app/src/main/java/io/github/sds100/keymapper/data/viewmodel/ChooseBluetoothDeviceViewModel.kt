package io.github.sds100.keymapper.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.devices.BluetoothDeviceInfo
import io.github.sds100.keymapper.devices.ChooseBluetoothDeviceUseCase
import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.ui.ListUiState
import io.github.sds100.keymapper.ui.createListState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Created by sds100 on 07/04/2021.
 */
class ChooseBluetoothDeviceViewModel(
    val useCase: ChooseBluetoothDeviceUseCase,
    resourceProvider: ResourceProvider
) : ViewModel(), ResourceProvider by resourceProvider {

    private val _caption = MutableStateFlow<String?>(null)
    val caption: StateFlow<String?> = _caption

    val listItems: StateFlow<ListUiState<BluetoothDeviceInfo>> = useCase.devices.map {
        when (it) {
            is State.Loading -> ListUiState.Loading
            is State.Data -> it.data.createListState()
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, ListUiState.Loading)

    private val rebuildUiState = MutableSharedFlow<Unit>()

    init {
        viewModelScope.launch {
            combine(rebuildUiState, useCase.devices) { _, devices ->
                devices
            }.collectLatest { devicesState ->
                _caption.value = when (devicesState) {
                    is State.Loading -> null
                    is State.Data -> getString(R.string.caption_no_paired_bt_devices)
                }
            }
        }
    }

    fun rebuildUiState() {
        runBlocking { rebuildUiState.emit(Unit) }
    }

    class Factory(
        private val useCase: ChooseBluetoothDeviceUseCase,
        private val resourceProvider: ResourceProvider
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) =
            ChooseBluetoothDeviceViewModel(
                useCase,
                resourceProvider
            ) as T
    }
}