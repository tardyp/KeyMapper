package io.github.sds100.keymapper.system.accessibility

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityNodeInfo
import io.github.sds100.keymapper.Constants
import io.github.sds100.keymapper.MainActivity
import io.github.sds100.keymapper.system.SettingsUtils
import io.github.sds100.keymapper.system.permissions.PermissionUtils

/**
 * Created by sds100 on 06/08/2019.
 */

/**
 * @return The node to find. Returns null if the node doesn't match the predicate
 */
fun AccessibilityNodeInfo?.findNodeRecursively(
    nodeInfo: AccessibilityNodeInfo? = this,
    depth: Int = 0,
    predicate: (node: AccessibilityNodeInfo) -> Boolean
): AccessibilityNodeInfo? {
    if (nodeInfo == null) return null

    if (predicate(nodeInfo)) return nodeInfo

    for (i in 0 until nodeInfo.childCount) {
        val node = findNodeRecursively(nodeInfo.getChild(i), depth + 1, predicate)

        if (node != null) {
            return node
        }
    }

    return null
}

fun AccessibilityNodeInfo?.focusedNode(func: (node: AccessibilityNodeInfo?) -> Unit) {
    func.invoke(findNodeRecursively { it.isFocused })
}

fun AccessibilityNodeInfo?.performActionOnFocusedNode(action: Int) {
    focusedNode {
        it?.performAction(action)
        it?.recycle()
    }
}