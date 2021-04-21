package io.github.sds100.keymapper.system.inputmethod

import io.github.sds100.keymapper.Constants
import io.github.sds100.keymapper.util.*

/**
 * Created by sds100 on 16/03/2021.
 */

class KeyMapperImeHelper(private val imeAdapter: InputMethodAdapter) {
    companion object {
        const val KEY_MAPPER_GUI_IME_PACKAGE =
            "io.github.sds100.keymapper.inputmethod.latin"

        val KEY_MAPPER_IME_PACKAGE_LIST = arrayOf(
            Constants.PACKAGE_NAME,
            KEY_MAPPER_GUI_IME_PACKAGE
        )
    }

    fun enableCompatibleInputMethods() {
        KEY_MAPPER_IME_PACKAGE_LIST.forEach { packageName ->
            imeAdapter.getInfoByPackageName(packageName).onSuccess {
                imeAdapter.enableIme(it.id)
            }
        }
    }

    suspend fun chooseCompatibleInputMethod(fromForeground: Boolean): Result<ImeInfo> {

        getLastUsedCompatibleImeId().onSuccess {
            return imeAdapter.chooseIme(it, fromForeground)
        }

        return imeAdapter.getInfoByPackageName(Constants.PACKAGE_NAME).suspendThen {
            imeAdapter.chooseIme(it.id, fromForeground)
        }
    }

    suspend fun chooseLastUsedIncompatibleInputMethod(fromForeground: Boolean): Result<ImeInfo> {
        return getLastUsedIncompatibleImeId().suspendThen {
            imeAdapter.chooseIme(it, fromForeground)
        }
    }

    suspend fun toggleCompatibleInputMethod(fromForeground: Boolean): Result<ImeInfo> {
        return if (isCompatibleImeChosen()) {
            chooseLastUsedIncompatibleInputMethod(fromForeground)
        } else {
            chooseCompatibleInputMethod(fromForeground)
        }
    }

    fun isCompatibleImeChosen(): Boolean {
        return imeAdapter.chosenIme.value.packageName in KEY_MAPPER_IME_PACKAGE_LIST
    }

    fun isCompatibleImeEnabled(): Boolean {
        return imeAdapter.inputMethods
            .firstBlocking()
            .filter { it.isEnabled }
            .any { it.packageName in KEY_MAPPER_IME_PACKAGE_LIST }
    }

    private fun getLastUsedCompatibleImeId(): Result<String> {
        for (ime in imeAdapter.inputMethodHistory.firstBlocking()) {
            if (ime.packageName in KEY_MAPPER_IME_PACKAGE_LIST) {
                return Success(ime.id)
            }
        }

        return imeAdapter.getInfoByPackageName(Constants.PACKAGE_NAME).then { Success(it.id) }
    }

    private fun getLastUsedIncompatibleImeId(): Result<String> {
        for (ime in imeAdapter.inputMethodHistory.firstBlocking()) {
            if (ime.packageName !in KEY_MAPPER_IME_PACKAGE_LIST) {
                return Success(ime.id)
            }
        }

        return Error.NoIncompatibleKeyboardsInstalled
    }
}