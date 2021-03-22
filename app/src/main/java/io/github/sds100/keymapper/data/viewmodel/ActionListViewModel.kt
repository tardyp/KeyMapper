package io.github.sds100.keymapper.data.viewmodel

import io.github.sds100.keymapper.domain.actions.*
import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.domain.utils.ifIsData
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.ui.ListUiState
import io.github.sds100.keymapper.ui.actions.ActionListItem
import io.github.sds100.keymapper.ui.actions.ActionListItemCreator
import io.github.sds100.keymapper.ui.actions.ActionUiHelper
import io.github.sds100.keymapper.ui.createListState
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.result.RecoverableError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Created by sds100 on 22/11/20.
 */

class ActionListViewModel<A : Action>(
    private val coroutineScope: CoroutineScope,
    private val configActions: ConfigActionsUseCase<A>,
    private val actionError: GetActionErrorUseCase,
    private val testAction: TestActionUseCase,
    actionUiHelper: ActionUiHelper<A>,
    resourceProvider: ResourceProvider
) {

    private val _state = MutableStateFlow<ListUiState<ActionListItem>>(ListUiState.Loading)
    val state = _state.asStateFlow()

    private val modelCreator = ActionListItemCreator(actionUiHelper, actionError, resourceProvider)
    private val _openEditOptions = MutableSharedFlow<String>()

    /**
     * value is the uid of the action
     */
    val openEditOptions = _openEditOptions.asSharedFlow()

    private val _fixError = MutableSharedFlow<RecoverableError>()
    val fixError = _fixError.asSharedFlow()

    private val _enableAccessibilityServicePrompt = MutableSharedFlow<Unit>()
    val enableAccessibilityServicePrompt = _enableAccessibilityServicePrompt.asSharedFlow()

    private val _chooseAction = MutableSharedFlow<Unit>()
    val chooseAction = _chooseAction.asSharedFlow()

    private val rebuildUiState = MutableSharedFlow<Unit>()

    init {
        coroutineScope.launch {
            combine(
                rebuildUiState,
                configActions.actionList,
                actionError.invalidateErrors
            ) { _, actionList, _ ->
                actionList
            }.collectLatest { actionList ->
                when (actionList) {
                    is State.Data -> _state.value = buildModels(actionList.data).createListState()

                    is State.Loading -> _state.value = ListUiState.Loading
                }
            }
        }
    }

    fun addAction(action: ActionData) = configActions.addAction(action)
    fun moveAction(fromIndex: Int, toIndex: Int) = configActions.moveAction(fromIndex, toIndex)
    fun removeAction(uid: String) = configActions.removeAction(uid)

    fun onModelClick(uid: String) {
        coroutineScope.launch {
            configActions.actionList.first().ifIsData { data ->
                val actionData = data.singleOrNull { it.uid == uid }?.data ?: return@launch

                actionError.getError(actionData)?.let { error ->
                    when (error) {
                        is RecoverableError -> _fixError.emit(error)
                        else -> testAction(actionData)
                    }
                }
            }
        }
    }

    fun promptToEnableAccessibilityService() {
        runBlocking {
            _enableAccessibilityServicePrompt.emit(Unit)
        }
    }

    fun onAddActionClick() {
        coroutineScope.launch {
            _chooseAction.emit(Unit)
        }
    }

    fun rebuildUiState() {
        runBlocking { rebuildUiState.emit(Unit) }
    }

    private fun buildModels(actionList: List<A>) = actionList.map {
        modelCreator.map(it, actionList.size)
    }
}