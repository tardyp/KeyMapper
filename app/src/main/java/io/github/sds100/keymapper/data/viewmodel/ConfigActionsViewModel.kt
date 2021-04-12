package io.github.sds100.keymapper.data.viewmodel

import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.domain.actions.*
import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.domain.utils.ifIsData
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.mappings.common.ConfigMappingUseCase
import io.github.sds100.keymapper.mappings.common.DisplayActionUseCase
import io.github.sds100.keymapper.mappings.common.Mapping
import io.github.sds100.keymapper.mappings.common.isDelayBeforeNextActionAllowed
import io.github.sds100.keymapper.ui.IconInfo
import io.github.sds100.keymapper.ui.ListUiState
import io.github.sds100.keymapper.ui.actions.ActionListItem
import io.github.sds100.keymapper.ui.actions.ActionUiHelper
import io.github.sds100.keymapper.ui.createListState
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.result.Error
import io.github.sds100.keymapper.util.result.FixableError
import io.github.sds100.keymapper.util.result.getFullMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Created by sds100 on 22/11/20.
 */

class ConfigActionsViewModel<A : Action, M : Mapping<A>>(
    private val coroutineScope: CoroutineScope,
    private val displayActionUseCase: DisplayActionUseCase,
    private val testAction: TestActionUseCase,
    private val config: ConfigMappingUseCase<A, M>,
    private val uiHelper: ActionUiHelper<M, A>,
    resourceProvider: ResourceProvider
) : ResourceProvider by resourceProvider {

    private val _state = MutableStateFlow<ListUiState<ActionListItem>>(ListUiState.Loading)
    val state = _state.asStateFlow()

    private val _openEditOptions = MutableSharedFlow<String>()

    /**
     * value is the uid of the action
     */
    val openEditOptions = _openEditOptions.asSharedFlow()

    private val _fixError = MutableSharedFlow<FixableError>()
    val fixError = _fixError.asSharedFlow()


    init {
        val rebuildUiState = MutableSharedFlow<State<M>>()

        coroutineScope.launch {
            rebuildUiState.collectLatest { mapping ->
                when (mapping) {
                    is State.Data -> withContext(Dispatchers.Default) {
                        _state.value = createListItems(mapping.data).createListState()
                    }

                    is State.Loading -> _state.value = ListUiState.Loading
                }
            }
        }

        coroutineScope.launch {
            config.mapping.collectLatest {
                rebuildUiState.emit(it)
            }
        }

        coroutineScope.launch {
            displayActionUseCase.invalidateErrors.collectLatest {
                rebuildUiState.emit(config.mapping.firstOrNull() ?: return@collectLatest)
            }
        }
    }

    fun onModelClick(uid: String) {
        coroutineScope.launch(Dispatchers.Default) {
            config.mapping.first().ifIsData { data ->
                val actionData = data.actionList.singleOrNull { it.uid == uid }?.data
                    ?: return@launch

                displayActionUseCase.getActionError(actionData)?.let { error ->
                    when (error) {
                        is FixableError -> _fixError.emit(error)
                        else -> testAction(actionData)
                    }
                }
            }
        }
    }

    fun addAction(data: ActionData) {
        config.addAction(data)
    }

    fun moveAction(fromIndex: Int, toIndex: Int) {
        config.moveAction(fromIndex, toIndex)
    }

    fun onRemoveClick(actionUid: String) {
        config.removeAction(actionUid)
    }

    fun editOptions(actionUid: String) {
        runBlocking { _openEditOptions.emit(actionUid) }
    }

    private fun createListItems(mapping: M): List<ActionListItem> {
        val actionCount = mapping.actionList.size

        return mapping.actionList.map { action ->

            val title: String = if (action.multiplier != null && action.multiplier!! > 1) {
                val multiplier = action.multiplier
                "${multiplier}x ${uiHelper.getTitle(action.data)}"
            } else {
                uiHelper.getTitle(action.data)
            }

            val icon: IconInfo? = uiHelper.getIcon(action.data)
            val error: Error? = uiHelper.getError(action.data)

            val extraInfo = buildString {
                val midDot = getString(R.string.middot)

                uiHelper.getOptionLabels(mapping, action).forEachIndexed { index, label ->
                    if (index != 0) {
                        append(" $midDot ")
                    }

                    append(label)
                }

                action.delayBeforeNextAction.apply {
                    if (mapping.isDelayBeforeNextActionAllowed() && action.delayBeforeNextAction != null) {
                        if (this@buildString.isNotBlank()) {
                            append(" $midDot ")
                        }

                        append(
                            getString(
                                R.string.action_title_wait,
                                action.delayBeforeNextAction!!
                            )
                        )
                    }
                }
            }.takeIf { it.isNotBlank() }

            ActionListItem(
                id = action.uid,
                tintType = if (error != null) {
                    TintType.ERROR
                } else {
                    icon?.tintType ?: TintType.NONE
                },
                icon = if (error != null) {
                    getDrawable(R.drawable.ic_baseline_error_outline_24)
                } else {
                    icon?.drawable
                },
                title = title,
                extraInfo = extraInfo,
                errorMessage = error?.getFullMessage(this),
                dragAndDrop = actionCount > 1
            )
        }
    }
}