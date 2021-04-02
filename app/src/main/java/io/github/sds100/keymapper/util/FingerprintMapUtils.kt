package io.github.sds100.keymapper.util

import android.accessibilityservice.FingerprintGestureController
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.data.model.FingerprintMapEntity
import splitties.bitflags.hasFlag

/**
 * Created by sds100 on 14/11/20.
 */
object FingerprintMapUtils {
    /**
     * Use version code for 2.2.0.beta.2 because in beta 1 there were issues detecting the
     * availability of fingerprint gestures.
     */
    const val FINGERPRINT_GESTURES_MIN_VERSION = 40
}