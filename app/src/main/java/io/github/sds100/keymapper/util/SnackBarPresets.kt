package io.github.sds100.keymapper.util

import android.content.Context
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.ViewDataBinding
import androidx.navigation.NavController
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.ui.SnackBarUi
import io.github.sds100.keymapper.util.delegate.FixErrorDelegate
import io.github.sds100.keymapper.util.result.Error
import io.github.sds100.keymapper.util.result.FixableError
import io.github.sds100.keymapper.util.result.getFullMessage
import splitties.snackbar.action
import splitties.snackbar.longSnack
import splitties.snackbar.snack

/**
 * Created by sds100 on 04/12/20.
 */

fun CoordinatorLayout.showEnableAccessibilityServiceSnackBar() {
    snack(R.string.error_accessibility_service_disabled_record_trigger) {
        setAction(str(R.string.snackbar_fix)) {
            AccessibilityUtils.enableService(context)
        }
    }
}

fun ViewDataBinding.showSnackBar(snackBarUi: SnackBarUi) {
    if (snackBarUi.long) {
        root.longSnack(snackBarUi.title) {
            show()
        }
    } else {
        root.snack(snackBarUi.title) {
            show()
        }
    }
}