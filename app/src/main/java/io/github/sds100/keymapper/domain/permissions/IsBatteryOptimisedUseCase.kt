package io.github.sds100.keymapper.domain.permissions

import io.github.sds100.keymapper.domain.adapter.PowerManagementAdapter

/**
 * Created by sds100 on 02/04/2021.
 */

class IsBatteryOptimisedUseCaseImpl(
    private val adapter: PowerManagementAdapter
) : IsBatteryOptimisedUseCase {
    override fun invoke(): Boolean = !adapter.isIgnoringBatteryOptimisation
}

interface IsBatteryOptimisedUseCase {
    operator fun invoke(): Boolean
}