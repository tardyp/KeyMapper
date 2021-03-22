package io.github.sds100.keymapper.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
import io.github.sds100.keymapper.util.ISelectionProvider
import io.github.sds100.keymapper.util.SelectionProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

//TODO move to homeviewmodel
class KeymapListViewModel internal constructor(
    private val useCase: ListKeymapsUseCase,
    private val getActionError: GetActionErrorUseCase,
    actionUiHelper: ActionUiHelper<KeymapAction>,
    constraintUiHelper: ConstraintUiHelper,
    getConstraintErrorUseCase: GetConstraintErrorUseCase,
    resourceProvider: ResourceProvider
) : ViewModel(), OnChipClickCallback {

    private val selectionProvider: ISelectionProvider = SelectionProvider()
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
        viewModelScope.launch {
            combine(
                rebuildUiState,
                useCase.keymapList,
                selectionProvider.isSelectable,
                selectionProvider.selectedIds,
                getActionError.invalidateErrors
            ) { _, keymapList, isSelectable, selectedIds, _ ->
                UiBuilder(keymapList, isSelectable, selectedIds)
            }.collectLatest {
                _state.value = it.build()
            }
        }
    }

    fun duplicateSelectedKeymaps() {
        TODO()
    }

    fun delete(vararg id: Long) {
        TODO()
    }

    fun enableSelectedKeymaps() {
        TODO()
    }

    fun disableSelectedKeymaps() {
        TODO()
    }

    fun enableAll() {
        TODO()
    }

    fun disableAll() {

    }

    fun backupSelectedKeymaps() {
        //TODO
    }

    fun onKeymapCardClick(uid: String) {
        viewModelScope.launch {
            val dbId = getKeymapDbIdFromUid(uid) ?: return@launch

            if (selectionProvider.isSelectable.value) {
                selectionProvider.select(dbId)
            } else {
                _launchConfigKeymap.emit(dbId)
            }
        }
    }

    fun onKeymapCardLongClick(uid: String) {
        viewModelScope.launch {
            val dbId = getKeymapDbIdFromUid(uid) ?: return@launch

            if (!selectionProvider.isSelectable.value) {
                selectionProvider.startSelecting()
                selectionProvider.select(dbId)
            }
        }
    }

    fun selectAll() {
        viewModelScope.launch {
            useCase.keymapList.first()
                .map { it.dbId }
                .toLongArray()
                .let { selectionProvider.select(*it) }
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
        private val isSelectable: Boolean,
        private val selectedIds: Set<Long>
    ) {
        fun build(): ListUiState<KeymapListItemModel> {
            return keymapList.map { keyMap ->
                modelCreator.map(
                    keyMap,
                    isSelectable,
                    selectedIds.contains(keyMap.dbId)
                )
            }.createListState()
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val useCase: ListKeymapsUseCase,
        private val getActionError: GetActionErrorUseCase,
        private val actionUiHelper: ActionUiHelper<KeymapAction>,
        private val constraintUiHelper: ConstraintUiHelper,
        private val getConstraintErrorUseCase: GetConstraintErrorUseCase,
        private val resourceProvider: ResourceProvider
    ) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return KeymapListViewModel(
                useCase,
                getActionError,
                actionUiHelper,
                constraintUiHelper,
                getConstraintErrorUseCase,
                resourceProvider
            ) as T
        }
    }
}