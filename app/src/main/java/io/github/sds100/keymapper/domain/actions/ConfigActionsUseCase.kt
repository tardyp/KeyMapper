package io.github.sds100.keymapper.domain.actions

import io.github.sds100.keymapper.util.DataState
import kotlinx.coroutines.flow.Flow

interface ConfigActionsUseCase<A : Action> {
    val actionList: Flow<DataState<List<A>>>
    fun addAction(action: ActionData)
    fun moveAction(fromIndex: Int, toIndex: Int)
    fun removeAction(uid: String)
}