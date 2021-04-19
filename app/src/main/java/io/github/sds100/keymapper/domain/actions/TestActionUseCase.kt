package io.github.sds100.keymapper.domain.actions

import io.github.sds100.keymapper.domain.adapter.ServiceAdapter
import io.github.sds100.keymapper.util.TestActionEvent
import io.github.sds100.keymapper.util.result.Result

/**
 * Created by sds100 on 20/02/2021.
 */

class TestActionUseCaseImpl(
    private val serviceAdapter: ServiceAdapter
) : TestActionUseCase {
    override fun invoke(action: ActionData): Result<Unit> {

        return serviceAdapter.send(TestActionEvent(action))
    }
}

interface TestActionUseCase {
    operator fun invoke(action: ActionData): Result<Unit>
}