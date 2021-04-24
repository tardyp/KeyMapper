package io.github.sds100.keymapper.actions

import io.github.sds100.keymapper.system.accessibility.ServiceAdapter
import io.github.sds100.keymapper.util.TestActionEvent
import io.github.sds100.keymapper.util.Result

/**
 * Created by sds100 on 20/02/2021.
 */

class TestActionUseCaseImpl(
    private val serviceAdapter: ServiceAdapter
) : TestActionUseCase {
    override suspend fun invoke(action: ActionData): Result<*> {

        return serviceAdapter.send(TestActionEvent(action))
    }
}

interface TestActionUseCase {
    suspend operator fun invoke(action: ActionData): Result<*>
}