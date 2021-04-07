package io.github.sds100.keymapper.data.viewmodel

import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.home.HomeScreenUseCase
import io.github.sds100.keymapper.mappings.common.BaseMappingListViewModel
import io.github.sds100.keymapper.ui.*
import io.github.sds100.keymapper.ui.dialogs.RequestUserResponse
import io.github.sds100.keymapper.ui.mappings.keymap.KeyMapListItem
import io.github.sds100.keymapper.ui.mappings.keymap.KeymapListItemCreator
import io.github.sds100.keymapper.ui.utils.SelectionState
import io.github.sds100.keymapper.util.MultiSelectProvider
import io.github.sds100.keymapper.util.result.FixableError
import io.github.sds100.keymapper.util.result.getFullMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class KeymapListViewModel internal constructor(
    private val coroutineScope: CoroutineScope,
    private val useCase: HomeScreenUseCase,
    resourceProvider: ResourceProvider,
    private val multiSelectProvider: MultiSelectProvider
) : BaseMappingListViewModel(coroutineScope, resourceProvider) {

    private val listItemCreator = KeymapListItemCreator(useCase, resourceProvider)

    private val _state = MutableStateFlow<ListUiState<KeyMapListItem>>(ListUiState.Loading)
    val state = _state.asStateFlow()

    /**
     * The database id of the key map
     */
    private val _launchConfigKeymap = MutableSharedFlow<String>()
    val launchConfigKeymap = _launchConfigKeymap.asSharedFlow()

    private val rebuildUiState = MutableSharedFlow<Unit>()

    init {
        val keymapStateListFlow =
            MutableStateFlow<ListUiState<KeyMapListItem.KeyMapUiState>>(ListUiState.Loading)

        coroutineScope.launch {
            combine(
                rebuildUiState,
                useCase.keymapList,
                useCase.invalidateErrors
            ) { _, keymapListState, _ ->
                keymapListState
            }.collectLatest { keymapListState ->
                //don't show progress bar because when swiping between tabs the recycler view will flash
                keymapStateListFlow.value = withContext(Dispatchers.Default) {
                    when (keymapListState) {
                        is State.Data -> keymapListState.data.map { listItemCreator.map(it) }
                            .createListState()

                        State.Loading -> ListUiState.Loading
                    }
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

                                KeyMapListItem(
                                    keymapUiState,
                                    KeyMapListItem.SelectionUiState(isSelected, isSelectable)
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
                    multiSelectProvider.select(*this.data.map { it.keyMapUiState.uid }
                        .toTypedArray())
                }
            }
        }
    }

    fun rebuildUiState() {
        runBlocking { rebuildUiState.emit(Unit) }
    }
}