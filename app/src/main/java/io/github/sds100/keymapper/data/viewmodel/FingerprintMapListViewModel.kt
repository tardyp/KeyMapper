package io.github.sds100.keymapper.data.viewmodel

import androidx.lifecycle.*
import io.github.sds100.keymapper.domain.actions.GetActionErrorUseCase
import io.github.sds100.keymapper.domain.constraints.GetConstraintErrorUseCase
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.EnableDisableFingerprintMapsUseCase
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.FingerprintMapAction
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.FingerprintMapId
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.GetFingerprintMapUseCase
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.ui.ChipUi
import io.github.sds100.keymapper.ui.ListUiState
import io.github.sds100.keymapper.ui.actions.ActionUiHelper
import io.github.sds100.keymapper.ui.constraints.ConstraintUiHelper
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
    private val get: GetFingerprintMapUseCase,
    private val enableDisableUseCase: EnableDisableFingerprintMapsUseCase,
    private val getActionError: GetActionErrorUseCase,
    actionUiHelper: ActionUiHelper<FingerprintMapAction>,
    constraintUiHelper: ConstraintUiHelper,
    getConstraintErrorUseCase: GetConstraintErrorUseCase,
    resourceProvider: ResourceProvider,
) {

    private val listItemCreator = FingerprintMapListItemCreator(
        getActionError,
        actionUiHelper,
        constraintUiHelper,
        getConstraintErrorUseCase,
        resourceProvider
    )

    private val _fixError = MutableSharedFlow<FixableError>()
    val fixError = _fixError.asSharedFlow()

    private val rebuildUiState = MutableSharedFlow<Unit>()

    val state = combine(
        get.swipeDown,
        get.swipeUp,
        get.swipeLeft,
        get.swipeRight,
        merge(rebuildUiState, getActionError.invalidateErrors)
    ) { swipeDown, swipeUp, swipeLeft, swipeRight, _ ->
        mutableListOf(
            listItemCreator.map(FingerprintMapId.SWIPE_DOWN, swipeDown),
            listItemCreator.map(FingerprintMapId.SWIPE_UP, swipeUp),
            listItemCreator.map(FingerprintMapId.SWIPE_LEFT, swipeLeft),
            listItemCreator.map(FingerprintMapId.SWIPE_RIGHT, swipeRight),
        ).createListState()

    }.flowOn(Dispatchers.Default)
        .stateIn(coroutineScope, SharingStarted.Eagerly, ListUiState.Loading)

    //TODO
    fun setEnabled(id: FingerprintMapId, isEnabled: Boolean) {
        if (isEnabled) {
            enableDisableUseCase.enable(id)
        } else {
            enableDisableUseCase.disable(id)
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

                val fingerprintMap = when (fingerprintMapId) {
                    FingerprintMapId.SWIPE_DOWN -> get.swipeDown.first()
                    FingerprintMapId.SWIPE_UP -> get.swipeUp.first()
                    FingerprintMapId.SWIPE_LEFT -> get.swipeLeft.first()
                    FingerprintMapId.SWIPE_RIGHT -> get.swipeRight.first()
                }

                val actionData = fingerprintMap.actionList
                    .singleOrNull { it.uid == actionUid }
                    ?.data
                    ?: return@launch

                val error = getActionError.getError(actionData)

                if (error is FixableError) {
                    _fixError.emit(error)
                }
            }
        }
    }

    fun rebuildUiState() {
        runBlocking { rebuildUiState.emit(Unit) }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val coroutineScope: CoroutineScope,
        private val get: GetFingerprintMapUseCase,
        private val enableDisableUseCase: EnableDisableFingerprintMapsUseCase,
        private val getActionError: GetActionErrorUseCase,
        private val actionUiHelper: ActionUiHelper<FingerprintMapAction>,
        private val constraintUiHelper: ConstraintUiHelper,
        private val getConstraintErrorUseCase: GetConstraintErrorUseCase,
        private val resourceProvider: ResourceProvider,
    ) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return FingerprintMapListViewModel(
                coroutineScope,
                get,
                enableDisableUseCase,
                getActionError,
                actionUiHelper,
                constraintUiHelper,
                getConstraintErrorUseCase,
                resourceProvider
            ) as T
        }
    }
}