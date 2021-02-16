package io.github.sds100.keymapper.util.delegate

/**
 * Created by sds100 on 14/02/2021.
 */
interface FingerprintGestureDetectionState {
    fun requestFingerprintGestureDetection()
    fun denyFingerprintGestureDetection()
    val isGestureDetectionAvailable: Boolean
}