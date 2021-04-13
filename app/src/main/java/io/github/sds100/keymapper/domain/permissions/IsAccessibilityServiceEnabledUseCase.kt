package io.github.sds100.keymapper.domain.permissions

import io.github.sds100.keymapper.domain.adapter.ServiceAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Created by sds100 on 02/04/2021.
 */

//TODO remove
class IsAccessibilityServiceEnabledUseCaseImpl(serviceAdapter: ServiceAdapter) : IsAccessibilityServiceEnabledUseCase {
    override val isEnabled = serviceAdapter.isEnabled
}

interface IsAccessibilityServiceEnabledUseCase {
    val isEnabled: StateFlow<Boolean>
}