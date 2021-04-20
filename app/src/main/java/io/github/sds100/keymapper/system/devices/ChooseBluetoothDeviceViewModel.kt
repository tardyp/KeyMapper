package io.github.sds100.keymapper.system.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.util.State
import io.github.sds100.keymapper.util.ui.ResourceProvider
import io.github.sds100.keymapper.util.ui.ListUiState
import io.github.sds100.keymapper.util.ui.createListState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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

    init {
        viewModelScope.launch {
            useCase.devices.collectLatest { devicesState ->
                _caption.value = when (devicesState) {
                    is State.Loading -> null
                    is State.Data -> getString(R.string.caption_no_paired_bt_devices)
                }
            }
        }
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