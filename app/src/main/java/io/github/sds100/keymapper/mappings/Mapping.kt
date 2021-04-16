package io.github.sds100.keymapper.mappings

import io.github.sds100.keymapper.constraints.ConstraintState
import io.github.sds100.keymapper.domain.actions.Action

/**
 * Created by sds100 on 04/04/2021.
 */

interface Mapping<ACTION: Action> {
    val isEnabled: Boolean
    val constraintState: ConstraintState
    val actionList: List<ACTION>
}

fun Mapping<*>.isDelayBeforeNextActionAllowed(): Boolean {
    return actionList.isNotEmpty()
}