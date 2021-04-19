package io.github.sds100.keymapper.domain.usecases

import io.github.sds100.keymapper.domain.actions.ActionData
import io.github.sds100.keymapper.domain.actions.GetActionErrorUseCase
import io.github.sds100.keymapper.util.InputEventType
import io.github.sds100.keymapper.util.result.Error
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
    ) {
       // TODO("Not yet implemented")
        Timber.e("perform $actionData $inputEventType $keyMetaState")
    }

    override fun getError(action: ActionData): Error? {
        return getActionError.getError(action)
    }
}

interface PerformActionsUseCase {
    fun performAction(actionData: ActionData,
                      inputEventType: InputEventType = InputEventType.DOWN_UP,
                      keyMetaState: Int = 0
    )

    fun getError(action: ActionData): Error?
}