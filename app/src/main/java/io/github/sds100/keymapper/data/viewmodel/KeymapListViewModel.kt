package io.github.sds100.keymapper.data.viewmodel

import io.github.sds100.keymapper.domain.mappings.keymap.KeyMap
import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.home.HomeScreenUseCase
import io.github.sds100.keymapper.mappings.common.BaseMappingListViewModel
import io.github.sds100.keymapper.ui.ListUiState
import io.github.sds100.keymapper.ui.createListState
import io.github.sds100.keymapper.ui.mappings.keymap.KeyMapListItem
import io.github.sds100.keymapper.ui.mappings.keymap.KeyMapListItemCreator
import io.github.sds100.keymapper.ui.utils.SelectionState
import io.github.sds100.keymapper.util.MultiSelectProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber

class KeymapListViewModel internal constructor(
    private val coroutineScope: CoroutineScope,
    private val useCase: HomeScreenUseCase,
    resourceProvider: ResourceProvider,
    private val multiSelectProvider: MultiSelectProvider
) : BaseMappingListViewModel(coroutineScope, resourceProvider) {

    private val listItemCreator = KeyMapListItemCreator(useCase, resourceProvider)

    private val _state = MutableStateFlow<ListUiState<KeyMapListItem>>(ListUiState.Loading)
    val state = _state.asStateFlow()

    /**
     * The database id of the key map
     */
    private val _launchConfigKeyMap = MutableSharedFlow<String>()
    val launchConfigKeymap = _launchConfigKeyMap.asSharedFlow()

    init {
        val keyMapStateListFlow =
            MutableStateFlow<ListUiState<KeyMapListItem.KeyMapUiState>>(ListUiState.Loading)

        val rebuildUiState = MutableSharedFlow<State<List<KeyMap>>>()

        coroutineScope.launch {
            rebuildUiState.collectLatest { keyMapListState ->
                keyMapStateListFlow.value = ListUiState.Loading

                keyMapStateListFlow.value = withContext(Dispatchers.Default) {
                    when (keyMapListState) {
                        is State.Data -> keyMapListState.data.map { listItemCreator.map(it) }
                            .createListState()

                        State.Loading -> ListUiState.Loading
                    }
                }
            }
        }

        coroutineScope.launch {
            useCase.keyMapList.collectLatest {
                rebuildUiState.emit(it)
            }
        }

        coroutineScope.launch {
            useCase.invalidateErrors.drop(1).collectLatest {
                rebuildUiState.emit(useCase.keyMapList.firstOrNull() ?: return@collectLatest)
            }
        }

        coroutineScope.launch {
            combine(
                keyMapStateListFlow,
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
                _launchConfigKeyMap.emit(uid)
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
}