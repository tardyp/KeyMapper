package io.github.sds100.keymapper.system.accessibility

import io.github.sds100.keymapper.util.InputEventType
import kotlinx.coroutines.flow.Flow

/**
 * Created by sds100 on 17/04/2021.
 */
interface IAccessibilityService {
    fun performGlobalAction(action: Int): Boolean

    fun tapScreen(x: Int, y: Int, inputEventType: InputEventType)
    val isGestureDetectionAvailable: Boolean
    fun requestFingerprintGestureDetection()
    fun denyFingerprintGestureDetection()

    val foregroundAppPackageName: String?
    fun performActionOnFocussedNode(action: Int)

    fun hideKeyboard()
    fun showKeyboard()
    val onKeyboardHiddenChange: Flow<Boolean>
}