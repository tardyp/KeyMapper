package io.github.sds100.keymapper.domain.adapter

import io.github.sds100.keymapper.util.Event
import io.github.sds100.keymapper.util.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Created by sds100 on 17/03/2021.
 */
interface ServiceAdapter {
    val isEnabled: StateFlow<Boolean>
    fun send(event: Event): Result<Unit>
    val eventReceiver: SharedFlow<Event>
}