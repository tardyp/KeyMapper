package io.github.sds100.keymapper.domain.actions

import android.os.Parcelable
import io.github.sds100.keymapper.domain.models.Action
import io.github.sds100.keymapper.util.*
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

/**
 * Created by sds100 on 20/02/2021.
 */

class ConfigActionsUseCaseImpl<O>(
    private val invalidateOptions: (actionList: List<ActionWithOptions<O>>) -> List<ActionWithOptions<O>>,
    private val getDefaultOptions: (action: Action) -> O
) : ConfigActionsUseCase<O> {

    override val actionList = MutableStateFlow<DataState<List<ActionWithOptions<O>>>>(Loading())

    fun setActionList(actions: List<ActionWithOptions<O>>) {
        actionList.value = actions.getDataState()
    }

    override fun addAction(action: Action) {
        actionList.value.apply {
            val newAction = ActionWithOptions(action, getDefaultOptions(action))

            val newList = when (this) {
                is Empty -> listOf(newAction)
                is Data -> this.data.plus(newAction)
                else -> return@apply
            }

            actionList.value = Data(invalidateOptions(newList))
        }
    }

    override fun moveAction(fromIndex: Int, toIndex: Int) = actionList.value.ifIsData { data ->
        data.toMutableList().apply {
            if (fromIndex < toIndex) {
                for (i in fromIndex until toIndex) {
                    Collections.swap(this, i, i + 1)
                }
            } else {
                for (i in fromIndex downTo toIndex + 1) {
                    Collections.swap(this, i, i - 1)
                }
            }

            actionList.value = Data(invalidateOptions(this))
        }
    }

    override fun removeAction(uid: String) = actionList.value.ifIsData { data ->
        data.toMutableList().apply {
            removeAll { it.action.uid == uid }

            actionList.value = Data(invalidateOptions(this))
        }
    }

    override fun getOptions(uid: String): O =
        actionList.value
            .single { it.action.uid == uid }.options

    override fun setOptions(uid: String, options: O) {
        actionList.value = actionList.value.map {
            if (it.action.uid == uid) {
                return@map it.copy(options = options)
            }

            it
        }

        invalidateOptions()
    }
}

interface ConfigActionsUseCase<O> {
    val actionList: StateFlow<DataState<List<ActionWithOptions<O>>>>
    fun addAction(action: Action)
    fun getOptions(uid: String): O
    fun setOptions(uid: String, options: O)
    fun moveAction(fromIndex: Int, toIndex: Int)
    fun removeAction(uid: String)
}

interface ConfigActionOptionsUseCase {
}

@Parcelize
data class ActionWithOptions<O>(val action: Action, val options: O) : Parcelable