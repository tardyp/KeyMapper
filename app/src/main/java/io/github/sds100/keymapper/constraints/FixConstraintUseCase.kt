package io.github.sds100.keymapper.constraints

import androidx.constraintlayout.solver.state.State
import io.github.sds100.keymapper.domain.actions.ActionData
import io.github.sds100.keymapper.domain.constraints.Constraint

/**
 * Created by sds100 on 03/04/2021.
 */
interface FixConstraintUseCase {
    fun fix(constraint: Constraint)
}