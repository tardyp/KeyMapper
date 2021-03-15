package io.github.sds100.keymapper.domain.actions

/**
 * Created by sds100 on 21/02/2021.
 */

interface Action {
    val uid: String
    val data: ActionData
}