package io.github.sds100.keymapper.system.inputmethod

import io.github.sds100.keymapper.Constants
import timber.log.Timber

/**
 * Created by sds100 on 16/03/2021.
 */

class KeyMapperImeHelper(val adapter: InputMethodAdapter) {
    private companion object {
        const val KEY_MAPPER_GUI_IME_PACKAGE = "io.github.sds100.keymapper.system.inputmethod.latin"

        val KEY_MAPPER_IME_PACKAGE_LIST = arrayOf(
            Constants.PACKAGE_NAME,
            KEY_MAPPER_GUI_IME_PACKAGE
        )
    }

    fun enableCompatibleInputMethods() {
        KeyboardUtils.KEY_MAPPER_IME_PACKAGE_LIST.forEach {
            adapter.enableImeByPackageName(it)
        }
    }

    //TODO
    fun chooseCompatibleInputMethod(fromForeground: Boolean) {
        Timber.e("choose compatible ime")
    }
    fun chooseLastUsedIncompatibleInputMethod(fromForeground: Boolean) {
        Timber.e("choose incompatible ime")
    }

    fun toggleCompatibleInputMethod() {
        Timber.e("toggle incompatible ime")
    }

    fun isCompatibleImeChosen(): Boolean {
        KEY_MAPPER_IME_PACKAGE_LIST.forEach { packageName ->
            if (adapter.isImeChosenByPackageName(packageName)) {
                return true
            }
        }

        return false
    }

    fun isCompatibleImeEnabled(): Boolean {
        KEY_MAPPER_IME_PACKAGE_LIST.forEach { packageName ->
            if (adapter.isImeEnabledByPackageName(packageName)) {
                return true
            }
        }

        return false
    }
}