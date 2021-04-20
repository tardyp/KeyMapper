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

//TODO remove
object AccessibilityUtils {

    /**
     * @return whether the accessibility service is enabled
     */
    fun isServiceEnabled(ctx: Context): Boolean {
        /* get a list of all the enabled accessibility services.
         * The AccessibilityManager.getEnabledAccessibilityServices() method just returns an empty
         * list. :(*/
        val settingValue = Settings.Secure.getString(
            ctx.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )

        //it can be null if the user has never interacted with accessibility settings before
        if (settingValue != null) {
            /* cant just use .contains because the debug and release accessibility service both contain
               io.github.sds100.keymapper. the enabled_accessibility_services are stored as

                 io.github.sds100.keymapper.debug/io.github.sds100.keymapper.service.MyAccessibilityService
                 :io.github.sds100.keymapper/io.github.sds100.keymapper.service.MyAccessibilityService

                 without the new line before the :
            */
            return settingValue.split(':').any { it.split('/')[0] == ctx.packageName }
        }

        return false
    }
}

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