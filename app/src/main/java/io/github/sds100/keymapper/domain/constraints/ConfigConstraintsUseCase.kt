package io.github.sds100.keymapper.domain.constraints

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Created by sds100 on 03/03/2021.
 */

class ConfigConstraintUseCaseImpl : ConfigConstraintsUseCase {
    override val constraintList = MutableStateFlow<List<Constraint>>(emptyList())
    override val mode = MutableStateFlow(ConstraintMode.OR)
}

interface ConfigConstraintsUseCase {
    val constraintList: StateFlow<List<Constraint>>
    val mode: StateFlow<ConstraintMode>
}