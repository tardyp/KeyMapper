package io.github.sds100.keymapper.framework.adapters

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import io.github.sds100.keymapper.Constants
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.domain.adapter.InputMethodAdapter
import io.github.sds100.keymapper.util.KeyboardUtils
import io.github.sds100.keymapper.util.PermissionUtils
import io.github.sds100.keymapper.util.RootUtils
import io.github.sds100.keymapper.util.result.*
import splitties.toast.toast

/**
 * Created by sds100 on 14/02/2021.
 */
internal class AndroidInputMethodAdapter(context: Context) : InputMethodAdapter {
    companion object {
        private const val SETTINGS_SECURE_SUBTYPE_HISTORY_KEY = "input_methods_subtype_history"
    }

    private val ctx = context.applicationContext

    override fun chooseLastUsedIncompatibleInputMethod() {
        getLastUsedIncompatibleImeId(ctx).onSuccess {
            KeyboardUtils.switchIme(ctx, it)
        }
    }

    override fun chooseCompatibleInputMethod() {
        if (PermissionUtils.haveWriteSecureSettingsPermission(ctx)) {
            getLastUsedCompatibleImeId(ctx).onSuccess {
                KeyboardUtils.switchIme(ctx, it)
                return
            }

            KeyboardUtils.getImeId(Constants.PACKAGE_NAME).valueOrNull()?.let {
                KeyboardUtils.switchIme(ctx, it)
                return
            }
        }

        KeyboardUtils.showInputMethodPicker()
    }

    override fun showImePickerOutsideApp() {
        /* Android 8.1 and higher don't seem to allow you to open the input method picker dialog
             * from outside the app :( but it can be achieved by sending a broadcast with a
             * system process id (requires root access) */

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
            ctx.getSystemService<InputMethodManager>()?.showInputMethodPicker()
        } else if ((Build.VERSION_CODES.O_MR1..Build.VERSION_CODES.P).contains(Build.VERSION.SDK_INT)) {
            val command =
                "am broadcast -a com.android.server.InputMethodManagerService.SHOW_INPUT_METHOD_PICKER"
            RootUtils.executeRootCommand(command)
        } else {
            ctx.toast(R.string.error_this_is_unsupported)
        }
    }

    private fun getSubtypeHistoryString(ctx: Context): String {
        return Settings.Secure.getString(
            ctx.contentResolver,
            SETTINGS_SECURE_SUBTYPE_HISTORY_KEY
        )
    }

    /**
     * Example:
     * io.github.sds100.keymapper.inputmethod.latin/.LatinIME;-921088104
     * :com.google.android.inputmethod.latin/com.android.inputmethod.latin.LatinIME;1891618174
     */
    private fun getInputMethodHistoryIds(ctx: Context): List<String> {
        return getSubtypeHistoryString(ctx)
            .split(':')
            .map { it.split(';')[0] }
    }

    private fun getLastUsedCompatibleImeId(ctx: Context): Result<String> {
        for (id in getInputMethodHistoryIds(ctx)) {
            if (id.split('/')[0] in KeyboardUtils.KEY_MAPPER_IME_PACKAGE_LIST) {
                return Success(id)
            }
        }

        return KeyboardUtils.getImeId(Constants.PACKAGE_NAME)
    }

    private fun getLastUsedIncompatibleImeId(ctx: Context): Result<String> {
        for (id in getInputMethodHistoryIds(ctx)) {
            if (id.split('/')[0] != Constants.PACKAGE_NAME) {
                return Success(id)
            }
        }

        return NoIncompatibleKeyboardsInstalled()
    }
}