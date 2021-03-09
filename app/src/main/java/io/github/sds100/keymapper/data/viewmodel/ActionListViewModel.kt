package io.github.sds100.keymapper.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hadilq.liveevent.LiveEvent
import io.github.sds100.keymapper.ui.actions.ActionListItemModel
import io.github.sds100.keymapper.domain.actions.ActionWithOptions
import io.github.sds100.keymapper.domain.actions.ConfigActionsUseCase
import io.github.sds100.keymapper.domain.actions.GetActionErrorUseCase
import io.github.sds100.keymapper.domain.actions.TestActionUseCase
import io.github.sds100.keymapper.domain.models.Action
import io.github.sds100.keymapper.ui.actions.ActionListItemMapper
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.delegate.ModelState
import io.github.sds100.keymapper.util.result.RecoverableError
import io.github.sds100.keymapper.util.result.errorOrNull
import io.github.sds100.keymapper.util.result.isError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Created by sds100 on 22/11/20.
 */

class ActionListViewModel<O>(
    private val coroutineScope: CoroutineScope,
    private val configActions: ConfigActionsUseCase<O>,
    private val actionError: GetActionErrorUseCase,
    private val testAction: TestActionUseCase,
    private val listItemModelMapper: ActionListItemMapper<O>,
) : ModelState<List<ActionListItemModel>> {

    override val model = MutableLiveData<DataState<List<ActionListItemModel>>>(Loading())

    override val viewState = MutableLiveData<ViewState>(ViewLoading())

    private val _openEditOptions = LiveEvent<O>()
    val openEditOptions: LiveData<O> = _openEditOptions

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

    fun addAction(action: Action) = configActions.addAction(action)
    fun moveAction(fromIndex: Int, toIndex: Int) = configActions.moveAction(fromIndex, toIndex)
    fun removeAction(uid: String) = configActions.removeAction(uid)

    fun editOptions(uid: String) {
        _openEditOptions.value = configActions.getOptions(uid)
    }

    fun onModelClick(uid: String) = configActions.actionList.value.ifIsData { data ->
        val action = data.singleOrNull { it.action.uid == uid }?.action ?: return

        val error = actionError.getError(action)

        when {
            error.isError && error is RecoverableError -> _fixError.value = error

            else -> testAction(action)
        }
    }

    fun setOptions(uid: String, options: O) {
        configActions.setOptions(uid, options)
    }

    fun promptToEnableAccessibilityService() {
        _enableAccessibilityServicePrompt.value = Unit
    }

    fun rebuildModels() = buildModels(configActions.actionList.value)

    private fun buildModels(actionList: DataState<List<ActionWithOptions<O>>>) {
        coroutineScope.launch {
            val newModel = actionList.mapData { data ->
                data.map {
                    listItemModelMapper.map(it, actionError.getError(it.action).errorOrNull())
                }
            }

            model.postValue(newModel)
        }
    }
}