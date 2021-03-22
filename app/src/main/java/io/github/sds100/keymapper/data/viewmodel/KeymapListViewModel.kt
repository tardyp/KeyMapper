package io.github.sds100.keymapper.data.viewmodel

import io.github.sds100.keymapper.domain.actions.GetActionErrorUseCase
import io.github.sds100.keymapper.domain.constraints.GetConstraintErrorUseCase
import io.github.sds100.keymapper.domain.mappings.keymap.KeyMap
import io.github.sds100.keymapper.domain.mappings.keymap.KeymapAction
import io.github.sds100.keymapper.domain.mappings.keymap.ListKeymapsUseCase
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.ui.ChipUi
import io.github.sds100.keymapper.ui.ListUiState
import io.github.sds100.keymapper.ui.actions.ActionUiHelper
import io.github.sds100.keymapper.ui.callback.OnChipClickCallback
import io.github.sds100.keymapper.ui.constraints.ConstraintUiHelper
import io.github.sds100.keymapper.ui.createListState
import io.github.sds100.keymapper.ui.mappings.keymap.KeymapListItemCreator
import io.github.sds100.keymapper.ui.mappings.keymap.KeymapListItemModel
import io.github.sds100.keymapper.ui.utils.SelectionState
import io.github.sds100.keymapper.util.MultiSelectProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class KeymapListViewModel internal constructor(
    private val coroutineScope: CoroutineScope,
    private val useCase: ListKeymapsUseCase,
    private val getActionError: GetActionErrorUseCase,
    actionUiHelper: ActionUiHelper<KeymapAction>,
    constraintUiHelper: ConstraintUiHelper,
    getConstraintErrorUseCase: GetConstraintErrorUseCase,
    resourceProvider: ResourceProvider,
    private val multiSelectProvider: MultiSelectProvider
) : OnChipClickCallback {

    private val modelCreator = KeymapListItemCreator(
        getActionError,
        actionUiHelper,
        constraintUiHelper,
        getConstraintErrorUseCase,
        resourceProvider
    )

    private val _state = MutableStateFlow<ListUiState<KeymapListItemModel>>(ListUiState.Loading)
    val state = _state.asStateFlow()

    /**
     * The database id of the key map
     */
    private val _launchConfigKeymap = MutableSharedFlow<Long>()
    val launchConfigKeymap = _launchConfigKeymap.asSharedFlow()

    private val rebuildUiState = MutableSharedFlow<Unit>()

    init {
        coroutineScope.launch {
            combine(
                rebuildUiState,
                useCase.keymapList,
                multiSelectProvider.state,
                getActionError.invalidateErrors
            ) { _, keymapList, selectionState, _ ->
                UiBuilder(keymapList, selectionState)
            }.collectLatest {
                _state.value = it.build()
            }
        }
    }

    fun onKeymapCardClick(uid: String) {
        coroutineScope.launch {
            val dbId = getKeymapDbIdFromUid(uid) ?: return@launch

            if (multiSelectProvider.state.value is SelectionState.Selecting) {
                multiSelectProvider.toggleSelection(dbId)
            } else {
                _launchConfigKeymap.emit(dbId)
            }
        }
    }

    fun onKeymapCardLongClick(uid: String) {
        coroutineScope.launch {
            val dbId = getKeymapDbIdFromUid(uid) ?: return@launch

            if (multiSelectProvider.state.value is SelectionState.NotSelecting) {
                multiSelectProvider.startSelecting()
                multiSelectProvider.select(dbId)
            }
        }
    }

    fun selectAll() {
        coroutineScope.launch {
            useCase.keymapList.first()
                .map { it.dbId }
                .toLongArray()
                .let { multiSelectProvider.select(*it) }
        }
    }

    override fun onChipClick(chipModel: ChipUi) {
        TODO("Not yet implemented")
    }

    fun rebuildUiState() {
        runBlocking { rebuildUiState.emit(Unit) }
    }

    private suspend fun getKeymapDbIdFromUid(uid: String): Long? {
        return useCase.keymapList.first().singleOrNull { it.uid == uid }?.dbId
    }

    private inner class UiBuilder(
        private val keymapList: List<KeyMap>,
        private val selectionState: SelectionState
    ) {
        fun build(): ListUiState<KeymapListItemModel> {
            return keymapList.map { keyMap ->
                modelCreator.map(
                    keyMap,
                    (selectionState as? SelectionState.Selecting)
                        ?.selectedIds?.contains(keyMap.dbId) ?: false,
                    selectionState is SelectionState.Selecting
                )
            }.createListState()
        }
    }
}