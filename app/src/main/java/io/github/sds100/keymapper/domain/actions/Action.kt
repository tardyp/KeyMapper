package io.github.sds100.keymapper.domain.actions

/**
 * Created by sds100 on 21/02/2021.
 */

interface Action {
    val uid: String
    val data: ActionData
    val multiplier: Int?
    val delayBeforeNextAction: Int?
    val repeat: Boolean
    val repeatRate: Int?
    val holdDown: Boolean
    val holdDownDuration: Int?
}