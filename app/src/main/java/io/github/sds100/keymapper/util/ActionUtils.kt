
package io.github.sds100.keymapper.util

import android.content.Context
import android.os.Build
import io.github.sds100.keymapper.data.entities.ActionEntity
import io.github.sds100.keymapper.data.entities.getData
import io.github.sds100.keymapper.util.result.valueOrNull
import splitties.bitflags.hasFlag

/**
 * Created by sds100 on 03/09/2018.
 */

val ActionEntity.repeat: Boolean
    get() = flags.hasFlag(ActionEntity.ACTION_FLAG_REPEAT)

val ActionEntity.holdDown: Boolean
    get() = flags.hasFlag(ActionEntity.ACTION_FLAG_HOLD_DOWN)

val ActionEntity.delayBeforeNextAction: Int?
    get() = extras.getData(ActionEntity.EXTRA_DELAY_BEFORE_NEXT_ACTION).valueOrNull()?.toInt()

val ActionEntity.multiplier: Int?
    get() = extras.getData(ActionEntity.EXTRA_MULTIPLIER).valueOrNull()?.toInt()

val ActionEntity.holdDownDuration: Int?
    get() = extras.getData(ActionEntity.EXTRA_HOLD_DOWN_DURATION).valueOrNull()?.toInt()

val ActionEntity.repeatRate: Int?
    get() = extras.getData(ActionEntity.EXTRA_REPEAT_RATE).valueOrNull()?.toInt()