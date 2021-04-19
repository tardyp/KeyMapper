package io.github.sds100.keymapper.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.os.Handler
import android.view.accessibility.AccessibilityNodeInfo
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