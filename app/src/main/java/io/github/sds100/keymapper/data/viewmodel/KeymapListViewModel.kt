package io.github.sds100.keymapper.data.viewmodel

import io.github.sds100.keymapper.domain.actions.GetActionErrorUseCase
import io.github.sds100.keymapper.domain.constraints.GetConstraintErrorUseCase
import io.github.sds100.keymapper.domain.mappings.keymap.KeymapAction
import io.github.sds100.keymapper.domain.mappings.keymap.ListKeymapsUseCase
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.ui.ChipUi
import io.github.sds100.keymapper.ui.ListUiState
import io.github.sds100.keymapper.ui.actions.ActionUiHelper
import io.github.sds100.keymapper.ui.callback.OnChipClickCallback
import io.github.sds100.keymapper.ui.constraints.ConstraintUiHelper
import io.github.sds100.keymapper.ui.createListState
import io.github.sds100.keymapper.ui.mappings.keymap.KeymapListItem
import io.github.sds100.keymapper.ui.mappings.keymap.KeymapListItemCreator
import io.github.sds100.keymapper.ui.utils.SelectionState
import io.github.sds100.keymapper.util.MultiSelectProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

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

    private val _state = MutableStateFlow<ListUiState<KeymapListItem>>(ListUiState.Loading)
    val state = _state.asStateFlow()

    /**
     * The database id of the key map
     */
    private val _launchConfigKeymap = MutableSharedFlow<String>()
    val launchConfigKeymap = _launchConfigKeymap.asSharedFlow()

    private val rebuildUiState = MutableSharedFlow<Unit>()

    init {
        val keymapStateListFlow =
            MutableStateFlow<ListUiState<KeymapListItem.KeymapUiState>>(ListUiState.Loading)

        coroutineScope.launch {
            combine(
                rebuildUiState,
                useCase.keymapList,
                getActionError.invalidateErrors
            ) { _, keymapList, _ ->
                keymapList
            }.collectLatest { keymapList ->
                keymapStateListFlow.value = ListUiState.Loading

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

    override fun onChipClick(chipModel: ChipUi) {
        TODO("Not yet implemented")
    }

    fun rebuildUiState() {
        runBlocking { rebuildUiState.emit(Unit) }
    }
}