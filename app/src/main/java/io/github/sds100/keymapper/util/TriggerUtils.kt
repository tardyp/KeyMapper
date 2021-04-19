package io.github.sds100.keymapper.util

import io.github.sds100.keymapper.data.entities.TriggerEntity
import splitties.bitflags.hasFlag

/**
 * Created by sds100 on 02/03/2020.
 */

//TODO remove this file

val TriggerEntity.triggerFromOtherApps: Boolean
    get() = flags.hasFlag(TriggerEntity.TRIGGER_FLAG_FROM_OTHER_APPS)

val TriggerEntity.showToast: Boolean
    get() = flags.hasFlag(TriggerEntity.TRIGGER_FLAG_SHOW_TOAST)

val TriggerEntity.vibrate: Boolean
    get() = flags.hasFlag(TriggerEntity.TRIGGER_FLAG_VIBRATE)