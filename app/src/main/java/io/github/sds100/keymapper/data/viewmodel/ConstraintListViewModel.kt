package io.github.sds100.keymapper.data.viewmodel

import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.domain.constraints.*
import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.domain.utils.ifIsData
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.ui.ListUiState
import io.github.sds100.keymapper.ui.constraints.ConstraintListItem
import io.github.sds100.keymapper.ui.constraints.ConstraintListItemCreator
import io.github.sds100.keymapper.ui.constraints.ConstraintUiHelper
import io.github.sds100.keymapper.ui.createListState
import io.github.sds100.keymapper.util.result.Error
import io.github.sds100.keymapper.util.result.FixableError
import io.github.sds100.keymapper.util.result.onFailure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Created by sds100 on 29/11/20.
 */

class ConstraintListViewModel(
    private val coroutineScope: CoroutineScope,
    private val useCase: ConfigConstraintsUseCase,
    private val uiHelper: ConstraintUiHelper,
    private val getError: GetConstraintErrorUseCase,
    resourceProvider: ResourceProvider
) : ResourceProvider by resourceProvider {

    private val modelCreator = ConstraintListItemCreator(uiHelper, getError, resourceProvider)

    private val _showToast = MutableSharedFlow<String>()
    val showToast = _showToast.asSharedFlow()

    private val _fixError = MutableSharedFlow<FixableError>()
    val fixError = _fixError.asSharedFlow()

    private val _state = MutableStateFlow(buildState(State.Loading))
    val state = _state.asStateFlow()

    private val rebuildUiState = MutableSharedFlow<Unit>()

    init {
        coroutineScope.launch {
            combine(rebuildUiState, useCase.state) { _, state ->
                state
            }.collectLatest { state ->
                this@ConstraintListViewModel._state.value = buildState(state)
            }
        }
    }

    fun addConstraint(constraint: Constraint) {
        useCase.addConstraint(constraint).onFailure {
            if (it is Error.Duplicate) {
                coroutineScope.launch {
                    _showToast.emit(getString(R.string.error_duplicate_constraint))
                }
            }
        }
    }

    fun onRemoveConstraintClick(id: String) = useCase.removeConstraint(id)

    fun onAndRadioButtonCheckedChange(checked: Boolean) {
        if (checked) {
            useCase.setAndMode()
        }
    }

    fun onOrRadioButtonCheckedChange(checked: Boolean) {
        if (checked) {
            useCase.setOrMode()
        }
    }

    fun onListItemClick(id: String) {
        coroutineScope.launch {
            useCase.state.firstOrNull()?.ifIsData { state ->
                val error = getError(state.list.singleOrNull { it.uid == id } ?: return@launch)

                if (error is FixableError) {
                    _fixError.emit(error)
                }
            }
        }
    }

    fun rebuildUiState() {
        runBlocking { rebuildUiState.emit(Unit) }
    }

    private fun buildState(state: State<ConfigConstraintsState>): ConstraintListViewState {
        return when (state) {
            is State.Data ->
                ConstraintListViewState(
                    constraintList = state.data.list.map { modelCreator.map(it) }.createListState(),
                    showModeRadioButtons = state.data.list.size > 1,
                    isAndModeChecked = state.data.mode == ConstraintMode.AND,
                    isOrModeChecked = state.data.mode == ConstraintMode.OR
                )

            is State.Loading ->
                ConstraintListViewState(
                    constraintList = ListUiState.Loading,
                    showModeRadioButtons = false,
                    isAndModeChecked = false,
                    isOrModeChecked = false
                )
        }
    }
}

data class ConstraintListViewState(
    val constraintList: ListUiState<ConstraintListItem>,
    val showModeRadioButtons: Boolean,
    val isAndModeChecked: Boolean,
    val isOrModeChecked: Boolean
)