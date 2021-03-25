package io.github.sds100.keymapper.domain.ime

import io.github.sds100.keymapper.Constants
import io.github.sds100.keymapper.domain.adapter.InputMethodAdapter
import io.github.sds100.keymapper.util.result.onSuccess

/**
 * Created by sds100 on 16/03/2021.
 */

class KeyMapperImeManager(val adapter: InputMethodAdapter) {
    private companion object {
        const val KEY_MAPPER_GUI_IME_PACKAGE = "io.github.sds100.keymapper.inputmethod.latin"

        val KEY_MAPPER_IME_PACKAGE_LIST = arrayOf(
            Constants.PACKAGE_NAME,
            KEY_MAPPER_GUI_IME_PACKAGE
        )
    }

    fun enableCompatibleInputMethods() {

    }

    fun chooseCompatibleInputMethod(fromForeground: Boolean) {}
    fun chooseLastUsedIncompatibleInputMethod(fromForeground: Boolean) {}
    fun toggleCompatibleInputMethod() {}

    fun isCompatibleImeChosen(): Boolean {
        KEY_MAPPER_IME_PACKAGE_LIST.forEach { packageName ->
            adapter.getImeId(packageName).onSuccess {
                if (adapter.isImeChosen(it)) {
                    return true
                }
            }
        }

        return false
    }

    fun isCompatibleImeEnabled(): Boolean {
        KEY_MAPPER_IME_PACKAGE_LIST.forEach { packageName ->
            adapter.getImeId(packageName).onSuccess {
                if (adapter.isImeEnabled(it)) {
                    return true
                }
            }
        }

        return false
    }
}