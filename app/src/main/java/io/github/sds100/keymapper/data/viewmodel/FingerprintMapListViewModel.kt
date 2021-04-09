package io.github.sds100.keymapper.data.viewmodel

import androidx.lifecycle.*
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.FingerprintMapId
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.home.HomeScreenUseCase
import io.github.sds100.keymapper.mappings.common.BaseMappingListViewModel
import io.github.sds100.keymapper.mappings.fingerprintmaps.FingerprintMapGroup
import io.github.sds100.keymapper.ui.ListUiState
import io.github.sds100.keymapper.ui.createListState
import io.github.sds100.keymapper.ui.mappings.fingerprintmap.FingerprintMapListItem
import io.github.sds100.keymapper.ui.mappings.fingerprintmap.FingerprintMapListItemCreator
import io.github.sds100.keymapper.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class FingerprintMapListViewModel(
    private val coroutineScope: CoroutineScope,
    private val useCase: HomeScreenUseCase,
    resourceProvider: ResourceProvider,
) : BaseMappingListViewModel(coroutineScope, resourceProvider) {

    private val listItemCreator = FingerprintMapListItemCreator(
        useCase,
        resourceProvider
    )

    private val _state = MutableStateFlow<ListUiState<FingerprintMapListItem>>(ListUiState.Loading)
    val state = _state.asStateFlow()

    init {
        val rebuildUiState = MutableSharedFlow<FingerprintMapGroup>()

        coroutineScope.launch {
            rebuildUiState.collectLatest { fingerprintMaps ->
                _state.value = withContext(Dispatchers.Default) {
                    mutableListOf(
                        listItemCreator.create(
                            FingerprintMapId.SWIPE_DOWN,
                            fingerprintMaps.swipeDown
                        ),
                        listItemCreator.create(
                            FingerprintMapId.SWIPE_UP,
                            fingerprintMaps.swipeUp),
                        listItemCreator.create(
                            FingerprintMapId.SWIPE_LEFT,
                            fingerprintMaps.swipeLeft
                        ),
                        listItemCreator.create(
                            FingerprintMapId.SWIPE_RIGHT,
                            fingerprintMaps.swipeRight
                        )
                    ).createListState()
                }
            }
        }

        coroutineScope.launch {
            useCase.fingerprintMaps.collectLatest {
                rebuildUiState.emit(it)
            }
        }

        coroutineScope.launch {
            useCase.invalidateErrors.collectLatest {
                rebuildUiState.emit(useCase.fingerprintMaps.firstOrNull()?:return@collectLatest)
            }
        }
    }

    fun onEnabledSwitchChange(id: FingerprintMapId, checked: Boolean) {
        if (checked) {
            useCase.enableFingerprintMap(id)
        } else {
            useCase.disableFingerprintMap(id)
        }
    }

    fun onBackupAllClick() {
        TODO()
    }

    fun onResetClick() {
        TODO()
    }
}