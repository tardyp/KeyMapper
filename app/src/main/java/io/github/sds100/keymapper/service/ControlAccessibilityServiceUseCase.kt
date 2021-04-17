package io.github.sds100.keymapper.service

import io.github.sds100.keymapper.domain.adapter.ServiceAdapter
import kotlinx.coroutines.flow.Flow

/**
 * Created by sds100 on 16/04/2021.
 */

class ControlAccessibilityServiceUseCaseImpl(
    private val adapter: ServiceAdapter
):ControlAccessibilityServiceUseCase{
    override val isEnabled: Flow<Boolean> = adapter.isEnabled

    override fun enable() {
        adapter.enableService()
    }

    override fun disable() {
        adapter.disableService()
    }
}

interface ControlAccessibilityServiceUseCase {
    val isEnabled: Flow<Boolean>
    fun enable()
    fun disable()
}