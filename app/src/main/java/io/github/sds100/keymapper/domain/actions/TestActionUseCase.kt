package io.github.sds100.keymapper.domain.actions

import io.github.sds100.keymapper.util.result.Result
import io.github.sds100.keymapper.util.result.Success

/**
 * Created by sds100 on 20/02/2021.
 */

class TestActionUseCaseImpl : TestActionUseCase {
    override fun invoke(action: ActionData): Result<Unit> {
        return Success(Unit)
    }
}

interface TestActionUseCase {
    operator fun invoke(action: ActionData): Result<Unit>
}