package io.github.sds100.keymapper.system.navigation

import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import io.github.sds100.keymapper.system.accessibility.findNodeRecursively
import io.github.sds100.keymapper.system.permissions.CheckRootPermissionUseCase
import io.github.sds100.keymapper.system.root.SuProcessDelegate
import io.github.sds100.keymapper.util.Error
import io.github.sds100.keymapper.util.Result
import io.github.sds100.keymapper.util.Success
import io.github.sds100.keymapper.util.firstBlocking

/**
 * Created by sds100 on 20/04/2021.
 */
class AndroidNavigationAdapter(
    private val accessibilityService: AccessibilityService,
    private val suProcess: SuProcessDelegate,
    private val checkRootPermission: CheckRootPermissionUseCase
) : NavigationAdapter {
    companion object {
        private const val OVERFLOW_MENU_CONTENT_DESCRIPTION = "More options"
    }

    override fun goBack(): Result<*> {
        val success = accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)

        if (success) {
            return Success(Unit)
        } else {
            return Error.FailedToPerformAccessibilityGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        }
    }

    override fun goHome(): Result<*> {
        val success = accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)

        if (success) {
            return Success(Unit)
        } else {
            return Error.FailedToPerformAccessibilityGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
        }    }

    override fun openRecents(): Result<*> {
        val success = accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)

        if (success) {
            return Success(Unit)
        } else {
            return Error.FailedToPerformAccessibilityGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
        }    }

    override fun openMenu(): Result<*> {
        if (checkRootPermission.isGranted.firstBlocking()) {
            suProcess.runCommand("input keyevent ${KeyEvent.KEYCODE_MENU}\n")
        } else {
            val node = accessibilityService.rootInActiveWindow.findNodeRecursively {
                it.contentDescription == OVERFLOW_MENU_CONTENT_DESCRIPTION
            } ?: return Error.FailedToFindAccessibilityNode

            node.performAction(AccessibilityNodeInfoCompat.ACTION_CLICK)
            node.recycle()
        }

        return Success(Unit)
    }
}