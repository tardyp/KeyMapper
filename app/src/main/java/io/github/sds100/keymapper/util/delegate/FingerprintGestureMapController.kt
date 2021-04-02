package io.github.sds100.keymapper.util.delegate

import android.accessibilityservice.FingerprintGestureController
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.FingerprintMap
import io.github.sds100.keymapper.domain.usecases.PerformActionsUseCase
import io.github.sds100.keymapper.util.IActionError
import io.github.sds100.keymapper.util.IConstraintDelegate
import kotlinx.coroutines.CoroutineScope

/**
 * Created by sds100 on 11/12/20.
 */
class FingerprintGestureMapController(
    coroutineScope: CoroutineScope,
    performActionsUseCase: PerformActionsUseCase,
    iConstraintDelegate: IConstraintDelegate,
    iActionError: IActionError
) : SimpleMappingController(
    coroutineScope,
    performActionsUseCase,
    iConstraintDelegate,
    iActionError
) {

    private var swipeDown: FingerprintMap? = null
    private var swipeUp: FingerprintMap? = null
    private var swipeLeft: FingerprintMap? = null
    private var swipeRight: FingerprintMap? = null

    fun setFingerprintMaps(
        swipeDown: FingerprintMap,
        swipeUp: FingerprintMap,
        swipeLeft: FingerprintMap,
        swipeRight: FingerprintMap
    ) {
        reset()
        this.swipeDown = swipeDown
        this.swipeUp = swipeUp
        this.swipeLeft = swipeLeft
        this.swipeRight = swipeRight
    }

    fun onGesture(sdkGestureId: Int) {
        val fingerprintMap = when (sdkGestureId) {
            FingerprintGestureController.FINGERPRINT_GESTURE_SWIPE_DOWN -> swipeDown
            FingerprintGestureController.FINGERPRINT_GESTURE_SWIPE_UP -> swipeUp
            FingerprintGestureController.FINGERPRINT_GESTURE_SWIPE_LEFT -> swipeLeft
            FingerprintGestureController.FINGERPRINT_GESTURE_SWIPE_RIGHT -> swipeRight
            else -> throw IllegalArgumentException("Don't know how to convert sdk fingerprint gesture id $sdkGestureId to key map id")
        }

        fingerprintMap.apply {
            //TODO
//            onDetected(
//                fingerprintMapId.toString(),
//                actionList,
//                constraintList,
//                constraintMode,
//                isEnabled,
//                extras,
//                flags.hasFlag(FingerprintMapEntity.FLAG_VIBRATE),
//                flags.hasFlag(FingerprintMapEntity.FLAG_SHOW_TOAST)
//            )
        }
    }
}