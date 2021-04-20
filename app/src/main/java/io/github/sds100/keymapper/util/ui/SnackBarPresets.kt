package io.github.sds100.keymapper.util

import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.ViewDataBinding
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.util.ui.SnackBarUi
import splitties.snackbar.longSnack
import splitties.snackbar.snack

/**
 * Created by sds100 on 04/12/20.
 */

//TODO delete
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