package io.github.sds100.keymapper.domain.actions

import io.github.sds100.keymapper.data.repository.DeviceInfoCache
import io.github.sds100.keymapper.domain.adapter.InputMethodAdapter
import io.github.sds100.keymapper.domain.repositories.PreferenceRepository
import io.github.sds100.keymapper.util.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Created by sds100 on 15/02/2021.
 */
class GetActionErrorUseCaseImpl(
    preferenceRepository: PreferenceRepository,
    deviceInfoRepository: DeviceInfoCache,
    inputMethodAdapter: InputMethodAdapter
) : GetActionErrorUseCase {

    override val invalidateErrors = combine(inputMethodAdapter.chosenImePackageName) {}

    override fun getError(action: ActionData): Result<Unit> {
        TODO("Not yet implemented")
    }
}

interface GetActionErrorUseCase {
    val invalidateErrors: Flow<Unit>
    fun getError(action: ActionData): Result<Unit>
}