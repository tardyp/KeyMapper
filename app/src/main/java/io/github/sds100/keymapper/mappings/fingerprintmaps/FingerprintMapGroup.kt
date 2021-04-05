package io.github.sds100.keymapper.mappings.fingerprintmaps

import io.github.sds100.keymapper.domain.mappings.fingerprintmap.FingerprintMap
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.FingerprintMapId

/**
 * Created by sds100 on 04/04/2021.
 */
data class FingerprintMapGroup(val swipeDown: FingerprintMap,
                               val swipeUp: FingerprintMap,
                               val swipeLeft: FingerprintMap,
                               val swipeRight: FingerprintMap){
    fun get(fingerprintMapId: FingerprintMapId): FingerprintMap{
        return when(fingerprintMapId){
            FingerprintMapId.SWIPE_DOWN -> swipeDown
            FingerprintMapId.SWIPE_UP -> swipeUp
            FingerprintMapId.SWIPE_LEFT -> swipeLeft
            FingerprintMapId.SWIPE_RIGHT -> swipeRight
        }
    }
}