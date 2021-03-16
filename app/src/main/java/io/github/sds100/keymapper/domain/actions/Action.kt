package io.github.sds100.keymapper.domain.actions

import io.github.sds100.keymapper.domain.models.Defaultable
import io.github.sds100.keymapper.domain.models.Option

/**
 * Created by sds100 on 21/02/2021.
 */

interface Action {
    val uid: String
    val data: ActionData
    val multiplier: Option<Defaultable<Int>>
    val delayBeforeNextAction: Option<Defaultable<Int>>
}