package io.github.sds100.keymapper.data.viewmodel

import androidx.lifecycle.*
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.FingerprintMapId
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.home.HomeScreenUseCase
import io.github.sds100.keymapper.ui.ChipUi
import io.github.sds100.keymapper.ui.ListUiState
import io.github.sds100.keymapper.ui.createListState
import io.github.sds100.keymapper.ui.mappings.fingerprintmap.FingerprintMapListItemCreator
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.result.FixableError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class FingerprintMapListViewModel(
    private val coroutineScope: CoroutineScope,
    private val useCase: HomeScreenUseCase,
    resourceProvider: ResourceProvider,
) {

    private val listItemCreator = FingerprintMapListItemCreator(
        useCase,
        resourceProvider
    )

    private val _fixError = MutableSharedFlow<FixableError>()
    val fixError = _fixError.asSharedFlow()

    private val rebuildUiState = MutableSharedFlow<Unit>()

    val state = combine(
        useCase.fingerprintMaps,
        rebuildUiState,
        useCase.invalidateErrors
    ) { fingerprintMaps, _, _ ->
        mutableListOf(
            listItemCreator.create(FingerprintMapId.SWIPE_DOWN, fingerprintMaps.swipeDown),
            listItemCreator.create(FingerprintMapId.SWIPE_UP, fingerprintMaps.swipeUp),
            listItemCreator.create(FingerprintMapId.SWIPE_LEFT, fingerprintMaps.swipeLeft),
            listItemCreator.create(FingerprintMapId.SWIPE_RIGHT, fingerprintMaps.swipeRight),
        ).createListState()

    }.flowOn(Dispatchers.Default)
        .stateIn(coroutineScope, SharingStarted.Eagerly, ListUiState.Loading)

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

    fun onChipClick(fingerprintMapId: FingerprintMapId, chipModel: ChipUi) {
        if (chipModel is ChipUi.Error) {
            coroutineScope.launch {
                val actionUid = chipModel.id

                val fingerprintMap = useCase.fingerprintMaps.first().get(fingerprintMapId)

                val actionData = fingerprintMap.actionList
                    .singleOrNull { it.uid == actionUid }
                    ?.data
                    ?: return@launch

                val error = useCase.getActionError(actionData)

                if (error is FixableError) {
                    _fixError.emit(error)
                }
            }
        }
    }

    fun rebuildUiState() {
        runBlocking { rebuildUiState.emit(Unit) }
    }
}