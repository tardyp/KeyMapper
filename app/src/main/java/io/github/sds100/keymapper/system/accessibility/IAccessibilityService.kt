package io.github.sds100.keymapper.system.accessibility

import io.github.sds100.keymapper.util.InputEventType
import io.github.sds100.keymapper.util.Result
import kotlinx.coroutines.flow.Flow

/**
 * Created by sds100 on 17/04/2021.
 */
interface IAccessibilityService {
    fun doGlobalAction(action: Int): Result<*>

    fun tapScreen(x: Int, y: Int, inputEventType: InputEventType): Result<*>
    val isGestureDetectionAvailable: Boolean
    fun requestFingerprintGestureDetection()
    fun denyFingerprintGestureDetection()

    fun performActionOnNode(action: Int, predicate: (node: AccessibilityNodeModel) -> Boolean): Result<*>
    val rootNode: AccessibilityNodeModel

    fun hideKeyboard()
    fun showKeyboard()
    fun switchIme(imeId: String)
    val onKeyboardHiddenChange: Flow<Boolean>
}