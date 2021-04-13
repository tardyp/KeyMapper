package io.github.sds100.keymapper.ui.utils

import android.view.View
import io.github.sds100.keymapper.ui.dialogs.GetUserResponse
import kotlinx.coroutines.suspendCancellableCoroutine
import splitties.snackbar.action
import splitties.snackbar.longSnack
import splitties.snackbar.onDismiss
import splitties.snackbar.snack
import kotlin.coroutines.resume

/**
 * Created by sds100 on 06/04/2021.
 */
object SnackBarUtils {

    suspend fun show(view: View, text: String, actionText: String? = null, long: Boolean = false) =
        suspendCancellableCoroutine<GetUserResponse.SnackBarActionResponse?> { continuation ->

            val snackBar = if (long) {
                view.longSnack(text) {
                    if (actionText != null) {
                        action(actionText) {
                            continuation.resume(GetUserResponse.SnackBarActionResponse)
                        }
                    }
                }
            } else {
                view.snack(text) {
                    if (actionText != null) {
                        action(actionText) {
                            continuation.resume(GetUserResponse.SnackBarActionResponse)
                        }
                    }
                }
            }

            snackBar.onDismiss {
                if (!continuation.isCompleted) {
                    continuation.resume(null)
                }
            }
        }

}