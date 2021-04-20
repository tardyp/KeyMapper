package io.github.sds100.keymapper.actions

import io.github.sds100.keymapper.util.Error
import io.github.sds100.keymapper.util.InputEventType
import io.github.sds100.keymapper.util.Result
import io.github.sds100.keymapper.util.Success
import timber.log.Timber

/**
 * Created by sds100 on 14/02/21.
 */

class PerformActionsUseCaseImpl(
    private val getActionError: GetActionErrorUseCase
) : PerformActionsUseCase {

    override fun performAction(
        actionData: ActionData,
        inputEventType: InputEventType,
        keyMetaState: Int
    ): Result<*> {
        // TODO("Not yet implemented")
        Timber.e("perform $actionData $inputEventType $keyMetaState")
        return Success(Unit)
    }

    override fun getError(action: ActionData): Error? {
        return getActionError.getError(action)
    }
}

interface PerformActionsUseCase {
    fun performAction(
        actionData: ActionData,
        inputEventType: InputEventType = InputEventType.DOWN_UP,
        keyMetaState: Int = 0
    ): Result<*>

    fun getError(action: ActionData): Error?
}