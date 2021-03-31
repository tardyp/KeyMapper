package io.github.sds100.keymapper.util

import android.view.KeyEvent
import io.github.sds100.keymapper.data.model.TriggerEntity
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.TriggerKey
import splitties.bitflags.hasFlag

/**
 * Created by sds100 on 02/03/2020.
 */

val TriggerEntity.triggerFromOtherApps: Boolean
    get() = flags.hasFlag(TriggerEntity.TRIGGER_FLAG_FROM_OTHER_APPS)

val TriggerEntity.showToast: Boolean
    get() = flags.hasFlag(TriggerEntity.TRIGGER_FLAG_SHOW_TOAST)

val TriggerEntity.vibrate: Boolean
    get() = flags.hasFlag(TriggerEntity.TRIGGER_FLAG_VIBRATE)

val TriggerKey.requiresDndAccessToImitate: Boolean
    get() = this.keyCode in arrayOf(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP)