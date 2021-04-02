package io.github.sds100.keymapper.data.viewmodel

import io.github.sds100.keymapper.domain.actions.GetActionErrorUseCase
import io.github.sds100.keymapper.domain.constraints.GetConstraintErrorUseCase
import io.github.sds100.keymapper.domain.mappings.keymap.GetKeymapListUseCase
import io.github.sds100.keymapper.domain.mappings.keymap.KeymapAction
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.ui.ChipUi
import io.github.sds100.keymapper.ui.ListUiState
import io.github.sds100.keymapper.ui.actions.ActionUiHelper
import io.github.sds100.keymapper.ui.constraints.ConstraintUiHelper
import io.github.sds100.keymapper.ui.createListState
import io.github.sds100.keymapper.ui.mappings.keymap.KeymapListItem
import io.github.sds100.keymapper.ui.mappings.keymap.KeymapListItemCreator
import io.github.sds100.keymapper.ui.utils.SelectionState
import io.github.sds100.keymapper.util.MultiSelectProvider
import io.github.sds100.keymapper.util.result.FixableError
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class KeymapListViewModel internal constructor(
    private val coroutineScope: CoroutineScope,
    private val getKeymapList: GetKeymapListUseCase,
    private val getActionError: GetActionErrorUseCase,
    actionUiHelper: ActionUiHelper<KeymapAction>,
    constraintUiHelper: ConstraintUiHelper,
    getConstraintErrorUseCase: GetConstraintErrorUseCase,
    resourceProvider: ResourceProvider,
    private val multiSelectProvider: MultiSelectProvider
) {

    private val modelCreator = KeymapListItemCreator(
        getActionError,
        actionUiHelper,
        constraintUiHelper,
        getConstraintErrorUseCase,
        resourceProvider
    )

    private val _state = MutableStateFlow<ListUiState<KeymapListItem>>(ListUiState.Loading)
    val state = _state.asStateFlow()

    /**
     * The database id of the key map
     */
    private val _launchConfigKeymap = MutableSharedFlow<String>()
    val launchConfigKeymap = _launchConfigKeymap.asSharedFlow()

    private val _fixError = MutableSharedFlow<FixableError>()
    val fixError = _fixError.asSharedFlow()

    private val rebuildUiState = MutableSharedFlow<Unit>()

    init {
        val keymapStateListFlow =
            MutableStateFlow<ListUiState<KeymapListItem.KeymapUiState>>(ListUiState.Loading)

        coroutineScope.launch {
            combine(
                rebuildUiState,
                getKeymapList.keymapList,
                getActionError.invalidateErrors
            ) { _, keymapList, _ ->
                keymapList
            }.collectLatest { keymapList ->
                //don't show progress bar because when swiping between tabs the recycler view will flash
                keymapStateListFlow.value = withContext(Dispatchers.Default) {
                    keymapList.map { modelCreator.map(it) }.createListState()
                }
            }
        }

        coroutineScope.launch {
            combine(
                keymapStateListFlow,
                multiSelectProvider.state
            ) { keymapListState, selectionState ->
                Pair(keymapListState, selectionState)
            }.collectLatest { pair ->
                val (keymapUiListState, selectionState) = pair

                when (keymapUiListState) {
                    ListUiState.Empty -> _state.value = ListUiState.Empty
                    ListUiState.Loading -> _state.value = ListUiState.Loading

                    is ListUiState.Loaded -> {

                        val isSelectable = selectionState is SelectionState.Selecting

                        _state.value = withContext(Dispatchers.Default) {
                            keymapUiListState.data.map { keymapUiState ->
                                val isSelected = if (selectionState is SelectionState.Selecting) {
                                    selectionState.selectedIds.contains(keymapUiState.uid)
                                } else {
                                    false
                                }

                                KeymapListItem(
                                    keymapUiState,
                                    KeymapListItem.SelectionUiState(isSelected, isSelectable)
                                )
                            }.createListState()
                        }
                    }
                }
            }
        }
    }

    fun onKeymapCardClick(uid: String) {
        if (multiSelectProvider.state.value is SelectionState.Selecting) {
            multiSelectProvider.toggleSelection(uid)
        } else {
            coroutineScope.launch {
                _launchConfigKeymap.emit(uid)
            }
        }
    }

    fun onKeymapCardLongClick(uid: String) {
        if (multiSelectProvider.state.value is SelectionState.NotSelecting) {
            multiSelectProvider.startSelecting()
            multiSelectProvider.select(uid)
        }
    }

    fun selectAll() {
        coroutineScope.launch {
            state.value.apply {
                if (this is ListUiState.Loaded) {
                    multiSelectProvider.select(*this.data.map { it.keymapUiState.uid }
                        .toTypedArray())
                }
            }
        }
    }

    fun onChipClick(keymapUid: String, chipModel: ChipUi) {
        if (chipModel is ChipUi.Error) {
            coroutineScope.launch {
                val actionUid = chipModel.id
                val actionData = getKeymapList.keymapList.first()
                    .singleOrNull { keyMap -> keyMap.uid == keymapUid }
                    ?.actionList
                    ?.singleOrNull { action -> action.uid == actionUid }
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
}