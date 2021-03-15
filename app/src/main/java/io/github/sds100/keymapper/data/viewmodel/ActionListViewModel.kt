package io.github.sds100.keymapper.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hadilq.liveevent.LiveEvent
import io.github.sds100.keymapper.domain.actions.*
import io.github.sds100.keymapper.ui.actions.ActionListItemMapper
import io.github.sds100.keymapper.ui.actions.ActionListItemModel
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.delegate.ModelState
import io.github.sds100.keymapper.util.result.RecoverableError
import io.github.sds100.keymapper.util.result.errorOrNull
import io.github.sds100.keymapper.util.result.isError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Created by sds100 on 22/11/20.
 */

class ActionListViewModel<A : Action>(
    private val coroutineScope: CoroutineScope,
    private val configActions: ConfigActionsUseCase<A>,
    private val actionError: GetActionErrorUseCase,
    private val testAction: TestActionUseCase,
    private val listItemModelMapper: ActionListItemMapper<A>,
) : ModelState<List<ActionListItemModel>> {

    override val model = MutableLiveData<DataState<List<ActionListItemModel>>>(Loading())

    override val viewState = MutableLiveData<ViewState>(ViewLoading())

    private val _openEditOptions = LiveEvent<String>()

    /**
     * value is the uid of the action
     */
    val openEditOptions: LiveData<String> = _openEditOptions

    private val _fixError = LiveEvent<RecoverableError>()
    val fixError: LiveData<RecoverableError> = _fixError

    private val _enableAccessibilityServicePrompt = LiveEvent<Unit>()
    val enableAccessibilityServicePrompt: LiveData<Unit> = _enableAccessibilityServicePrompt

    init {
        configActions.actionList
            .onEach { buildModels(it) }
            .launchIn(coroutineScope)

        actionError.invalidateErrors.onEach { rebuildModels() }.launchIn(coroutineScope)
    }

    fun addAction(action: ActionData) = configActions.addAction(action)
    fun moveAction(fromIndex: Int, toIndex: Int) = configActions.moveAction(fromIndex, toIndex)
    fun removeAction(uid: String) = configActions.removeAction(uid)

    fun onModelClick(uid: String) {
        coroutineScope.launch {
            configActions.actionList.first().ifIsData { data ->
                val actionData = data.singleOrNull { it.uid == uid }?.data ?: return@launch

                val error = actionError.getError(actionData)

                when {
                    error.isError && error is RecoverableError -> _fixError.value = error

                    else -> testAction(actionData)
                }
            }
        }
    }

    fun promptToEnableAccessibilityService() {
        _enableAccessibilityServicePrompt.value = Unit
    }

    fun rebuildModels() {
        coroutineScope.launch {
            buildModels(configActions.actionList.first())
        }
    }

    private fun buildModels(actionList: DataState<List<A>>) {
        coroutineScope.launch {
            val newModel = actionList.mapData { data ->
                data.map {
                    listItemModelMapper.map(it, actionError.getError(it.data).errorOrNull())
                }
            }

            model.postValue(newModel)
        }
    }
}