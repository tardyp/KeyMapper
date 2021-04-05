package io.github.sds100.keymapper.mappings.common

import io.github.sds100.keymapper.constraints.ConstraintState
import io.github.sds100.keymapper.domain.actions.Action
import io.github.sds100.keymapper.domain.actions.ActionData
import io.github.sds100.keymapper.domain.constraints.Constraint
import io.github.sds100.keymapper.domain.constraints.ConstraintMode
import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.domain.utils.ifIsData
import io.github.sds100.keymapper.domain.utils.moveElement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Created by sds100 on 04/04/2021.
 */

abstract class BaseConfigMappingUseCase<ACTION : Action, T : Mapping<ACTION>> : ConfigMappingUseCase<ACTION, T> {

    override val mapping = MutableStateFlow<State<T>>(State.Loading)

    override fun setMapping(mapping: T) {
        this.mapping.value = State.Data(mapping)
    }

    override fun getMapping(): State<T> = mapping.value

    override fun addConstraint(constraint: Constraint): Boolean {
        var containsConstraint = false

        mapping.value.ifIsData { mapping ->
            val oldState = mapping.constraintState

            containsConstraint = oldState.constraints.contains(constraint)
            val newState = oldState.copy(constraints = oldState.constraints.plus(constraint))

            setConstraintState(newState)
        }

        return !containsConstraint
    }

    override fun removeConstraint(id: String) {
        mapping.value.ifIsData { mapping ->
            val newList = mapping.constraintState.constraints.toMutableSet().apply {
                removeAll { it.uid == id }
            }

            setConstraintState(mapping.constraintState.copy(constraints = newList))
        }
    }

    override fun setAndMode() {
        mapping.value.ifIsData { mapping ->
            setConstraintState(mapping.constraintState.copy(mode = ConstraintMode.AND))
        }
    }

    override fun setOrMode() {
        mapping.value.ifIsData { mapping ->
            setConstraintState(mapping.constraintState.copy(mode = ConstraintMode.OR))
        }
    }

    override fun addAction(data: ActionData) = mapping.value.ifIsData { mapping ->
        mapping.actionList.toMutableList().apply {
            add(createAction(data))
            setActionList(this)
        }
    }

    override fun moveAction(fromIndex: Int, toIndex: Int) {
        mapping.value.ifIsData { mapping ->
            mapping.actionList.toMutableList().apply {
                moveElement(fromIndex, toIndex)
                setActionList(this)
            }
        }
    }

    override fun removeAction(uid: String) {
        mapping.value.ifIsData { mapping ->
            mapping.actionList.toMutableList().apply {
                removeAll { it.uid == uid }
                setActionList(this)
            }
        }
    }

    abstract fun createAction(data: ActionData): ACTION
    abstract fun setActionList(actionList: List<ACTION>)
    abstract fun setConstraintState(constraintState: ConstraintState)
}

interface ConfigMappingUseCase<ACTION : Action, T : Mapping<ACTION>> {
    val mapping: Flow<State<T>>

    fun setMapping(mapping: T)
    fun getMapping(): State<T>

    fun setEnabled(enabled: Boolean)

    fun addAction(data: ActionData)
    fun moveAction(fromIndex: Int, toIndex: Int)
    fun removeAction(uid: String)

    fun addConstraint(constraint: Constraint): Boolean
    fun removeConstraint(id: String)
    fun setAndMode()
    fun setOrMode()
}