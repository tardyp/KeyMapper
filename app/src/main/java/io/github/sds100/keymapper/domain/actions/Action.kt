package io.github.sds100.keymapper.domain.actions

import io.github.sds100.keymapper.domain.models.Option
import io.github.sds100.keymapper.domain.utils.Defaultable

/**
 * Created by sds100 on 21/02/2021.
 */

interface Action {
    companion object{
        const val ACTION_MULTIPLIER_MIN = 1
        const val ACTION_MULTIPLIER_SLIDER_MAX = 20

        const val DELAY_BEFORE_NEXT_ACTION_MIN = 0
        const val DELAY_BEFORE_NEXT_ACTION_SLIDER_STEP_SIZE = 10
        const val DELAY_BEFORE_NEXT_ACTION_SLIDER_MAX = 2000
    }

    val uid: String
    val data: ActionData
    val multiplier: Int?
    val delayBeforeNextAction: Int?
}