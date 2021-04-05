package io.github.sds100.keymapper.constraints

import io.github.sds100.keymapper.domain.constraints.Constraint
import io.github.sds100.keymapper.domain.constraints.ConstraintMode
import kotlinx.serialization.Serializable

/**
 * Created by sds100 on 04/04/2021.
 */

@Serializable
data class ConstraintState(
    val constraints: Set<Constraint> = emptySet(),
    val mode: ConstraintMode = ConstraintMode.AND
)
