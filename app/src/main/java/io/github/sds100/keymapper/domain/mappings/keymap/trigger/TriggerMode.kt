package io.github.sds100.keymapper.domain.mappings.keymap.trigger

import io.github.sds100.keymapper.domain.utils.ClickType
import kotlinx.serialization.Serializable

/**
 * Created by sds100 on 21/02/2021.
 */

@Serializable
sealed class TriggerMode {
    @Serializable
    data class Parallel(val clickType: ClickType) : TriggerMode()

    @Serializable
    object Sequence : TriggerMode()

    @Serializable
    object Undefined : TriggerMode()
}