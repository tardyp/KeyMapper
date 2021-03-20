package io.github.sds100.keymapper.domain.constraints

import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.util.result.Error
import io.github.sds100.keymapper.util.result.Result
import io.github.sds100.keymapper.util.result.Success
import kotlinx.coroutines.flow.Flow

/**
 * Created by sds100 on 03/03/2021.
 */

class ConfigConstraintUseCaseImpl(
    override val state: Flow<State<ConfigConstraintsState>>,
    val editState: (block: (ConfigConstraintsState) -> ConfigConstraintsState) -> Unit
) : ConfigConstraintsUseCase {

    override fun addConstraint(constraint: Constraint): Result<Unit> {
        var containsConstraint = false

        editState { state ->
            containsConstraint = state.list.contains(constraint)
            state.copy(list = state.list.plus(constraint))
        }

        if (containsConstraint) {
            return Error.Duplicate
        } else {
            return Success(Unit)
        }
    }

    override fun removeConstraint(id: String) {
        editState { state ->
            val newList = state.list.toMutableSet().apply {
                removeAll { it.uid == id }
            }

            state.copy(list = newList)
        }
    }

    override fun setAndMode() {
        editState { state -> state.copy(mode = ConstraintMode.AND) }
    }

    override fun setOrMode() {
        editState { state -> state.copy(mode = ConstraintMode.OR) }
    }
}

data class ConfigConstraintsState(
    val list: Set<Constraint>,
    val mode: ConstraintMode
)

interface ConfigConstraintsUseCase {
    val state: Flow<State<ConfigConstraintsState>>

    fun addConstraint(constraint: Constraint): Result<Unit>
    fun removeConstraint(id: String)

    fun setAndMode()
    fun setOrMode()
}