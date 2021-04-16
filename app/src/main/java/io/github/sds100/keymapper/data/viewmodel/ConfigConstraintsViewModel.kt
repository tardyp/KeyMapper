package io.github.sds100.keymapper.data.viewmodel

import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.constraints.ChooseConstraintType
import io.github.sds100.keymapper.constraints.ConstraintState
import io.github.sds100.keymapper.domain.constraints.*
import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.domain.utils.ifIsData
import io.github.sds100.keymapper.domain.utils.mapData
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.mappings.ConfigMappingUseCase
import io.github.sds100.keymapper.mappings.DisplayConstraintUseCase
import io.github.sds100.keymapper.mappings.Mapping
import io.github.sds100.keymapper.ui.IconInfo
import io.github.sds100.keymapper.ui.ListUiState
import io.github.sds100.keymapper.ui.constraints.ConstraintListItem
import io.github.sds100.keymapper.ui.constraints.ConstraintUiHelper
import io.github.sds100.keymapper.ui.createListState
import io.github.sds100.keymapper.util.TintType
import io.github.sds100.keymapper.util.result.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Created by sds100 on 29/11/20.
 */

class ConfigConstraintsViewModel(
    private val coroutineScope: CoroutineScope,
    private val display: DisplayConstraintUseCase,
    private val config: ConfigMappingUseCase<*, *>,
    val allowedConstraints: Array<ChooseConstraintType>,
    resourceProvider: ResourceProvider
) : ResourceProvider by resourceProvider {

    private val uiHelper = ConstraintUiHelper(display, resourceProvider)

    private val _showToast = MutableSharedFlow<String>()
    val showToast = _showToast.asSharedFlow()

    private val _state = MutableStateFlow(buildState(State.Loading))
    val state = _state.asStateFlow()

    init {
        val rebuildUiState = MutableSharedFlow<State<Mapping<*>>>()

        coroutineScope.launch {
            rebuildUiState.collectLatest { mapping ->
                _state.value = buildState(mapping.mapData { it.constraintState })
            }
        }

        coroutineScope.launch {
            config.mapping.collectLatest {
                rebuildUiState.emit(it)
            }
        }

        coroutineScope.launch {
            display.invalidateErrors.collectLatest {
                rebuildUiState.emit(config.mapping.firstOrNull() ?: return@collectLatest)
            }
        }
    }

    fun onChosenNewConstraint(constraint: Constraint) {
        val isDuplicate = !config.addConstraint(constraint)

        if (isDuplicate) {
            coroutineScope.launch {
                _showToast.emit(getString(R.string.error_duplicate_constraint))
            }
        }
    }

    fun onRemoveConstraintClick(id: String) = config.removeConstraint(id)

    fun onAndRadioButtonCheckedChange(checked: Boolean) {
        if (checked) {
            config.setAndMode()
        }
    }

    fun onOrRadioButtonCheckedChange(checked: Boolean) {
        if (checked) {
            config.setOrMode()
        }
    }

    fun onListItemClick(id: String) {
        coroutineScope.launch {
            config.mapping.firstOrNull()?.ifIsData { mapping ->
                val constraint = mapping.constraintState.constraints.singleOrNull { it.uid == id }
                    ?: return@launch
                val error = display.getConstraintError(constraint)

                if (error is FixableError) {
                   display.fixError(error)
                }
            }
        }
    }

    private fun createListItem(constraint: Constraint): ConstraintListItem {
        val title: String = uiHelper.getTitle(constraint)
        val icon: IconInfo? = uiHelper.getIcon(constraint)
        val error: Error? = display.getConstraintError(constraint)

        return ConstraintListItem(
            id = constraint.uid,
            tintType = icon?.tintType ?: TintType.ERROR,
            icon = icon?.drawable ?: getDrawable(R.drawable.ic_baseline_error_outline_24),
            title = title,
            errorMessage = error?.getFullMessage(this)
        )
    }

    private fun buildState(state: State<ConstraintState>): ConstraintListViewState {
        return when (state) {
            is State.Data ->
                ConstraintListViewState(
                    constraintList = state.data.constraints.map { createListItem(it) }
                        .createListState(),
                    showModeRadioButtons = state.data.constraints.size > 1,
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