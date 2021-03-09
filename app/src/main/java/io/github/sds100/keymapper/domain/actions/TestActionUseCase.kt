package io.github.sds100.keymapper.domain.actions

import io.github.sds100.keymapper.domain.models.Action
import io.github.sds100.keymapper.util.result.Result
import io.github.sds100.keymapper.util.result.Success

/**
 * Created by sds100 on 20/02/2021.
 */

class TestActionUseCaseImpl() : TestActionUseCase {
    override fun invoke(action: Action): Result<Action> {
        return Success(action)
    }
}

interface TestActionUseCase {
    operator fun invoke(action: Action): Result<Action>
}