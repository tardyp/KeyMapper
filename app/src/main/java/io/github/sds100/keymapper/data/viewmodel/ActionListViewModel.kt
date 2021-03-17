package io.github.sds100.keymapper.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hadilq.liveevent.LiveEvent
import io.github.sds100.keymapper.domain.actions.*
import io.github.sds100.keymapper.ui.actions.ActionListItemMapper
import io.github.sds100.keymapper.ui.actions.ActionListItemState
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.delegate.ModelState
import io.github.sds100.keymapper.util.result.RecoverableError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
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
) : ModelState<List<ActionListItemState>> {

    override val model = MutableLiveData<DataState<List<ActionListItemState>>>(Loading())

    override val viewState = MutableLiveData<ViewState>(ViewLoading())

    private val _openEditOptions = MutableSharedFlow<String>()

    /**
     * value is the uid of the action
     */
    val openEditOptions = _openEditOptions.asSharedFlow()

    private val _fixError = LiveEvent<RecoverableError>()
    val fixError: LiveData<RecoverableError> = _fixError

    private val _enableAccessibilityServicePrompt = LiveEvent<Unit>()
    val enableAccessibilityServicePrompt: LiveData<Unit> = _enableAccessibilityServicePrompt

    init {
        combine(
            configActions.actionList,
            actionError.invalidateErrors
        ) { models, _ ->
            model.value = models.mapData { buildModels(it) }
        }.launchIn(coroutineScope)
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
                        is RecoverableError -> _fixError.value = error
                        else -> testAction(actionData)
                    }
                }
            }
        }
    }

    fun promptToEnableAccessibilityService() {
        _enableAccessibilityServicePrompt.value = Unit
    }

    override fun rebuildModels() {
        coroutineScope.launch {
            model.value = configActions.actionList.first().mapData {
                buildModels(it)
            }
        }
    }

    private fun buildModels(actionList: List<A>) = actionList.map {
        listItemModelMapper.map(
            it,
            actionError.getError(it.data),
            actionList.size
        )
    }
}