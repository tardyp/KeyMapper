package io.github.sds100.keymapper.ui.utils

import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.domain.utils.RingerMode

/**
 * Created by sds100 on 23/03/2021.
 */
object RingerModeUtils {
    fun getLabel(ringerMode: RingerMode) = when (ringerMode) {
        RingerMode.NORMAL -> R.string.ringer_mode_normal
        RingerMode.VIBRATE -> R.string.ringer_mode_vibrate
        RingerMode.SILENT -> R.string.ringer_mode_silent
    }
}