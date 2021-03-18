package io.github.sds100.keymapper.domain.adapter

import io.github.sds100.keymapper.util.Event
import io.github.sds100.keymapper.util.result.Result
import kotlinx.coroutines.flow.SharedFlow

/**
 * Created by sds100 on 17/03/2021.
 */
interface ServiceAdapter {
    fun send(event: Event): Result<Unit>
    val eventReceiver: SharedFlow<Event>
}