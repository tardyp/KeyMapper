package io.github.sds100.keymapper.util

import android.accessibilityservice.FingerprintGestureController
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.data.model.ActiveEdgeMap
import io.github.sds100.keymapper.data.model.FingerprintMap
import splitties.bitflags.hasFlag

fun ActiveEdgeMap.getFlagLabelList(ctx: Context): List<String> = sequence {
    ActiveEdgeMap.FLAG_LABEL_MAP.keys.forEach { flag ->
        if (flags.hasFlag(flag)) {
            yield(ctx.str(ActiveEdgeMap.FLAG_LABEL_MAP.getValue(flag)))
        }
    }
}.toList()

fun ActiveEdgeMap.buildOptionsDescription(ctx: Context): String = buildString {
    getFlagLabelList(ctx).forEachIndexed { index, label ->
        if (index > 0) {
            append(" ${ctx.str(R.string.interpunct)} ")
        }

        append(label)
    }
}