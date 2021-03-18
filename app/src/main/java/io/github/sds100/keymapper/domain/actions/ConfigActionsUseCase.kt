package io.github.sds100.keymapper.domain.actions

import io.github.sds100.keymapper.domain.utils.State
import kotlinx.coroutines.flow.Flow

interface ConfigActionsUseCase<A : Action> {
    val actionList: Flow<State<List<A>>>
    fun addAction(action: ActionData)
    fun moveAction(fromIndex: Int, toIndex: Int)
    fun removeAction(uid: String)
}